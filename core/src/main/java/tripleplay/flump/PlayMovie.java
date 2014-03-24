//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import tripleplay.anim.Animation;

/** Plays a movie until it ends. */
public class PlayMovie extends Animation
{
    public PlayMovie (Movie movie) {
        _movie = movie;
    }

    @Override protected void init (float time) {
        super.init(time);
        _lastTime = time;
        _movie.setPosition(0);
    }

    @Override protected float apply (float time) {
        float dt = time - _lastTime;
        _lastTime = time;

        float remaining = _movie.symbol().duration - _movie.position() - dt*_movie.speed();
        if (remaining < 0) {
            dt = (_movie.symbol().duration-_movie.position()) / _movie.speed();
        }
        _movie.paint(dt);
        return remaining;
    }

    @Override protected void makeComplete () {
        _movie.paint((_movie.symbol().duration-_movie.position()) / _movie.speed());
    }

    protected Movie _movie;
    protected float _lastTime;
}
