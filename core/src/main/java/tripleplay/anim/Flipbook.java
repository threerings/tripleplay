//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import tripleplay.util.Frames;

/**
 * Encapsulates a set of frames and timings for animating those frames.
 */
public class Flipbook
{
    /** The frames to be animated. */
    public final Frames frames;

    /** The index of the frames to be shown. The animation will display the frame at the 0th
     * position in this array, then the frame at the 1st position, etc. */
    public final int[] frameIndexes;

    /** The timestamp at which to stop playing each frame and move to the next. */
    public final float[] frameEnds;

    /**
     * Creates a flipbook with the specified frames. The frames will be played in order, each for
     * the specified duration.
     *
     * @param secsPerFrame the number of seconds to display each frame.
     */
    public Flipbook (Frames frames, float secsPerFrame) {
        this(frames, uniformTimes(frames, secsPerFrame));
    }

    /**
     * Creates a flipbook with the specified frames. The frames will be played in order, each for
     * its associated duration in {@code frameEnds}.
     *
     * @param frameEnds the time (in seconds since animation start) at which each frame ends. The
     * values must be monotonically increasing (e.g. {@code (1.5f, 2f, 2.5f, 4f)}.
     */
    public Flipbook (Frames frames, float[] frameEnds) {
        this(frames, uniformOrder(frames), frameEnds);
    }

    /**
     * Creates a flipbook with the specified frames.
     *
     * @param frameIndexes an array of frame indexes to be played in the specified order.
     * @param frameEnds the time (in seconds since animation start) at which the frame specified
     * at the corresponding position in {@code frameIndex} ends. The values must be monotonically
     * increasing (e.g. {@code (1.5f, 2f, 2.5f, 4f)}.
     */
    public Flipbook (Frames frames, int[] frameIndexes, float[] frameEnds) {
        this.frames = frames;
        this.frameIndexes = frameIndexes;
        this.frameEnds = frameEnds;
    }

    protected static float[] uniformTimes (Frames frames, float secsPerFrame) {
        float[] times = new float[frames.count()];
        times[0] = secsPerFrame;
        for (int ii = 1, ll = times.length; ii < ll; ii++) times[ii] = times[ii-1] + secsPerFrame;
        return times;
    }

    protected static int[] uniformOrder (Frames frames) {
        int[] indexes = new int[frames.count()];
        for (int ii = 1, ll = indexes.length; ii < ll; ii++) indexes[ii] = ii;
        return indexes;
    }
}
