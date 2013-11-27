//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

import java.util.ArrayList;
import java.util.Iterator;

import playn.core.util.Clock;

import react.Signal;

import tripleplay.util.Bag;

/**
 * A collection of entities and systems. A world is completely self-contained, so it would be
 * possible to have multiple separate worlds running simultaneously, though this would be uncommon.
 */
public class World
{
    /** A signal emitted when an entity is added to this world. */
    public final Signal<Entity> entityAdded = Signal.create();

    /** A signal emitted when an entity in this world has changed (usually this means components
     * have been added to or removed from the entity). */
    public final Signal<Entity> entityChanged = Signal.create();

    /** A signal emitted when an entity in this world which was disabled becomes enabled. */
    public final Signal<Entity> entityEnabled = Signal.create();

    /** A signal emitted when an entity in this world which was enabled becomes disabled. */
    public final Signal<Entity> entityDisabled = Signal.create();

    /** A signal emitted when an entity is removed from the world. This happens when an entity is
     * disabled, as well as when it is destroyed. */
    public final Signal<Entity> entityRemoved = Signal.create();

    /** Returns the entity with the specified id. Note: this method is optimized for speed, which
     * means that passing an invalid/unused entity id to this method may return null or it may
     * throw an exception.
     */
    public Entity entity (int id) {
        return _entities[id];
    }

    /** Returns an iterator over all entities in the world. {@link Iterator#remove} is not
     * implemented for this iterator.
     */
    public Iterator<Entity> entities () {
        return new Iterator<Entity>() {
            @Override public void remove () { throw new UnsupportedOperationException(); }
            @Override public boolean hasNext () { return _nextIdx >= 0; }
            @Override public Entity next () {
                Entity next = _entities[_nextIdx];
                _nextIdx = findNext(_nextIdx+1);
                return next;
            }
            protected int findNext (int idx) {
                while (idx < _entities.length) {
                    if (_entities[idx] != null) return idx;
                    idx++;
                }
                return -1;
            }
            protected int _nextIdx = findNext(0);
        };
    }

    /** Updates all of the {@link System}s in this world. The systems will likely in turn update
     * the components of registered {@link Entity}s.
     */
    public void update (int delta) {
        // process any pending entity additions
        for (int ii = toAdd.size()-1; ii >= 0; ii--) {
            Entity entity = toAdd.removeLast();
            // add the entity to our global entity array (expanding as needed)
            if (_entities.length <= entity.id) {
                Entity[] entities = new Entity[_entities.length*2];
                java.lang.System.arraycopy(_entities, 0, entities, 0, _entities.length);
                _entities = entities;
            }
            _entities[entity.id] = entity;
            // we note that the entity is added before passing it through the systems, both so that
            // they see an added entity and so that if any of those systems then change the
            // entity's components, that triggers the entity to be queued up as changed again;
            // otherwise a system half-way through the list could change things and the systems in
            // the first half of the list would no longer be aware of the entity's real state
            entity.noteAdded();
            for (int ss = 0, ll = _systems.size(); ss < ll; ss++) {
                _systems.get(ss).entityAdded(entity);
            }
            entityAdded.emit(entity);
        }
        // process any pending entity changes
        for (int ii = toChange.size()-1; ii >= 0; ii--) {
            Entity entity = toChange.removeLast();
            // we clear the entity's changing flag before passing it through the systems, so that
            // if any of those systems then change the entity's components yet further, that
            // triggers the entity to be queued up as changed again; otherwise a system half-way
            // through the list could change things and the systems in the first half of the list
            // would no longer be aware of the entity's real state
            entity.clearChanging();
            for (int ss = 0, ll = _systems.size(); ss < ll; ss++) {
                _systems.get(ss).entityChanged(entity);
            }
            entityChanged.emit(entity);
        }
        // process any pending entity removals
        for (int ii = toRemove.size()-1; ii >= 0; ii--) {
            Entity entity = toRemove.removeLast();
            for (int ss = 0, ll = _systems.size(); ss < ll; ss++) {
                _systems.get(ss).entityRemoved(entity);
            }
            _entities[entity.id] = null;
            entityRemoved.emit(entity);
            if (entity.isDestroyed()) _ids.add(entity.id);
            entity.noteRemoved();
        }
        // and finally update all of our systems
        for (int ii = 0, ll = _systems.size(); ii < ll; ii++) {
            _systems.get(ii).update(delta);
        }
    }

    /** Paints all of the {@link System}s in this world. */
    public void paint (Clock clock) {
        for (int ii = 0, ll = _systems.size(); ii < ll; ii++) {
            _systems.get(ii).paint(clock);
        }
    }

    /** Returns the next unused entity id. */
    int claimId () {
        return _ids.isEmpty() ? _nextEntityId++ : _ids.removeLast();
    }

    /** Registers {@code system} with this world.
     * @return a unique index assigned to the system for use in bitmasks.
     */
    int register (System system) {
        if (_systems.size() == MAX_SYSTEMS) throw new IllegalStateException(
            "Only " + MAX_SYSTEMS + " systems can be used in a world.");
        int idx = 0; // insert the system based on its priority
        for (int ii = _systems.size()-1; ii >= 0; ii--) {
            if (_systems.get(ii).priority >= system.priority) {
                idx = ii+1;
                break;
            }
        }
        _systems.add(idx, system);
        return _systems.size()-1;
    }

    /** Registers {@code component} with this world.
     * @return a unique index assigned to the component for use in bitmasks.
     */
    int register (Component component) {
        if (_comps.size() == MAX_COMPS) throw new IllegalStateException(
            "Only " + MAX_COMPS + " components can be used in a world.");
        _comps.add(component);
        return _comps.size()-1;
    }

    // Entity will add itself to the appropriate set as needed
    final Bag<Entity> toAdd = Bag.create();
    final Bag<Entity> toChange = Bag.create();
    final Bag<Entity> toRemove = Bag.create();

    protected final ArrayList<System> _systems = new ArrayList<System>();
    protected final ArrayList<Component> _comps = new ArrayList<Component>();

    protected final IntBag _ids = new IntBag();
    protected Entity[] _entities = new Entity[64];
    protected int _nextEntityId = 1;

    // currently limited to 64 system types and 64 component types in a world due to using a long
    // as a bitmask; when GWT supports BitSet we can relax this limitation
    protected static final int MAX_SYSTEMS = 64, MAX_COMPS = 64;
}
