//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.util;

import org.junit.Assert;
import org.junit.Test;

import playn.core.Connection;

public class LayersTest
{
    @Test
    public void testJoin () {
        class TestConn implements Connection {
            int disconnects;
            @Override public void disconnect () {
                disconnects++;
            }
        }

        Connection joined;
        joined = Layers.join();
        joined.disconnect();
        joined.disconnect();

        TestConn c1 = new TestConn();
        joined = Layers.join(c1, joined);
        joined.disconnect();
        Assert.assertEquals(c1.disconnects, 1);
        joined.disconnect();
        Assert.assertEquals(c1.disconnects, 1);
    }
}
