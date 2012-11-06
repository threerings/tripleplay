//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2012, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.flump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import pythagoras.f.AffineTransform;
import pythagoras.f.FloatMath;

import playn.core.GroupLayer;
import playn.core.Json;
import playn.core.Layer;
import static playn.core.PlayN.*;

public class Movie
    implements Instance
{
    public static class Symbol
        implements tripleplay.flump.Symbol
    {
        /** The number of frames in this movie. */
        public final int frames;

        /** The layers in this movie. */
        public final List<LayerData> layers;

        protected Symbol (Library lib, Json.Object json) {
            _name = json.getString("id");

            ArrayList<LayerData> layers = new ArrayList<LayerData>();
            this.layers = Collections.unmodifiableList(layers);

            int frames = 0;
            for (Json.Object layerJson : json.getArray("layers", Json.Object.class)) {
                LayerData layer = new LayerData(layerJson);
                frames = Math.max(layer.frames(), frames);
                layers.add(layer);
            }
            this.frames = frames;

            _frameRate = lib.frameRate/1000;
            _duration = frames/_frameRate;
        }

        @Override public String name () {
            return _name;
        }

        @Override public Movie createInstance () {
            return new Movie(this);
        }

        /** The duration of this movie, in milliseconds. */
        public float duration () {
            return _duration;
        }

        protected float frameRate () {
            return _frameRate;
        }

        protected String _name;
        protected float _frameRate;
        protected float _duration;
    }

    protected Movie (Symbol symbol) {
        _symbol = symbol;
        _animators = new LayerAnimator[symbol.layers.size()];
        for (int ii = 0, ll = _animators.length; ii < ll; ++ii) {
            LayerAnimator animator = new LayerAnimator(symbol.layers.get(ii));
            _animators[ii] = animator;
            _root.add(animator.content);
        }
        setFrame(1, 0);
    }

    @Override public GroupLayer layer () {
        return _root;
    }

    @Override public void update (float dt) {
        dt *= _speed;

        _position += dt;
        if (_position > _symbol.duration()) {
            _position = _position % _symbol.duration();
        }

        float nextFrame = _position*_symbol.frameRate();
        setFrame(nextFrame, dt);
    }

    /** The playback position, in milliseconds. */
    public float position () {
        return _position;
    }

    /** Changes the playback position. */
    public void setPosition (float position) {
        if (position < 0) position = 0;
        _position = position;
        update(0); // Force the display list changes immediately
    }

    public Symbol symbol () {
        return _symbol;
    }

    /** The playback speed multiplier, defaults to 1. Larger values will play faster. */
    public float speed () {
        return _speed;
    }

    /** Changes the playback speed multiplier. */
    public void setSpeed (float speed) {
        _speed = speed;
    }

    protected void setFrame (float frame, float dt)
    {
        if (frame < _frame) {
            // Wrap back to the beginning
            for (int ii = 0, ll = _animators.length; ii < ll; ++ii) {
                LayerAnimator animator = _animators[ii];
                animator.changedKeyframe = true;
                animator.keyframeIdx = 0;
            }
        }
        for (int ii = 0, ll = _animators.length; ii < ll; ++ii) {
            LayerAnimator animator = _animators[ii];
            animator.setFrame(frame, dt);
        }
        _frame = frame;
    }

    // Controls a single Flash layer
    protected static class LayerAnimator
    {
        public final Layer content;
        public int keyframeIdx = 0;
        public boolean changedKeyframe = false;

        public LayerAnimator (LayerData data) {
            _data = data;
            if (data._multipleSymbols) {
                _instances = new Instance[data.keyframes.size()];
                for (int ii = 0, ll = _instances.length; ii < ll; ++ii) {
                    _instances[ii] = data.keyframes.get(ii).symbol().createInstance();
                }
                content = graphics().createGroupLayer();
                setCurrent(_instances[0]);

            } else if (data._lastSymbol != null) {
                _current = data._lastSymbol.createInstance();
                content = _current.layer();

            } else {
                content = graphics().createGroupLayer();
            }
        }

        public void setFrame (float frame, float dt) {
            List<KeyframeData> keyframes = _data.keyframes;
            int finalFrame = keyframes.size()-1;

            while (keyframeIdx < finalFrame && keyframes.get(keyframeIdx+1).index <= frame) {
                ++keyframeIdx;
                changedKeyframe = true;
            }

            if (changedKeyframe && _instances != null) {
                // Switch to the next instance if this is a multi-symbol layer
                setCurrent(_instances[keyframeIdx]);
                changedKeyframe = false;
            }

            KeyframeData kf = keyframes.get(keyframeIdx);
            boolean visible = kf.symbol() != null && kf.visible;
            content.setVisible(visible);
            if (!visible) {
                return; // Don't bother animating invisible layers
            }

            float locX = kf.loc.x();
            float locY = kf.loc.y();
            float scaleX = kf.scale.x();
            float scaleY = kf.scale.y();
            float skewX = kf.skew.x();
            float skewY = kf.skew.y();
            float alpha = kf.alpha;

            if (keyframeIdx < finalFrame) {
                // Interpolate with the next keyframe
                float interp = (frame-kf.index) / kf.duration;
                float ease = kf.ease;
                if (ease != 0) {
                    float t;
                    if (ease < 0) {
                        // Ease in
                        float inv = 1 - interp;
                        t = 1 - inv*inv;
                        ease = -ease;
                    } else {
                        // Ease out
                        t = interp*interp;
                    }
                    interp = ease*t + (1-ease)*interp;
                }

                KeyframeData nextKf = keyframes.get(keyframeIdx+1);
                locX += (nextKf.loc.x()-locX) * interp;
                locY += (nextKf.loc.y()-locY) * interp;
                scaleX += (nextKf.scale.x()-scaleX) * interp;
                scaleY += (nextKf.scale.y()-scaleY) * interp;
                skewX += (nextKf.skew.x()-skewX) * interp;
                skewY += (nextKf.skew.y()-skewY) * interp;
                alpha += (nextKf.alpha-alpha) * interp;
            }

            float sinX = FloatMath.sin(skewX), cosX = FloatMath.cos(skewX);
            float sinY = FloatMath.sin(skewY), cosY = FloatMath.cos(skewY);
            float offsetX = -kf.pivot.x(), offsetY = -kf.pivot.y();

            // Create a transformation matrix that translates to locX/Y, skews, scales, then
            // translates by offsetX/Y (in that order)
            float m00 = cosY * scaleX;
            float m01 = sinY * scaleX;
            float m10 = -sinX * scaleY;
            float m11 = cosX * scaleY;
            float tx = locX + m00*offsetX + m10*offsetY;
            float ty = locY + m11*offsetY + m01*offsetX;
            content.transform().setTransform(m00, m01, m10, m11, tx, ty);

            content.setAlpha(alpha);

            if (_current != null) {
                _current.update(dt);
            }
        }

        protected void setCurrent (Instance current) {
            if (_current != current) {
                _current = current;
                GroupLayer group = (GroupLayer)content;
                group.clear();
                group.add(current.layer());
            }
        }

        protected LayerData _data;
        protected Instance _current; // The instance currently visible
        protected Instance[] _instances; // Null if only 0-1 instance on this layer
    }

    protected Symbol _symbol;
    protected GroupLayer _root = graphics().createGroupLayer();
    protected LayerAnimator[] _animators;

    protected float _frame = 0;
    protected float _position = 0;
    protected float _speed = 1;
}
