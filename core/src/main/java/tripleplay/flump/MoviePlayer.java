//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import playn.core.GroupLayer;
import playn.core.Layer;
import static playn.core.PlayN.*;

import tripleplay.anim.Animation;

/**
 * A convenient controller to play though multiple different movies. Designed for characters and
 * objects that have a separate Flump symbol for each of their animations, and need to switch
 * between them.
 */
public class MoviePlayer
{
    public MoviePlayer (Library lib) {
        this(lib, graphics().createGroupLayer());
    }

    public MoviePlayer (Library lib, GroupLayer root) {
        _lib = lib;
        _root = root;
    }

    /** The layer the movies are placed on. */
    public Layer layer () {
        return _root;
    }

    /** The currently playing movie, if any. */
    public Movie movie () {
        return _currentMovie;
    }

    /** Whether the current movie is being looped. */
    public boolean looping () {
        return _oneshotMovie == null && _loopingMovie != null;
    }

    // TODO(bruno): public boolean setCache (CacheBuilder cache)

    /**
     * Shows a movie that plays once. When it completes, the last looping movie is returned to. It
     * is an error to call this without starting a loop() first.
     * @param name The symbol name of the movie to play.
     * @param restart If this movie is already being played, whether it will restart it from the
     *   beginning. Defaults to true.
     * @return This instance, for chaining.
     */
    public MoviePlayer play (String name, boolean restart) {
        if (_loopingMovie == null) {
            throw new IllegalStateException(
                "A loop must be started before the first call to play()");
        }
        if (restart || _oneshotMovie == null || !_oneshotMovie.symbol().name().equals(name)) {
            _oneshotMovie = setCurrent(createMovie(name));
        }
        return this;
    }

    public MoviePlayer play (String name) {
        return play(name, true);
    }

    /**
     * Shows a movie that loops forever.
     * @param name The symbol name of the movie to loop.
     * @param restart If this movie is already being looped, whether it will restart it from the
     *   beginning. Defaults to true.
     * @return This instance, for chaining.
     */
    public MoviePlayer loop (String name, boolean restart) {
        if (restart || _loopingMovie == null || !_loopingMovie.symbol().name().equals(name)) {
            _oneshotMovie = null;
            _loopingMovie = setCurrent(createMovie(name));
        }
        return this;
    }

    public MoviePlayer loop (String name) {
        return loop(name, true);
    }

    /** Creates an {@link Animation} that plays a one-shot movie. */
    public Animation animate (String name) {
        return new PlayAnimation(name);
    }

    public void update (float dt) {
        // If this update would end the oneshot movie, replace it with the looping movie
        if (_oneshotMovie != null && _oneshotMovie.position() + dt*_oneshotMovie.speed() >
                _oneshotMovie.symbol().duration) {
            _oneshotMovie = null;
            setCurrent(_loopingMovie);
        }
        _currentMovie.update(dt);
    }

    /** Override this to dress up avatars or any other custom initialization. */
    protected Movie createMovie (String name) {
        return _lib.createMovie(name);
    }

    /** Replace the currently active movie. */
    protected Movie setCurrent (Movie movie) {
        if (_currentMovie != null) {
            _root.remove(_currentMovie.layer());
        }
        _root.add(movie.layer());
        return _currentMovie = movie;
    }

    public void destroy () {
        if (_currentMovie != null) {
            _currentMovie.layer().destroy();
        }
        _currentMovie = null;
        _loopingMovie = null;
        _oneshotMovie = null;
    }

    protected class PlayAnimation extends Animation {
        public PlayAnimation (String name) {
            _name = name;
        }

        @Override protected void init (float time) {
            play(_name);
            _playing = _currentMovie;
        }

        @Override protected float apply (float time) {
            // Wait until the original movie is no longer playing
            return (_currentMovie == _playing) ? 1 : 0;
        }

        protected String _name;
        protected Movie _playing;
    }

    protected Library _lib;
    protected GroupLayer _root;

    protected Movie _currentMovie;
    protected Movie _oneshotMovie;
    protected Movie _loopingMovie;
}

