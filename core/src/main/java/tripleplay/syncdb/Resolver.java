//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

/**
 * Provides a mechanism for resolving conflict between two conflicting values in a sync db.
 */
public interface Resolver<T>
{
    /** Uses the server value, ignoring the client value. */
    Resolver<Object> SERVER = new Resolver<Object>() {
        public <V> V resolve (V client, V server) {
            return server;
        }
    };

    /** Uses the smallest of the client and server integers. */
    Resolver<Integer> INTMIN = new Resolver<Integer>() {
        public <V extends Integer> V resolve (V client, V server) {
            return (client > server) ? server : client;
        }
    };

    /** Uses the largest of the client and server integers. */
    Resolver<Integer> INTMAX = new Resolver<Integer>() {
        public <V extends Integer> V resolve (V client, V server) {
            return (client > server) ? client : server;
        }
    };

    /** Uses the largest of the client and server longs. */
    Resolver<Long> LONGMAX = new Resolver<Long>() {
        public <V extends Long> V resolve (V client, V server) {
            return (client > server) ? client : server;
        }
    };

    /** Uses the largest of the client and server floats. */
    Resolver<Float> FLOATMAX = new Resolver<Float>() {
        public <V extends Float> V resolve (V client, V server) {
            return (client > server) ? client : server;
        }
    };

    /** Uses whichever of the client or server is true. */
    Resolver<Boolean> TRUE = new Resolver<Boolean>() {
        public <V extends Boolean> V resolve (V client, V server) {
            return client ? client : server;
        }
    };

    /** Uses whichever of the client or server is false. */
    Resolver<Boolean> FALSE = new Resolver<Boolean>() {
        public <V extends Boolean> V resolve (V client, V server) {
            return client ? server : client;
        }
    };

    /** Resolves a conflict between a client and server value.
     * @return the value to be used on the client. */
    <V extends T> V resolve (V client, V server);
}
