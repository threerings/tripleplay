//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

/** Defines a Flump symbol. */
public interface Symbol
{
    /** The exported name of this symbol. */
    String name ();

    /** Creates a new instance of this symbol. */
    Instance createInstance ();
}
