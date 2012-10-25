//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import playn.core.GroupLayer;
import playn.core.Layer;
import static playn.core.PlayN.*;

/**
 * A convenient controller to play though multiple different movies. Designed for characters and
 * objects that have a separate Flump symbol for each of their animations, and need to switch
 * between them. The played movies will be added to a new child entity of the owner.
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

    public void update (float dt) {
        // If this update would end the oneshot movie, replace it with the looping movie
        if (_oneshotMovie != null &&
                _oneshotMovie.position()+dt > _oneshotMovie.symbol().duration()) {
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
        _root.clear();
        _root.add(movie.layer());
        return _currentMovie = movie;
    }

    protected Library _lib;
    protected GroupLayer _root;

    protected Movie _currentMovie;
    protected Movie _oneshotMovie;
    protected Movie _loopingMovie;
}

