//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.HashMap;
import java.util.Map;

import playn.core.util.Callback;

import static tripleplay.syncdb.Log.log;

/**
 * Handles the process of syncing a databse with the server.
 */
public class Protocol
{
    /** Encapsualtes a result from the server. */
    public static class Result {
        /** The latest server version. */
        public final int version;

        /** Deltas the client needs to apply to sync with the latest server version. */
        public final Map<String,String> delta;

        /** Whether the client's submitted deltas were applied cleanly to the server. If false, the
         * client's submitted delta was not applied at all to the server and should be resubmitted
         * after the client applies the server deltas. */
        public final boolean cleanSync;

        /** Creates a result that represents a clean sync. */
        public Result (int version) {
            this(version, new HashMap<String,String>(), true);
        }

        /** Creates a (non-clean-sync) result with the supplied version and delta. */
        public Result (int version, Map<String,String> delta) {
            this(version, delta, false);
        }

        protected Result (int version, Map<String,String> delta, boolean cleanSync) {
            this.version = version;
            this.delta = delta;
            this.cleanSync = cleanSync;
        }
    }

    /** Abstracts away the sending of a sync request to the server. */
    public interface Server {
        /** Sends a sync request to the server, receives its response and dispatches it to the
         * supplied callback. */
        void sendSync (int version, Map<String,String> delta, Callback<Result> onResult);
    }

    /** Encapsulates a syncing session with the server. Also provides hooks for a game to perform
     * custom conflict resolution before deltas are applied to the database. */
    public static abstract class Session {
        public Session (Server server) {
            _server = server;
        }

        /** Syncs the supplied database with this session's server. */
        public void sync (final SyncDB db) {
            _server.sendSync(db.version(), db.getDelta(), new Callback<Result>() {
                @Override public void onSuccess (Result result) { onSyncSuccess(db, result); }
                @Override public void onFailure (Throwable cause) { onSyncFailure(db, cause); }
            });
        }

        protected void onSyncSuccess (final SyncDB db, final Result result) {
            if (result.cleanSync) {
                db.noteSync(result.version);
                onCleanSync();
            } else {
                Runnable merge = new Runnable() { public void run () {
                    db.applyDelta(result.version, result.delta);
                    if (db.hasUnsyncedChanges()) sync(db);
                }};
                if (db.containsMerges(result.delta)) {
                    onBeforeMerge(result.version, result.delta, merge);
                } else {
                    merge.run();
                }
            }
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

        protected abstract void onSyncFailure (SyncDB db, Throwable cause);

        protected final Server _server;
    }
}
