//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.HashMap;
import java.util.Map;

import playn.core.Asserts;
import playn.core.Net;
import playn.core.util.Callback;

/**
 * Handles the process of syncing a databse with the server.
 */
public class Protocol
{
    /** Encapsulates a syncing session with the server. Also provides hooks for a game to perform
     * custom conflict resolution before deltas are applied to the database. */
    public static abstract class Session {
        public Session (Server server) {
            _server = server;
        }

        /** Syncs the supplied database with this session's server. */
        public void sync (final SyncDB db) {
            final Map<String,Integer> mods = db.getMods();
            _server.sendSync(db.version(), db.getDelta(), new Callback<Response>() {
                @Override public void onSuccess (Response rsp) { onSyncSuccess(db, mods, rsp); }
                @Override public void onFailure (Throwable cause) { onSyncFailure(db, cause); }
            });
        }

        /** Called after we have completed a clean sync with the server. At this point this client
         * and the server have the same data (at least until some other client syncs data with the
         * server). */
        protected void onCleanSync () {} // noop

        /** Called before we apply a set of deltas from the server that will result in merging of
         * conflicting modifications. The supplied runnable must eventually be executed to apply
         * the deltas and continue the sync process. The game may defer that merger while it asks
         * the client how to resolve merges that cannot be handled by simple conflict resolution
         * policies. */
        protected void onBeforeMerge (int version, Map<String,String> delta, Runnable apply) {
            apply.run();
        }

        /**
         * Called if we fail to contact the server for any reason during a sync. Most likely this
         * will occur when the client has no network connectivity, or the server is down.
         */
        protected abstract void onSyncFailure (SyncDB db, Throwable cause);

        protected void onSyncSuccess (final SyncDB db, Map<String,Integer> mods,
                                      final Response rsp) {
            if (rsp.cleanSync) {
                db.noteSync(rsp.version, mods);
                onCleanSync();
            } else {
                Runnable merge = new Runnable() { public void run () {
                    db.applyDelta(rsp.version, rsp.delta);
                    if (db.hasUnsyncedChanges()) sync(db);
                    else onCleanSync();
                }};
                if (db.containsMerges(rsp.delta)) {
                    onBeforeMerge(rsp.version, rsp.delta, merge);
                } else {
                    merge.run();
                }
            }
        }

        protected final Server _server;
    }

    /** Encapsulates a sync request from the client. */
    public static class Request {
        /** The version of {@link Protocol} in use by the client. */
        public final int protocolVersion = protocolVersion();

        /** The latest version with which the client has synced. */
        public final int version;

        /** The changes that have been made on the client since the last sync. */
        public final Map<String,String> delta;

        /** Creates a request with the supplied data. */
        public Request (int version, Map<String,String> delta) {
            this.version = version;
            this.delta = delta;
        }
    }

    /** Encapsualtes a response from the server. */
    public static class Response {
        /** The version of {@link Protocol} in use by the client. */
        public final int protocolVersion = protocolVersion();

        /** The latest server version. */
        public final int version;

        /** Deltas the client needs to apply to sync with the latest server version. */
        public final Map<String,String> delta;

        /** Whether the client's submitted deltas were applied cleanly to the server. If false, the
         * client's submitted delta was not applied at all to the server and should be resubmitted
         * after the client applies the server deltas. */
        public final boolean cleanSync;

        /** Creates a response that represents a clean sync. */
        public Response (int version) {
            this(version, new HashMap<String,String>(), true);
        }

        /** Creates a (non-clean-sync) response with the supplied version and delta. */
        public Response (int version, Map<String,String> delta) {
            this(version, delta, false);
        }

        protected Response (int version, Map<String,String> delta, boolean cleanSync) {
            this.version = version;
            this.delta = delta;
            this.cleanSync = cleanSync;
        }
    }

    /** Abstracts away the sending of a sync request to the server. */
    public interface Server {
        /** Sends a sync request to the server, receives its response and dispatches it to the
         * supplied callback. */
        void sendSync (int version, Map<String,String> delta, Callback<Response> onResponse);
    }

    /** A {@link Server} implementation that delivers deltas to the server via {@link Net}. */
    public static abstract class NetServer implements Server {
        /** Creates a net server instance with the supplied Net service and sync URL. */
        public NetServer (Net net) {
            _net = net;
        }

        @Override
        public void sendSync (int version, Map<String,String> delta, final Callback<Response> cb) {
            String payload = encodeRequest(new Request(version, delta));
            _net.post(syncURL(payload), payload, new Callback<String>() {
                public void onSuccess (String payload) {
                    try {
                        cb.onSuccess(decodeResponse(payload));
                    } catch (Throwable t) {
                        onFailure(t);
                    }
                }
                public void onFailure (Throwable cause) {
                    cb.onFailure(cause);
                }
            });
        }

        /** Generates the sync URL, given the supplied payload. The client may wish to sign the URL
         * based on the contents of the payload for security purposes. */
        protected abstract String syncURL (String payload);

