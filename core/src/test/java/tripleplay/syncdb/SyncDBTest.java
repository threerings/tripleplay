//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.syncdb;

import java.util.HashMap;
import java.util.Map;

import react.RMap;
import react.RSet;
import react.Value;

import playn.core.Platform;
import playn.core.StubPlatform;
import playn.core.util.Callback;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;

import org.junit.*;
import static org.junit.Assert.*;

public class SyncDBTest
{
    public static class TestDB extends SyncDB {
        public final Value<Boolean> trueBool = value(
            "trueBool", false, Codec.BOOLEAN, Resolver.TRUE);
        public final Value<Boolean> falseBool = value(
            "falseBool", true, Codec.BOOLEAN, Resolver.FALSE);
        public final Value<Integer> maxInt = value("maxInt", 0, Codec.INT, Resolver.INTMAX);
        public final Value<Long> maxLong = value("maxLong", 0L, Codec.LONG, Resolver.INTMAX);
        public final Value<String> serverString = value(
            "serverString", null, Codec.STRING, Resolver.SERVER);
        public final RSet<String> unionSet = set("unionSet", Codec.STRING, SetResolver.UNION);
        public final RSet<String> interSet = set(
            "interSet", Codec.STRING, SetResolver.INTERSECTION);
        public final RSet<String> serverSet = set("serverSet", Codec.STRING, SetResolver.SERVER);
        public final RMap<String,Integer> maxMap = map(
            "maxMap", Codec.STRING, Codec.INT, Resolver.INTMAX);

        public TestDB () {
            this(testPlatform());
        }

        public TestDB (Platform platform) {
            super(platform);
        }

        @Override public TestDB clone () {
            return new TestDB(_platform);
        }

        public void assertEquals (TestDB other) {
            Assert.assertEquals(trueBool, other.trueBool);
            Assert.assertEquals(falseBool, other.falseBool);
            Assert.assertEquals(maxInt, other.maxInt);
            Assert.assertEquals(maxLong, other.maxLong);
            Assert.assertEquals(serverString, other.serverString);
            Assert.assertEquals(unionSet, other.unionSet);
            Assert.assertEquals(interSet, other.interSet);
            Assert.assertEquals(serverSet, other.serverSet);
            Assert.assertEquals(maxMap, other.maxMap);
        }
    }

    @Test public void testSimpleSync () {
        Protocol.Session session = testSession();
        TestDB one = new TestDB(), two = new TestDB();

        one.trueBool.update(true);
        one.maxInt.update(42);
        one.maxLong.update(400L);
        one.serverString.update("foo");
        one.unionSet.add("one");
        one.unionSet.add("two");
        one.unionSet.add("three");
        one.interSet.add("four");
        one.serverSet.add("five");

        session.sync(one);
        session.sync(two);
        one.assertEquals(two);

        // make sure we have no lingering modifications
        assertEquals(0, one.getMods().size());
        assertEquals(0, two.getMods().size());
    }

    @Test public void testModsWhileSyncing () {
        final TestDB one = new TestDB(), two = new TestDB();
        Protocol.Session session = new Protocol.Session(new TestServer()) {
            @Override protected void onSyncSuccess (SyncDB db, Map<String,Integer> mods,
                                                    Protocol.Response rsp) {
                // simulate a concurrent modification by modifying values in one just before we
                // note that our sync has completed
                if (rsp.cleanSync) {
                    one.maxInt.update(50); // modify a property that was part of the sync
                    one.maxLong.update(400L); // and modify a property that was not part of the sync
                }
                super.onSyncSuccess(db, mods, rsp);
            }
            @Override protected void onSyncFailure (SyncDB db, Throwable cause) {
                System.err.println("Sync failure " + cause);
            }
        };

        one.trueBool.update(true);
        one.maxInt.update(42);
        one.serverString.update("foo");

        session.sync(one);
        session.sync(two);
        // one.assertEquals(two); // they won't be equal as we modified 'one' concurrently

        // make sure the concurrent modifications in DB one did not get wiped out
        assertEquals(Sets.newHashSet("maxInt", "maxLong"), one.getMods().keySet());
        assertEquals(0, two.getMods().size());
    }

    @Test public void testMaxing () {
        Protocol.Session session = testSession();
        TestDB one = new TestDB(), two = new TestDB();

        // start with some synced changes
        one.maxInt.update(42);
        one.maxLong.update(60L);
        session.sync(one);
        session.sync(two);

        // now make conflicting changes to both client one and two and resync
        one.maxInt.update(40);
        one.maxLong.update(65L);
        two.maxInt.update(45);
        two.maxLong.update(30L);
        session.sync(one); // this will go through no questions asked
        session.sync(two); // this will sync one's changes into two and overwrite some of one's
                           // changes with the merged data from two
        session.sync(one); // this will sync two's merged data back to one

        one.assertEquals(two);
        assertEquals(45, one.maxInt.get().intValue());
        assertEquals(65L, one.maxLong.get().longValue());

        // make sure we have no lingering modifications
        assertEquals(0, one.getMods().size());
        assertEquals(0, two.getMods().size());
    }

