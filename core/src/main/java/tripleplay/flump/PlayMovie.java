//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import tripleplay.anim.Animation;

/** Plays through an entire movie once. */
public class PlayMovie extends Animation
{
    public PlayMovie (Movie movie) {
        _movie = movie;
    }

    @Override protected void init (float time) {
        super.init(time);
        _movie.setPosition(0);
    }

    @Override protected float apply (float time) {
        float elapsed = time - _start;
        _movie.setPosition(1000*elapsed);
        return _movie.symbol().duration/1000 - elapsed;
    }

    protected Movie _movie;
}