        protected final Net _net;
    }

    /** Used to decode ints and strings from one big compact string. */
    public static class PayloadReader {
        public PayloadReader (String payload) {
            _payload = payload;
        }

        public boolean atEOF () {
            return _pos >= _payload.length();
        }

        public int readInt () {
            int value = 0;
            char c;
            do {
                value *= BASE;
                c = _payload.charAt(_pos++);
                value += (c >= CONT0) ? (c - CONT0) : (c - ABS0);
            } while (c >= CONT0);
            return value;
        }

        public String readString () {
            int length = readInt(), start = _pos;
            if (length == Short.MAX_VALUE) return null;
            try {
                return _payload.substring(start, _pos += length);
            } catch (Exception e) {
                throw new RuntimeException(
                    "Invalid readString state [start=" + start + ", length=" + length + "]", e);
            }
        }

        protected final String _payload;
        protected int _pos;
    }

    /** Used to encode ints and strings in one big compact string. */
    public static class PayloadWriter {
        public void writeInt (int value) {
            Asserts.checkArgument(value >= 0, "Cannot write negative integers to payload.");
            writeInt(value, false);
        }

        public void writeString (String value) {
            Asserts.checkArgument(value == null || value.length() < Short.MAX_VALUE,
                                  "Strings must be less than " + Short.MAX_VALUE + " chars.");
            if (value == null) writeInt(Short.MAX_VALUE);
            else {
                writeInt(value.length());
                _payload.append(value);
            }
        }

        public String payload () {
            return _payload.toString();
        }

        protected void writeInt (int value, boolean cont) {
            if (value >= BASE) writeInt(value / BASE, true);
            _payload.append((cont ? VARCONT : VARABS).charAt(value % BASE));
        }

        protected StringBuilder _payload = new StringBuilder();
    }

    /** Encodes a client request into a compact string format. */
    public static String encodeRequest (Request req) {
        PayloadWriter out = new PayloadWriter();
        out.writeInt(req.protocolVersion);
        out.writeInt(req.version);
        for (Map.Entry<String,String> entry : req.delta.entrySet()) {
            out.writeString(entry.getKey());
            out.writeString(entry.getValue());
        }
        return out.payload();
    }

    /** Decodes a client request from a compact string format. */
    public static Request decodeRequest (String payload) {
        if (payload == null) throw new NullPointerException("Cannot decode null request.");
        PayloadReader in = new PayloadReader(payload);
        try {
            int protocolVersion = in.readInt();
            if (protocolVersion != 1) throw new UnsupportedOperationException(
                "Unknown protocol version " + protocolVersion);

            int version = in.readInt();
            Map<String,String> delta = new HashMap<String,String>();
            while (!in.atEOF()) delta.put(in.readString(), in.readString());
            return new Request(version, delta);

        } catch (Exception e) {
            throw new RuntimeException("Error decoding request: " + payload, e);
        }
    }

    /** Encodes a server response into a compact string format. */
    public static String encodeResponse (Response rsp) {
        PayloadWriter out = new PayloadWriter();
        out.writeInt(rsp.protocolVersion);
        out.writeInt(rsp.version);
        out.writeInt(rsp.cleanSync ? 1 : 0);
        for (Map.Entry<String,String> entry : rsp.delta.entrySet()) {
            out.writeString(entry.getKey());
            out.writeString(entry.getValue());
        }
        return out.payload();
    }

    /** Decodes a server response from a compact string format. */
    public static Response decodeResponse (String payload) {
        if (payload == null) throw new NullPointerException("Cannot decode null response.");
        PayloadReader in = new PayloadReader(payload);
        try {
            int protocolVersion = in.readInt();
            if (protocolVersion != 1) throw new UnsupportedOperationException(
                "Unknown protocol version " + protocolVersion);

            int version = in.readInt();
            boolean cleanSync = in.readInt() == 1;
            if (cleanSync) return new Response(version);
            Map<String,String> delta = new HashMap<String,String>();
            while (!in.atEOF()) delta.put(in.readString(), in.readString());
            return new Response(version, delta);

        } catch (Exception e) {
            throw new RuntimeException("Error decoding response: " + payload, e);
        }
    }

    /**
     * Returns the current version of the protocol code. This is used to handle backwards
     * compatibility in the unlikely event that this very simple protocol evolves. We can't rely on
     * clients to update themselves, so we may need to bridge the gap on the server.
     */
    public static int protocolVersion () {
        return 1;
    }

    /** Used to encode the final chunk between 0 and 46. */
    protected static final String VARABS =  "!\"#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNO";

    /** Used to encode a chunk between 0 and 46 with continuation. */
    protected static final String VARCONT = "PQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    /** Used when encoding and decoding. */
    protected static final int BASE = VARABS.length();

    /** Used when decoding. */
    protected static final char ABS0 = VARABS.charAt(0), CONT0 = VARCONT.charAt(0);
}