    @Test public void testUseServer () {
        Protocol.Session session = testSession();
        TestDB one = new TestDB(), two = new TestDB();

        // start with some synced changes
        one.serverString.update("foo");
        session.sync(one);
        session.sync(two);

        // now make conflicting changes to both client one and two and resync
        one.serverString.update("bar");
        two.serverString.update("baz");
        session.sync(one); // this will go through no questions asked
        session.sync(two); // this will sync one's changes into two

        one.assertEquals(two);
        assertEquals("bar", two.serverString.get());

        // make sure we have no lingering modifications
        assertEquals(0, one.getMods().size());
        assertEquals(0, two.getMods().size());
    }

    @Test public void testSets () {
        Protocol.Session session = testSession();
        TestDB one = new TestDB(), two = new TestDB();

        // start with some synced changes
        one.unionSet.add("one");
        one.unionSet.add("two");
        one.interSet.add("1");
        one.interSet.add("2");
        one.serverSet.add("a");
        one.serverSet.add("b");
        session.sync(one);
        session.sync(two);

        // now make conflicting changes to both client one and two and resync
        one.unionSet.add("three");
        two.unionSet.add("four");
        one.interSet.remove("1");
        two.interSet.add("3");
        one.serverSet.add("c");
        two.serverSet.remove("b");
        session.sync(one); // this will go through no questions asked
        session.sync(two); // this will sync one's changes into two and overwrite some of one's
                           // changes with the merged data from two
        session.sync(one); // this will sync two's merged data back to one

        one.assertEquals(two);
        assertEquals(Sets.newHashSet("one", "two", "three", "four"), two.unionSet);
        assertEquals(Sets.newHashSet("2"), two.interSet);
        assertEquals(Sets.newHashSet("a", "b", "c"), two.serverSet);

        // make sure we reread our data from storage properly
        one.assertEquals(one.clone());

        // make sure we have no lingering modifications
        assertEquals(0, one.getMods().size());
        assertEquals(0, two.getMods().size());
    }

    @Test public void testMap () {
        Protocol.Session session = testSession();
        TestDB one = new TestDB(), two = new TestDB();

        // start with some synced changes
        one.maxMap.put("one", 1);
        one.maxMap.put("two", 2);
        session.sync(one);
        session.sync(two);

        // now make conflicting changes to both client one and two and resync
        one.maxMap.put("three", 3);
        one.maxMap.put("four", 4);
        one.maxMap.remove("one");
        two.maxMap.put("four", 44);
        session.sync(one); // this will go through no questions asked
        session.sync(two); // this will sync one's changes into two and overwrite some of one's
                           // changes with the merged data from two
        session.sync(one); // this will sync two's merged data back to one

        one.assertEquals(two);
        assertEquals(ImmutableMap.of("two", 2, "three", 3, "four", 44), two.maxMap);

        // make sure we reread our data from storage properly
        one.assertEquals(one.clone());

        // make sure we have no lingering modifications
        assertEquals(0, one.getMods().size());
        assertEquals(0, two.getMods().size());
    }

    protected void makeTestChanges2 (TestDB db) {
        db.trueBool.update(false);
        db.maxInt.update(60);
        db.maxLong.update(1000L);
        db.serverString.update("bar");
        db.unionSet.remove("one");
        db.unionSet.add("five");
        db.interSet.add("one");
        db.serverSet.add("six");
    }

    protected static class Datum {
        public final int version;
        public final String value;
        public Datum (int version, String value) {
            this.version = version;
            this.value = value;
        }
    }

    protected static class TestServer implements Protocol.Server {
        @Override public void sendSync (int version, Map<String,String> delta,
                                        Callback<Protocol.Response> onResponse) {
            if (version > _version) {
                throw new IllegalStateException("So impossible! " + version + " > " + _version);
            } else if (version < _version) {
                onResponse.onSuccess(needSync(version));
            } else if (delta.size() == 0) {
                onResponse.onSuccess(new Protocol.Response(_version));
            } else {
                _version += 1;
                for (Map.Entry<String,String> entry : delta.entrySet()) {
                    _data.put(entry.getKey(), new Datum(_version, entry.getValue()));
                }
                onResponse.onSuccess(new Protocol.Response(_version));
            }
        }

        protected Protocol.Response needSync (int clientVers) {
            Map<String,String> delta = new HashMap<String,String>();
            for (Map.Entry<String,Datum> entry : _data.entrySet()) {
                Datum d = entry.getValue();
                if (d.version > clientVers) delta.put(entry.getKey(), d.value);
            }
            return new Protocol.Response(_version, delta);
        }

        protected int _version;
        protected Map<String,Datum> _data = new HashMap<String,Datum>();
    }

    protected static Protocol.Session testSession () {
        return new Protocol.Session(new TestServer()) {
            @Override protected void onSyncFailure (SyncDB db, Throwable cause) {
                System.err.println("Sync failure " + cause);
            }
        };
    }

    protected static Platform testPlatform () {
        return new StubPlatform();
    }
}
