//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

import tripleplay.util.BitVec;

/**
 * Tracks the state of a single entity. This includes its enabled state, as well as the components
 * which are attached to this entity.
 */
public final class Entity
{
    /** The world to which this entity belongs. */
    public final World world;

    /** This entity's unique id. This id will be valid for as long as the entity remains alive.
     * Once {@link #destroy} is called, this id may be reused by a new entity. */
    public final int id;

    /** Creates an entity in the specified world, initializes it with the supplied set of
     * components and queues it to be added to the world on the next update.
     */
    public Entity (World world, int id) {
        this.world = world;
        this.id = id;
    }

    /** Returns whether this entity has been destroyed. */
    public boolean isDestroyed () {
        return (_flags & DESTROYED) != 0;
    }

    /** Returns whether this entity is currently enabled. */
    public boolean isEnabled () {
        return (_flags & ENABLED) != 0;
    }

    /** Enables or disables this entity. When an entity is disabled, it is removed from all systems
     * in which it is currently an active participant (prior to the next update). When it is
     * re-enabled, it is added back to all systems that are interested in it (prior to the next
     * update).
     */
    public void setEnabled (boolean enabled) {
        checkDestroyed("Cannot modify destroyed entity.");
        boolean isEnabled = (_flags & ENABLED) != 0;
        if (isEnabled && !enabled) {
            _flags &= ~ENABLED;
            world.toRemove.add(this);
        } else if (!isEnabled && enabled) {
            _flags |= ENABLED;
            world.toAdd.add(this);
        }
    }

    /** Returns true if this entity has the component {@code comp}, false otherwise. */
    public boolean has (Component comp) {
        return comps.isSet(comp.id);
    }

    /** Adds the specified component to this entity. This will queue the component up to be added
     * or removed to appropriate systems on the next update.
     *
     * @return this entity for call chaining.
     */
    public Entity add (Component comp) {
        checkDestroyed("Cannot add components to destroyed entity.");
        comp.add(this);
        queueChange();
        return this;
    }

    /** Adds the specified components to this entity. This will queue the component up to be added
     * or removed to appropriate systems on the next update. <em>Note:</em> this method uses varags
     * and thus creates an array every time it is called. If you are striving to eliminate all
     * unnecessary garbage generation, use repeated calls to {@link #add(Component)}, or {@link
     * #add(Component[])} with a pre-allocated array.
     *
     * @return this entity for call chaining.
     */
    public Entity add (Component c1, Component c2, Component... rest) {
        checkDestroyed("Cannot add components to destroyed entity.");
        c1.add(this);
        c2.add(this);
        for (Component cn : rest) cn.add(this);
        queueChange();
        return this;
    }

    /** Adds the supplied components to this entity. This will queue the component up to be added
     * or removed to appropriate systems on the next update. This avoids the garbage generation of
     * the varags {@code add} method, and is slightly more efficient than a sequence of calls to
     * {@link #add(Component)}. The expectation is that you would keep an array around with the
     * components for a particular kind of entity, like so:
     *
     * <pre>{@code
     * Entity createFoo (...) {
     *   Entity foo = create(true).add(FOO_COMPS);
     *   // init foo...
     *   return foo;
     * }
     * private static final Component[] FOO_COMPS = { pos, vel, etc. };
     * }</pre>
     *
     * @return this entity for call chaining.
     */
    public Entity add (Component[] comps) {
        checkDestroyed("Cannot add components to destroyed entity.");
        for (Component comp : comps) comp.add(this);
        queueChange();
        return this;
    }

    /** Removes the specified component from this entity. This will queue the component up to be
     * added or removed to appropriate systems on the next update.
     */
    public Entity remove (Component comp) {
        checkDestroyed("Cannot remove components from destroyed entity.");
        comp.remove(this);
        queueChange();
        return this;
    }

    /** Destroys this entity, causing it to be removed from the world on the next update. */
    public void destroy () {
        if (!isDestroyed()) {
            _flags |= DESTROYED;
            world.toRemove.add(this);
        }
    }

    /** Indicates that this entity has changed, and causes it to be reconsidered for inclusion or
     * exclusion from systems on the next update. This need not be called when adding or removing
     * components, and should only be called if some other external circumstance changes that
     * requires recalculation of which systems are interested in this entity.
     */
    public void didChange () {
        checkDestroyed("Cannot didChange destroyed entity.");
        queueChange();
    }

    protected final void queueChange () {
        // if we're not yet added, we can stop now because we'll be processed when added; if we're
        // not currently enabled, we can stop now and we'll be processed when re-enabled
        if ((_flags & (ADDED|ENABLED)) == 0) return;
        // if we're already queued up as changing, we've got nothing to do either
        if ((_flags & CHANGING) != 0) return;
        // otherwise, mark ourselves as changing and queue on up
        _flags |= CHANGING;
        world.toChange.add(this);
    }

    protected final void checkDestroyed (String error) {
        if ((_flags & DESTROYED) != 0) throw new IllegalStateException(error);
    }

    void noteAdded () { _flags |= ADDED; }
    void clearChanging () { _flags &= ~CHANGING; }
    void reset () { _flags = 0; }

    /** A bit mask indicating which systems are interested in this entity. */
    final BitVec systems = new BitVec(2);

    /** A bit mask indicating which components are possessed by this entity. */
    final BitVec comps = new BitVec(2);

    /** Flags pertaining to this entity's state. */
    protected int _flags;

    protected static final int ENABLED   = 1 << 0;
    protected static final int DESTROYED = 1 << 1;
    protected static final int ADDED     = 1 << 2;
    protected static final int CHANGING  = 1 << 3;
}
