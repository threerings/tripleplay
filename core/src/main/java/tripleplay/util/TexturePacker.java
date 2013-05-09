//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pythagoras.i.IRectangle;
import pythagoras.i.Rectangle;

import playn.core.Image;
import playn.core.Surface;
import playn.core.SurfaceImage;
import static playn.core.PlayN.*;

import react.Slot;

/**
 * A runtime texture packer.
 */
public class TexturePacker
{
    public interface Renderer {
        void render (Surface surface, IRectangle bounds);
    }

    /** Add an image to the packer. */
    public TexturePacker add (String id, Image image) {
        return addItem(new ImageItem(id, image));
    }

    /** Add a lazily rendered region to the packer. The renderer will be used to draw the region
     * each time pack() is called. */
    public TexturePacker add (String id, int width, int height, Renderer renderer) {
        return addItem(new RenderedItem(id, width, height, renderer));
    }

    /**
     * Pack all images into as few atlases as possible.
     * @return A map containing the new images, keyed by the id they were added with.
     */
    public Map<String,Image.Region> pack () {
        List<Item> unpacked = new ArrayList<Item>(_items.values());
        // TODO(bruno): Sort by some heuristic (area, perimeter)

        List<Atlas> atlases = new ArrayList<Atlas>();
        while (!unpacked.isEmpty()) {
            atlases.add(createAtlas());

            // Try to pack each item into any atlas
            for (Iterator<Item> it = unpacked.iterator(); it.hasNext(); ) {
                Item item = it.next();

                for (Atlas atlas : atlases) {
                    if (atlas.place(item)) {
                        it.remove();
                    }
                }
            }
        }

        final Map<String,Image.Region> packed = new HashMap<String,Image.Region>();
        for (Atlas atlas : atlases) {
            Node root = atlas.root;
            final SurfaceImage atlasImage = graphics().createSurface(root.width, root.height);
            root.visitItems(new Slot<Node>() {
                @Override public void onEmit (Node node) {
                    // Draw the item to the atlas
                    node.item.draw(atlasImage.surface(), node.x, node.y);

                    // Record its region
                    packed.put(node.item.id, atlasImage.subImage(
                        node.x, node.y, node.width, node.height));
                }
            });
        }
        return packed;
    }

    protected Atlas createAtlas () {
        // TODO(bruno): Be smarter about sizing
        return new Atlas(MAX_SIZE, MAX_SIZE);
    }

    protected TexturePacker addItem (Item item) {
        if (item.width()+PADDING > MAX_SIZE || item.height()+PADDING > MAX_SIZE) {
            throw new RuntimeException("Item is too big to pack [id=" + item.id +
                ", width=" + item.width() + ", height=" + item.height() + "]");
        }
        _items.put(item.id, item);
        return this;
    }

    protected static abstract class Item {
        public final String id;

        public Item (String id) {
            this.id = id;
        }

        public abstract int width ();
        public abstract int height ();
        public abstract void draw (Surface surface, int x, int y);
    }

    protected static class ImageItem extends Item {
        public final Image image;

        public ImageItem (String id, Image image) {
            super(id);
            this.image = image;
        }

        public int width () { return (int)image.width(); }
        public int height () { return (int)image.height(); }
        public void draw (Surface surface, int x, int y) {
            surface.drawImage(image, x, y);
        }
    }

    protected static class RenderedItem extends Item {
        public final int width, height;
        public final Renderer renderer;

        public RenderedItem (String id, int width, int height, Renderer renderer) {
            super(id);
            this.width = width;
            this.height = height;
            this.renderer = renderer;
        }

        public int width () { return width; }
        public int height () { return height; }
        public void draw (Surface surface, int x, int y) {
            renderer.render(surface, new Rectangle(x, y, width, height));
        }
    }

    protected static class Atlas {
        public final Node root;

        public Atlas (int width, int height) {
            root = new Node(0, 0, width, height);
        }

        public boolean place (Item item) {
            Node node = root.search(item.width() + PADDING, item.height() + PADDING);
            if (node == null) return false;
            node.item = item;
            return true;
        }
    }

    protected static class Node {
        /** The bounds of this node (and its children). */
        public final int x, y, width, height;

        /** This node's two children, if any. */
        public Node left, right;

        /** The texture that is placed here, if any. Implies that this is a leaf node. */
        public Item item;

        public Node (int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        /** Find a free node in this tree big enough to fit an area, or null. */
        public Node search (int w, int h) {
            // There's already an item here, terminate
            if (item != null) return null;

            // That'll never fit, terminate
            if (width < w || height < h) return null;

            if (left != null) {
                Node descendent = left.search(w, h);
                if (descendent == null) descendent = right.search(w, h);
                return descendent;

            } else {
                // This node is a perfect size, no need to subdivide
                if (width == w && height == h) return this;

                // Split into two children
                int dw = width-w, dh = height-h;
                if (dw > dh) {
                    left = new Node(x, y, w, height);
                    right = new Node(x + w, y, dw, height);
                } else {
                    left = new Node(x, y, width, h);
                    right = new Node(x, y + h, width, dh);
                }

                return left.search(w, h);
            }
        }

        /** Iterate over all nodes with items in this tree. */
        public void visitItems (Slot<Node> slot) {
            if (item != null) slot.onEmit(this);
            if (left != null) {
                left.visitItems(slot);
                right.visitItems(slot);
            }
        }
    }

    protected static final int PADDING = 1;
    protected static final int MAX_SIZE = 2048;

    protected Map<String,Item> _items = new HashMap<String,Item>();
}
