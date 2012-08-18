package tripleplay.flump;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        /**
         * The number of frames in this movie.
         */
        public final int frames;

        /**
         * The layers in this movie.
         */
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
        for (int ii = 0; ii < _animators.length; ++ii) {
            LayerAnimator animator = new LayerAnimator(symbol.layers.get(ii));
            _animators[ii] = animator;
            _root.add(animator.layer);
        }
        setFrame(1);
    }

    @Override public GroupLayer layer () {
        return _root;
    }

    @Override public void update (float dt) {
        _position += _speed*dt;
        if (_position > _symbol.duration()) {
            _position = _position % _symbol.duration();
        }

        float nextFrame = _position*_symbol.frameRate();
        setFrame(nextFrame);
    }

    public float position () {
        return _position;
    }

    public void setPosition (float position) {
        if (position < 0) position = 0;
        _position = position;
        update(0); // Force the display list changes immediately
    }

    public float speed () {
        return _speed;
    }

    public void setSpeed (float speed) {
        _speed = speed;
    }

    protected void setFrame (float frame)
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
            _animators[ii].composeFrame(frame);
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

        public void update (float dt) {
            if (_current != null) {
                _current.update(dt);
            }
        }

        public void composeFrame (float frame) {
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
            if (keyframeIdx == finalFrame) {
                content.setTranslation(kf.loc.x(), kf.loc.y());
                content.setScale(kf.scale.x(), kf.scale.y());
                content.setRotation(kf.skew.x());
                content.setAlpha(kf.alpha);

            } else {
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
                content.setTranslation(
                    kf.loc.x() + (nextKf.loc.x()-kf.loc.x())*interp,
                    kf.loc.y() + (nextKf.loc.y()-kf.loc.y())*interp);
                content.setScale(
                    kf.scale.x() + (nextKf.scale.x()-kf.scale.x())*interp,
                    kf.scale.y() + (nextKf.scale.y()-kf.scale.y())*interp);
                content.setRotation(kf.skew.x() + (nextKf.skew.x()-kf.skew.x())*interp);
                content.setAlpha(kf.alpha + (nextKf.alpha-kf.alpha)*interp);
            }

            // content.setOrigin(kf.pivot.x(), kf.pivot.y());
            content.setVisible(kf.visible);
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
