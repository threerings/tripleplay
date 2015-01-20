//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import pythagoras.f.Rectangle;

import playn.core.Graphics;
import playn.core.QuadBatch;
import playn.core.Surface;
import playn.core.Texture;
import playn.core.TextureSurface;
import playn.core.Tile;

import react.Slot;

/**
 * A runtime texture packer.
 */
public class TexturePacker
{
    public interface Renderer {
        void render (Surface surface, float x, float y, float width, float height);
    }

    /** Add an image to the packer. */
    public TexturePacker add (String id, Tile tile) {
        return addItem(new TileItem(id, tile));
    }

    /** Add a lazily rendered region to the packer.
      * The renderer will be used to draw the region each time pack() is called. */
    public TexturePacker add (String id, float width, float height, Renderer renderer) {
        return addItem(new RenderedItem(id, width, height, renderer));
    }

    /**
     * Pack all images into as few atlases as possible.
     * @return A map containing the new images, keyed by the id they were added with.
     */
    public Map<String,Tile> pack (Graphics gfx, QuadBatch batch) {
        List<Item> unpacked = new ArrayList<Item>(_items.values());
        // TODO(bruno): Experiment with different heuristics. Brute force calculate using multiple
        // different heuristics and use the best one?
        Collections.sort(unpacked, new Comparator<Item>() {
            public int compare (Item o1, Item o2) {
                // Sort by perimeter (instead of area). It can be harder to fit long skinny
                // textures after the large square ones
                return (int)(o2.width+o2.height) - (int)(o1.width+o1.height);
            }
        });

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

        final Map<String,Tile> packed = new HashMap<String,Tile>();
        for (Atlas atlas : atlases) {
            Node root = atlas.root;
            final TextureSurface atlasTex = new TextureSurface(gfx, batch, root.width, root.height);
            atlasTex.begin();
            root.visitItems(new Slot<Node>() { @Override public void onEmit (Node n) {
                // Draw the item to the atlas
                n.item.draw(atlasTex, n.x, n.y);
                // Record its region
                packed.put(n.item.id, atlasTex.texture.tile(n.x, n.y, n.width, n.height));
            }});
            atlasTex.end();
        }
        return packed;
    }

    protected Atlas createAtlas () {
        // TODO(bruno): Be smarter about sizing
        return new Atlas(MAX_SIZE, MAX_SIZE);
    }

    protected TexturePacker addItem (Item item) {
        if (item.width+PADDING > MAX_SIZE || item.height+PADDING > MAX_SIZE) {
            throw new RuntimeException("Item is too big to pack " + item);
        }
        _items.put(item.id, item);
        return this;
    }

    protected static abstract class Item {
        public final String id;
        public final float width, height;

        public Item (String id, float width, float height) {
            this.id = id;
            this.width = width;
            this.height = height;
        }

        public abstract void draw (Surface surface, float x, float y);

        @Override public String toString () {
            return "[id=" + id + ", size=" + width + "x" + height + "]";
        }
    }

    protected static class TileItem extends Item {
        public final Tile tile;

        public TileItem (String id, Tile tile) {
            super(id, tile.width(), tile.height());
            this.tile = tile;
        }

        @Override public void draw (Surface surface, float x, float y) {
            surface.draw(tile, x, y);
        }
    }

    protected static class RenderedItem extends Item {
        public final Renderer renderer;

        public RenderedItem (String id, float width, float height, Renderer renderer) {
            super(id, width, height);
            this.renderer = renderer;
        }

        @Override public void draw (Surface surface, float x, float y) {
            renderer.render(surface, x, y, width, height);
        }
    }

    protected static class Atlas {
        public final Node root;

        public Atlas (int width, int height) {
            root = new Node(0, 0, width, height);
        }

        public boolean place (Item item) {
            Node node = root.search(item.width + PADDING, item.height + PADDING);
            if (node == null) return false;
            node.item = item;
            return true;
        }
    }

    protected static class Node {
        /** The bounds of this node (and its children). */
        public final float x, y, width, height;

        /** This node's two children, if any. */
        public Node left, right;

        /** The texture that is placed here, if any. Implies that this is a leaf node. */
        public Item item;

        public Node (float x, float y, float width, float height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }

        /** Find a free node in this tree big enough to fit an area, or null. */
        public Node search (float w, float h) {
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
                float dw = width-w, dh = height-h;
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
