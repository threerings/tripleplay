//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

/**
 * A shared interface by all resources that can be destroyed (manually cleaned up).
 */
public interface Destroyable
{
  /** Destroys this resource. The resource will no longer be usable after this call. */
  void destroy ();
}
