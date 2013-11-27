//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.entity;

import pythagoras.f.Dimension;
import pythagoras.f.Point;
import pythagoras.f.Vector;

/**
 * A component contains the data for a single aspect of an entity. This might be its position in a
 * 2D space, or its animation state, or any other piece of data that evolves as the entity exists
 * in the world.
 *
 * <p>A {@code Component} instance contains the data for <em>all</em> entities that possess the
 * component in question (in a sparse array). This enables a data-driven approach to entity
 * processing where a system can process one or more components for its active entities with a
 * cache-friendly memory access pattern.</p>
 */
public abstract class Component
{
    /** A component implementation for arbitrary objects. */
    public static class Generic<T> extends Component {
        public Generic (World world) { super(world); }

        /** Returns the value of this component for {@code entityId}. */
        public T get (int entityId) {
            Object[] block = _blocks[entityId / BLOCK];
            @SuppressWarnings("unchecked") T value = (T)block[entityId % BLOCK];
            return value;
        }

        /** Updates the value of this component for {@code entityId}. */
        public void set (int entityId, T value) {
            _blocks[entityId / BLOCK][entityId % BLOCK] = value;
        }

        @Override protected void init (int entityId) {
            int blockIdx = entityId / BLOCK;
            if (blockIdx >= _blocks.length) {
                Object[][] blocks = new Object[_blocks.length*2][];
                java.lang.System.arraycopy(_blocks, 0, blocks, 0, _blocks.length);
                _blocks = blocks;
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = new Object[BLOCK];
        }

        @Override protected void clear (int entityId) {
            Object[] block = _blocks[entityId / BLOCK];
            int idx = entityId % BLOCK;
            @SuppressWarnings("unchecked") T prev = (T)block[idx];
            block[idx] = null;
            release(prev);
        }

        /** Releases an instance of this component for an entity that is no longer using it. The
         * default does nothing (assuming the object will be garbage collected), but if the
         * component was set to an object fetched from a pool, this is a convenient place to return
         * it to the pool. */
        protected void release (T value) {}

        protected Object[][] _blocks = new Object[INDEX_BLOCKS][];
    }

    /** A component implementation for a single scalar {@code int}. */
    public static class IScalar extends Component {
        public IScalar (World world) { super(world); }

        /** Returns the value of this component for {@code entityId}. */
        public int get (int entityId) {
            return _blocks[entityId / BLOCK][entityId % BLOCK];
        }

        /** Updates the value of this component for {@code entityId}. */
        public void set (int entityId, int value) {
            _blocks[entityId / BLOCK][entityId % BLOCK] = value;
        }

        /** Adds {@code dv} to the value of this component for {@code entityId}. */
        public void add (int entityId, int dv) {
            _blocks[entityId / BLOCK][entityId % BLOCK] += dv;
        }

        @Override protected void init (int entityId) {
            int blockIdx = entityId / BLOCK;
            if (blockIdx >= _blocks.length) {
                int[][] blocks = new int[_blocks.length*2][];
                java.lang.System.arraycopy(_blocks, 0, blocks, 0, _blocks.length);
                _blocks = blocks;
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = new int[BLOCK];
        }

        protected int[][] _blocks = new int[INDEX_BLOCKS][];
    }

    /** A component implementation for a single scalar {@code float}. */
    public static class FScalar extends Component {
        public FScalar (World world) { super(world); }

        /** Returns the value of this component for {@code entityId}. */
        public float get (int entityId) {
            return _blocks[entityId / BLOCK][entityId % BLOCK];
        }

        /** Updates the value of this component for {@code entityId}. */
        public void set (int entityId, float value) {
            _blocks[entityId / BLOCK][entityId % BLOCK] = value;
        }

        /** Adds {@code dv} to the value of this component for {@code entityId}. */
        public void add (int entityId, float dv) {
            _blocks[entityId / BLOCK][entityId % BLOCK] += dv;
        }

        @Override protected void init (int entityId) {
            int blockIdx = entityId / BLOCK;
            if (blockIdx >= _blocks.length) {
                float[][] blocks = new float[_blocks.length*2][];
                java.lang.System.arraycopy(_blocks, 0, blocks, 0, _blocks.length);
                _blocks = blocks;
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = new float[BLOCK];
        }

        protected float[][] _blocks = new float[INDEX_BLOCKS][];
    }

