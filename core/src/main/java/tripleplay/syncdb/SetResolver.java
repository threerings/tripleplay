//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Provides a mechanism for resolving conflict between two conflicting sets in a sync db.
 */
public interface SetResolver
{
    /** Uses the server set, ignoring all elements in the client set. */
    SetResolver SERVER = new SetResolver() {
        public <V> boolean resolve (Set<V> client, Set<V> server) {
            // remove anything in the client set that's not in the server set
            int osize = client.size();
            for (Iterator<V> iter = client.iterator(); iter.hasNext(); ) {
                if (!server.contains(iter.next())) iter.remove();
            }
            boolean removed = client.size() != osize;
            // add the server set to the pruned client set
            return client.addAll(server) || removed;
        }
    };

    /** Uses the union of the client and server sets. */
    SetResolver UNION = new SetResolver() {
        public <V> boolean resolve (Set<V> client, Set<V> server) {
            return client.addAll(server);
        }
    };

    /** Uses the intersection of the client and server sets. */
    SetResolver INTERSECTION = new SetResolver() {
        public <V> boolean resolve (Set<V> client, Set<V> server) {
            return client.retainAll(server);
        }
    };

    /** Resolves a conflict between a client and server set. The client set is mutated to reflect
     * the resolved state.
     * @return whether the client set was mutated during resolution. */
    <V> boolean resolve (Set<V> client, Set<V> server);
}
