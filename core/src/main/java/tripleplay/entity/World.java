//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

import java.util.ArrayList;
import java.util.Iterator;

import react.Closeable;
import react.Signal;
import react.Slot;

import playn.core.Clock;

import tripleplay.util.Bag;
import tripleplay.util.BitVec;

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

    /** Connects this world to the supplied {@code update} and {@code paint} signals.
      * @return an object that can be used to disconnect both connections.
      */
    public Closeable connect (Signal<Clock> update, Signal<Clock> paint) {
        return Closeable.Util.join(
            update.connect(new Slot<Clock>() { public void onEmit (Clock clk) { update(clk); }}),
            paint.connect(new Slot<Clock>() { public void onEmit (Clock clk) { paint(clk); }}));
    }

    /** Creates and returns an entity. The entity may actually be obtained from a pool of free
      * entities to avoid unnecessary garbage generation.
      *
      * @param enabled whether the entity should be enabled by default. If it is enabled, it will
      * automatically be queued for addition to the world. If it is not enabled, it will remain
      * dormant until {@link Entity#setEnabled} is used to enable it.
      */
    public Entity create (boolean enabled) {
        Entity e;
        if (_ids.isEmpty()) {
            e = create(genEntityId());
        } else {
            e = _entities[_ids.removeLast()];
            e.reset();
        }
        if (enabled) e.setEnabled(true);
        return e;
    }

    /** Creates an entity with the specified id. NOTE: this is not how you normally create
      * entities, this is only used in situations like restoring a collection of entities from
      * persistent state.
      */

    /** Creates an entity with the specified id and component bitvec. This is only used when
      * restoring entities from persistent storage. Use {@link #create} to create new entities.
      */
    public Entity restore (int id, BitVec components) {
        Entity ent = create(id);
        // init the components directly to avoid extra checking done by Entity.add(Component)
        ent.comps.set(components);
        for (int ii = 0, ll = _comps.size(); ii < ll; ii++) {
            if (components.isSet(ii)) _comps.get(ii).add(ent);
        }
        return ent;
    }

    /** Returns the entity with the specified id. Note: this method is optimized for speed, which
     * means that passing an invalid/unused entity id to this method may return a destroyed entity
     * or it may throw an exception.
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
                    Entity e = _entities[idx];
                    if (e != null && !e.isDisposed()) return idx;
                    idx++;
                }
                return -1;
            }
            protected int _nextIdx = findNext(0);
        };
    }

    /** Updates all of the {@link System}s in this world. The systems will likely in turn update
      * the components of registered {@link Entity}s. */
    public void update (Clock clock) {
        // init any to-be-initted systems (before we add to-be-added entities)
        for (int ii = _toInit.size()-1; ii >= 0; ii--) {
            System sys = _toInit.removeLast();
            for (Entity ent : _entities) {
                // skip non-existent or disabled entities
                if (ent == null || !ent.isEnabled()) continue;
                // if the entity is already added tell the new system about it, otherwise it is in
                // the toAdd list and we'll tell all systems about it in the next block
                if (ent.isAdded()) sys.entityAdded(ent);
            }
        }

        // process any pending entity additions
        for (int ii = toAdd.size()-1; ii >= 0; ii--) {
            Entity entity = toAdd.removeLast();
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
            entityRemoved.emit(entity);
            // if the entity is destroyed, remove its components and return it to the pool
            if (entity.isDisposed()) {
                for (int cc = 0, ll = _comps.size(); cc < ll; cc++) {
                    if (entity.comps.isSet(cc)) _comps.get(cc).remove(entity);
                }
                _ids.add(entity.id);
            }
        }

        // and finally update all of our systems
        for (int ii = 0, ll = _systems.size(); ii < ll; ii++) _systems.get(ii).update(clock);
    }

    /** Paints all of the {@link System}s in this world. */
    public void paint (Clock clock) {
        for (int ii = 0, ll = _systems.size(); ii < ll; ii++) _systems.get(ii).paint(clock);
    }

    /** Registers {@code system} with this world.
     * @return a unique index assigned to the system for use in bitmasks.
     */
    int register (System system) {
        int idx = 0; // insert the system based on its priority
        for (int ii = _systems.size()-1; ii >= 0; ii--) {
            if (_systems.get(ii).priority >= system.priority) {
                idx = ii+1;
                break;
            }
        }
        _systems.add(idx, system);
        _toInit.add(system); // tell it about existing entities on the next update
        return _systems.size()-1;
    }

    /** Registers {@code component} with this world.
     * @return a unique index assigned to the component for use in bitmasks.
     */
    int register (Component component) {
        _comps.add(component);
        return _comps.size()-1;
    }

    // Entity will add itself to the appropriate set as needed
    final Bag<Entity> toAdd = Bag.create();
    final Bag<Entity> toChange = Bag.create();
    final Bag<Entity> toRemove = Bag.create();

    protected int genEntityId () {
        return _nextEntityId++;
    }

    protected Entity create (int id) {
        if (_entities.length <= id) {
            Entity[] entities = new Entity[_entities.length*2];
            java.lang.System.arraycopy(_entities, 0, entities, 0, _entities.length);
            _entities = entities;
        }
        assert _entities[id] == null : "Entity already exists with id " + id;
        return _entities[id] = new Entity(this, id);
    }

    protected BitVec components (Entity ent) {
        return ent.comps;
    }

    // Systems that need to be initted on the next update
    protected final Bag<System> _toInit = Bag.create();

    protected final ArrayList<System> _systems = new ArrayList<System>();
    protected final ArrayList<Component> _comps = new ArrayList<Component>();

    protected final IntBag _ids = new IntBag();
    protected Entity[] _entities = new Entity[64];
    protected int _nextEntityId = 1;
}
