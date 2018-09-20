//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
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

    /** Uses the smallest of the client and server integral numbers. */
    Resolver<Number> INTMIN = new Resolver<Number>() {
        public <V extends Number> V resolve (V client, V server) {
            return (client.longValue() > server.longValue()) ? server : client;
        }
    };

    /** Uses the largest of the client and server integral numbers. */
    Resolver<Number> INTMAX = new Resolver<Number>() {
        public <V extends Number> V resolve (V client, V server) {
            return (client.longValue() > server.longValue()) ? client : server;
        }
    };

    /** Uses the smallest of the client and server floating point numbers. */
    Resolver<Number> FLOATMIN = new Resolver<Number>() {
        public <V extends Number> V resolve (V client, V server) {
            return (client.doubleValue() > server.doubleValue()) ? server : client;
        }
    };

    /** Uses the largest of the client and server floating point numbers. */
    Resolver<Number> FLOATMAX = new Resolver<Number>() {
        public <V extends Number> V resolve (V client, V server) {
            return (client.doubleValue() > server.doubleValue()) ? client : server;
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
