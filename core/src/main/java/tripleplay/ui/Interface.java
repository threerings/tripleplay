//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import java.util.ArrayList;
import java.util.List;

import playn.core.Clock;
import playn.core.Platform;
import playn.scene.GroupLayer;

import react.Closeable;
import react.Signal;
import react.Slot;

import tripleplay.anim.Animator;

/**
 * The main class that integrates the Triple Play UI with a PlayN game. This class is mainly
 * necessary to automatically validate hierarchies of {@code Element}s during each paint.
 * Create an interface instance, create {@link Root} groups via the interface and add the
 * {@link Root#layer}s into your scene graph wherever you desire.
 */
public class Interface implements Closeable
{
    /** The platform in which this interface is operating. */
    public final Platform plat;

    /** A signal emitted just before we render a frame. */
    public final Signal<Clock> frame;

    /** An animator that can be used to animate things in this interface. */
    public final Animator anim = new Animator();

    /** Creates an interface for {@code plat}. The interface will be connected to {@code frame} to
      * drive any per-frame animations and activity. Either provide a frame signal whose lifetime
      * is the same as the interface (for example {@code Screen.paint}), or call {@link #close}
      * when this interface should be disconnected from the frame signal. */
    public Interface (Platform plat, Signal<Clock> frame) {
        this.plat = plat;
        this.frame = frame;
        _onFrame = Closeable.join(
            frame.connect(new Slot<Clock>() { public void onEmit (Clock clock) { paint(clock); }}),
            frame.connect(anim.onPaint));
    }

    @Override public void close () {
        _onFrame.close();
    }

    /** Returns an iterable over the current roots. Don't delete from this iterable! */
    public Iterable<Root> roots () {
        return _roots;
    }

    /**
     * Creates a root element with the specified layout and stylesheet.
     */
    public Root createRoot (Layout layout, Stylesheet sheet) {
        return addRoot(new Root(this, layout, sheet));
    }

    /**
     * Creates a root element with the specified layout and stylesheet and adds its layer to the
     * specified parent.
     */
    public Root createRoot (Layout layout, Stylesheet sheet, GroupLayer parent) {
        Root root = createRoot(layout, sheet);
        parent.add(root.layer);
        return root;
    }

    /**
     * Adds a root to this interface. The root must have been created with this interface and not
     * be added to any other interfaces. Generally you should use {@link #createRoot}, but this
     * method is exposed for callers with special needs.
     */
    public <R extends Root> R addRoot (R root) {
        _roots.add(root);
        return root;
    }

    /**
     * Removes the supplied root element from this interface, iff it's currently added. If the
     * root's layer has a parent, the layer will be removed from the parent as well. This leaves
     * the Root's layer in existence, so it may be used again. If you're done with the Root and all
     * of the elements inside of it, call {@link #disposeRoot} to free its resources.
     *
     * @return true if the root was removed, false if it was not currently added.
     */
    public boolean removeRoot (Root root) {
        if (!_roots.remove(root)) return false;
        root.wasRemoved();
        if (root.layer.parent() != null) root.layer.parent().remove(root.layer);
        return true;
    }

    /**
     * Removes the supplied root element from this interface and disposes its layer, iff it's
     * currently added. Disposing the layer disposes the layers of all elements contained in the
     * root as well. Use this method if you're done with the Root. If you'd like to reuse it, call
     * {@link #removeRoot} instead.
     *
     * @return true if the root was removed and disposed, false if it was not currently added.
     */
    public boolean disposeRoot (Root root) {
        if (!_roots.remove(root)) return false;
        root.set(Element.Flag.WILL_DISPOSE, true);
        root.wasRemoved();
        root.layer.close();
        return true;
    }

    /**
     * Removes and disposes all roots in this interface.
     */
    public void disposeRoots () {
        while (!_roots.isEmpty()) disposeRoot(_roots.get(0));
    }

    protected void paint (Clock clock) {
        // ensure that our roots are validated
        for (int ii = 0, ll = _roots.size(); ii < ll; ii++) _roots.get(ii).validate();
    }

    protected final Closeable _onFrame;
    protected final List<Root> _roots = new ArrayList<Root>();
}
