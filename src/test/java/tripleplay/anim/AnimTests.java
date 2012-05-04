//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.anim;

import playn.core.Game;
import playn.core.GroupLayer;
import playn.core.PlayN;
import playn.java.JavaPlatform;

public class AnimTests implements Game
{
    public static abstract class Test {
        public abstract void init ();

        public void update (float delta) {
            _elapsed += delta;
        }

        public void paint (float alpha) {
            _anim.update(_elapsed + alpha * 30);
        }

        protected float _elapsed;
        protected final Animator _anim = Animator.create();
    }

    public static void main (String[] args) {
        _mainArgs = args;
        JavaPlatform.register();
        PlayN.run(new AnimTests());
    }

    void nextTest () {
        GroupLayer root = PlayN.graphics().rootLayer();
        for (int ii = root.size()-1; ii >= 0; ii--) root.get(ii).destroy();
        _test = _tests[++_testIdx];
        _test.init();
    }

    @Override // from interface Game
    public void init () {
        // TODO: wire up some interaction for moving to move to next test
        nextTest();
    }

    @Override // from interface Game
    public void update (float delta) {
        _test.update(delta);
    }

    @Override // from interface Game
    public void paint (float alpha) {
        _test.paint(alpha);
    }

    @Override // from interface Game
    public int updateRate () {
        return 60;
    }

    protected Test[] _tests = {
        new FramesTest(),
        new RepeatTest()
    };

    protected Test _test;
    protected int _testIdx = -1;
    protected static String[] _mainArgs;
}