    /** A component implementation for a pair of {@code float}s. */
    public static class XY extends Component {
        public XY (World world) { super(world); }

        /** Returns the x component of the point for {@code entityId}. */
        public float getX (int entityId) {
            return _blocks[entityId / BLOCK][2*(entityId % BLOCK)];
        }

        /** Returns the y component of the point for {@code entityId}. */
        public float getY (int entityId) {
            return _blocks[entityId / BLOCK][2*(entityId % BLOCK) + 1];
        }

        /** Writes the x/y components of the point for {@code entityId} into {@code into}.
         * @return into for easy method chaining. */
        public Point get (int entityId, Point into) {
            float[] block = _blocks[entityId / BLOCK];
            int idx = 2*(entityId % BLOCK);
            into.x = block[idx];
            into.y = block[idx + 1];
            return into;
        }

        /** Writes the x/y components of the point for {@code entityId} into {@code into}.
         * @return into for easy method chaining. */
        public Vector get (int entityId, Vector into) {
            float[] block = _blocks[entityId / BLOCK];
            int idx = 2*(entityId % BLOCK);
            into.x = block[idx];
            into.y = block[idx + 1];
            return into;
        }

        /** Writes the x/y components of the point for {@code entityId} into {@code into}.
         * @return into for easy method chaining. */
        public Dimension get (int entityId, Dimension into) {
            float[] block = _blocks[entityId / BLOCK];
            int idx = 2*(entityId % BLOCK);
            into.width = block[idx];
            into.height = block[idx + 1];
            return into;
        }

        /** Updates the x component of the point for {@code entityId}. */
        public void setX (int entityId, float x) {
            _blocks[entityId / BLOCK][2*(entityId % BLOCK)] = x;
        }

        /** Updates the y component of the point for {@code entityId}. */
        public void setY (int entityId, float y) {
            _blocks[entityId / BLOCK][2*(entityId % BLOCK) + 1] = y;
        }

        /** Updates the x/y components of the point for {@code entityId}. */
        public void set (int entityId, pythagoras.f.XY value) {
            set(entityId, value.x(), value.y());
        }

        /** Updates the x/y components of the point for {@code entityId}. */
        public void set (int entityId, float x, float y) {
            float[] block = _blocks[entityId / BLOCK];
            int idx = 2*(entityId % BLOCK);
            block[idx] = x;
            block[idx+1] = y;
        }

        /** Copies the value of {@code other} for {@code entityId} to this component. */
        public void set (int entityId, Component.XY other) {
            int blockIdx = entityId / BLOCK, idx = 2*(entityId % BLOCK);
            float[] oblock = other._blocks[blockIdx], block = _blocks[blockIdx];
            block[idx] = oblock[idx];
            block[idx+1] = oblock[idx+1];
        }

        /** Adds {@code dx} and {@code dy} to the x and y components for {@code entityId}. */
        public void add (int entityId, float dx, float dy) {
            float[] block = _blocks[entityId / BLOCK];
            int idx = 2*(entityId % BLOCK);
            block[idx] += dx;
            block[idx+1] += dy;
        }

        @Override protected void init (int entityId) {
            int blockIdx = entityId / BLOCK;
            if (blockIdx >= _blocks.length) {
                float[][] blocks = new float[_blocks.length*2][];
                java.lang.System.arraycopy(_blocks, 0, blocks, 0, _blocks.length);
                _blocks = blocks;
            }
            if (_blocks[blockIdx] == null) _blocks[blockIdx] = new float[2*BLOCK];
        }

        protected float[][] _blocks = new float[INDEX_BLOCKS][];
    }

    /** The world in which this component exists. */
    public final World world;

    protected Component (World world) {
        this.world = world;
        mask = (1L << world.register(this));
    }

    /** Ensures that space is allocated for the component at {@code index}. */
    protected abstract void init (int index);

    /** Clears the value of the component at {@code index}. */
    protected void clear (int index) {} // noop by default

    void add (Entity entity) {
        entity.componentsMask |= mask;
        init(entity.id);
    }

    void remove (Entity entity) {
        entity.componentsMask &= ~mask;
        clear(entity.id);
    }

    /** This component's unique bit mask. */
    final long mask;

    /** The number of components in a single block. */
    protected static final int BLOCK = 256;

    /** The number of index blocks to allocate by default. */
    protected static final int INDEX_BLOCKS = 32;
}
