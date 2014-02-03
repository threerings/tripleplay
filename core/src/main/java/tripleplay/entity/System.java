//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

import playn.core.util.Clock;

/**
 * Handles a single concern in an entity-based game. That might be processing collisions, or
 * updating entity's logical positions, or regenerating health, etc. A system operates on all
 * entities which meet its criteria on a given tick. See {@link #isInterested} for an explanation
 * of how to choose which entities on which to operate.
 */
public abstract class System
{
    /** Provides a way to iterate over this system's active entities. */
    public interface Entities {
        /** Returns the size of the active entities set. */
        int size ();
        /** Returns the entity at {@code index}. Entities are arbitrarily ordered. */
        int get (int index);
    }

    /** The world of which this system is a part. */
    public final World world;

    /** Enables or disables this system. When a system is disabled, it will not be processed every
     * frame. However, it will still be checked for entity interest and be notified when entities
     * are added to and removed from its active set.
     */
    public void setEnabled (boolean enabled) {
        _enabled = enabled;
    }

    /** Creates a new system and registers it with {@code world}.
     * @param priorty this system's priority with respect to other systems. Systems with higher
     * priority will be notified of entity addition/removal and will be processed before systems
     * with lower priority. Systems with the same priority will be processed in unspecified order.
     * When order matters, use priority, don't rely on the order you register systems with the
     * world.
     */
    protected System (World world, int priority) {
        this.world = world;
        this.priority = priority;
        _id = world.register(this);
    }

    /** Called when an entity is added to our world (or an already added entity is changed) which
     * matches this system's criteria. This entity will subsequently be processed by this system
     * until it is removed from the world or no longer matches our criteria.
     */
    protected void wasAdded (Entity entity) {
    }

    /**
     * Called when an entity that was previously in our active set is removed from the world or is
     * changed such that it no longer matches our criteria.
     *
     * @param index the index in the {@link #_active} bag from which the entity was removed. This
     * makes it more efficient for a derived class to keep a parallel bag containing the entities
     * themselves, if needed.
     */
    protected void wasRemoved (Entity entity, int index) {
    }

    /**
     * Processes this system's active entities. This is where each entity's simulation state would
     * be updated. This is not called if the system is disabled.
     */
    protected void update (int delta, Entities entities) {
    }

    /**
     * Paints this system's active entities. This should only be used to perform interpolation on
     * values computed during {@link #update(int,Entities)}. Entities <em>must not</em> be added,
     * changed or removed during this call. This is not called if the system is disabled.
     */
    protected void paint (Clock clock, Entities entities) {
    }

    /**
     * Indicates whether this system is "interested" in this entity. A system will process all
     * entries in which it is interested, every tick. As entities are added to the world or changed
     * while added to the world, all systems will be checked for interest in the entity. Generally
     * a system will be interested in entities that have a particular combination of components,
     * but any criteria are allowed as long as any change to the criteria (on active entities) are
     * accompanied by a call to {@link Entity#didChange} so that all systems may be rechecked for
     * interest in the entity. Note that {@code didChange} is called automatically when components
     * are added to or removed from an entity.
     */
    protected abstract boolean isInterested (Entity entity);

    void entityAdded (Entity entity) {
        if (isInterested(entity)) addEntity(entity);
    }

    void entityChanged (Entity entity) {
        boolean wasAdded = entity.systems.isSet(_id);
        boolean haveInterest = isInterested(entity);
        if (haveInterest && !wasAdded) addEntity(entity);
        else if (!haveInterest && wasAdded) removeEntity(entity);
    }

    void entityRemoved (Entity entity) {
        if (entity.systems.isSet(_id)) removeEntity(entity);
    }

    void update (int delta) {
        if (!_enabled) return;
        update(delta, _active);
    }

    void paint (Clock clock) {
        if (!_enabled) return;
        paint(clock, _active);
    }

    private void addEntity (Entity entity) {
        _active.add(entity.id);
        entity.systems.set(_id);
        wasAdded(entity);
    }

    private void removeEntity (Entity entity) {
        // TODO: this is O(N), would be nice if it was O(log N) or O(1)
        int idx = _active.remove(entity.id);
        entity.systems.clear(_id);
        wasRemoved(entity, idx);
    }

    /** This system's priority with respect to other systems. See {@link #System}. */
    final int priority;

    /** Our active entities. */
    protected final IntBag _active = new IntBag();

    /** This system's unique id (used in bit masks). */
    private final int _id;

    /** Whether or not this system is enabled. */
    private boolean _enabled = true;
}
