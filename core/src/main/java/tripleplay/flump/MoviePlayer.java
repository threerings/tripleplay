//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import react.Value;

import playn.core.GroupLayer;
import playn.core.Layer;
import playn.core.util.Clock;
import static playn.core.PlayN.*;

import tripleplay.anim.Animation;
import tripleplay.util.Destroyable;
import tripleplay.util.Paintable;

/**
 * A convenient controller to play though multiple different movies. Designed for characters and
 * objects that have a separate Flump symbol for each of their animations, and need to switch
 * between them.
 */
public class MoviePlayer
    implements Destroyable, Paintable
{
    /** The currently playing movie, if any. */
    public final Value<Movie> movie = Value.create(null);
    public Movie movie () { return movie.get(); }

    public MoviePlayer (Library lib) {
        this(lib, graphics().createGroupLayer());
    }

    public MoviePlayer (Library lib, GroupLayer root) {
        _lib = lib;
        _root = root;
    }

    /** Sets the library all further movies we load will come from. */
    public void setLibrary (Library lib)
    {
        _lib = lib;
    }

    /** The layer the movies are placed on. */
    public Layer layer () {
        return _root;
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

    public void paint (Clock clock) {
        // If this update would end the oneshot movie, replace it with the looping movie
        if (_oneshotMovie != null && _oneshotMovie.position() + clock.dt()*_oneshotMovie.speed() >
                _oneshotMovie.symbol().duration) {
            _oneshotMovie = null;
            setCurrent(_loopingMovie);
        }
        movie().paint(clock);
    }

    /** Override this to dress up avatars or any other custom initialization. */
    protected Movie createMovie (String name) {
        return _lib.createMovie(name);
    }

    /** Replace the currently active movie. */
    protected Movie setCurrent (Movie current) {
        if (movie() != null) {
            _root.remove(movie().layer());
        }
        _root.add(current.layer());
        movie.update(current);
        return current;
    }

    @Override public void destroy () {
        if (movie() != null) {
            movie().layer().destroy();
        }
        _loopingMovie = null;
        _oneshotMovie = null;
        movie.update(null);
    }

    protected class PlayAnimation extends Animation {
        public PlayAnimation (String name) {
            _name = name;
        }

        @Override protected void init (float time) {
            super.init(time);
            _lastTime = time;
            play(_name);
            _playing = movie();
        }

        @Override protected float apply (float time) {
            if (movie() != _playing) {
                return 0;

            } else {
                float dt = time - _lastTime;
                _lastTime = time;

                float remaining = _playing.symbol().duration - _playing.position() -
                    dt*_playing.speed();
                return remaining;
            }
        }

        @Override protected void makeComplete () {
            // fast-forward the playing movie so that the next paint call will switch back to the
            // looping animation
            _playing.setPosition(_playing.symbol().duration);
            _playing = null;
        }

        protected String _name;
        protected Movie _playing;
        protected float _lastTime;
    }

    protected Library _lib;
    protected GroupLayer _root;

    protected Movie _oneshotMovie;
    protected Movie _loopingMovie;
}
