//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2013, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.entity;

import java.util.Iterator;
import java.util.Random;

import playn.core.*;
import playn.core.util.Clock;
import static playn.core.PlayN.*;

import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;
import pythagoras.f.Vector;

import react.Signal;
import react.Slot;

import tripleplay.ui.Group;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Randoms;
import tripleplay.util.TextConfig;

import tripleplay.entity.Component;
import tripleplay.entity.Entity;
import tripleplay.entity.System;
import tripleplay.entity.World;

import tripleplay.demo.DemoScreen;

public class AsteroidsDemo extends DemoScreen
{
    public final Image asteroids = assets().getImage("images/asteroids.png");

    class AsteroidsWorld extends World {
        public final GroupLayer stage;
        public final float swidth, sheight;
        public final Randoms rando = Randoms.with(new Random());

        public final Component.XY opos = new Component.XY(this);
        public final Component.XY pos = new Component.XY(this);
        public final Component.XY vel = new Component.XY(this); // pixels/ms
        public final Component.Generic<Layer> sprite = new Component.Generic<Layer>(this);
        public final Component.FScalar spin = new Component.FScalar(this); // rads/ms
        public final Component.FScalar radius = new Component.FScalar(this);
        public final Component.IScalar expires = new Component.IScalar(this);

        public final Signal<Key> keyDown = Signal.create();
        public final Signal<Key> keyUp = Signal.create();

        public int now; // ms elapsed since world start, used by expirer/expires

        // handles updating entity position based on entity velocity
        public final System mover = new System(this, 0) {
            @Override protected void update (int delta, Entities entities) {
                Point p = _pos;
                Vector v = _vel;
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid = entities.get(ii);
                    pos.get(eid, p); // get our current pos
                    p.x = wrapx(p.x); // wrap it around the screen if necessary
                    p.y = wrapy(p.y);
                    opos.set(eid, p); // copy wrapped pos to opos
                    vel.get(eid, v).scaleLocal(delta); // turn velocity into delta pos
                    pos.set(eid, p.x + v.x, p.y + v.y); // add velocity (but don't wrap)
                }
            }

            @Override protected boolean isInterested (Entity entity) {
                return entity.has(opos) && entity.has(pos) && entity.has(vel);
            }

            protected final Point _pos = new Point();
            protected final Vector _vel = new Vector();
        };

        private float wrapx (float x) {
            return (x > swidth) ? (x-swidth) : ((x < 0) ? (x+swidth) : x);
        }
        private float wrapy (float y) {
            return (y > sheight) ? (y-sheight) : ((y < 0) ? (y+sheight) : y);
        }

        // updates sprites to interpolated position of entities on each paint() call
        public final System spriter = new System(this, 0) {
            @Override protected void paint (Clock clock, Entities entities) {
                float alpha = clock.alpha();
                Point op = _oldPos, p = _pos;
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid = entities.get(ii);
                    // interpolate between opos and pos and use that to update the sprite position
                    opos.get(eid, op);
                    pos.get(eid, p);
                    // wrap our interpolated position as we may interpolate off the screen
                    sprite.get(eid).setTranslation(wrapx(MathUtil.lerp(op.x, p.x, alpha)),
                                                   wrapy(MathUtil.lerp(op.y, p.y, alpha)));
                }
            }

            @Override protected void wasAdded (Entity entity) {
                super.wasAdded(entity);
                stage.addAt(sprite.get(entity.id), pos.getX(entity.id), pos.getX(entity.id));
            }

            @Override protected void wasRemoved (Entity entity, int index) {
                super.wasRemoved(entity, index);
                stage.remove(sprite.get(entity.id));
            }

            @Override protected boolean isInterested (Entity entity) {
                return entity.has(opos) && entity.has(pos) && entity.has(sprite);
            }

            protected final Point _oldPos = new Point(), _pos = new Point();
        };

        // spins things
        public final System spinner = new System(this, 0) {
            @Override protected void paint (Clock clock, Entities entities) {
                float dt = clock.dt();
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid = entities.get(ii);
                    float angvel = spin.get(eid);
                    if (angvel == 0) continue;
                    Layer s = sprite.get(eid);
                    s.setRotation(s.rotation() + angvel * dt);
                }
            }

            @Override protected boolean isInterested (Entity entity) {
                return entity.has(spin) && entity.has(sprite);
            }
        };

        // expires things with limited lifespan (like bullets)
        public final System expirer = new System(this, 0) {
            @Override protected void update (int delta, Entities entities) {
                int now = AsteroidsWorld.this.now;
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid = entities.get(ii);
                    if (expires.get(eid) <= now) world.entity(eid).destroy();
                }
            }

            @Override protected boolean isInterested (Entity entity) {
                return entity.has(expires);
            }
        };

        // checks for collisions (modeling everything as a sphere)
        public final System collider = new System(this, 1) {
            @Override protected void update (int delta, Entities entities) {
                // simple O(n^2) collision check; no need for anything fancy here
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid1 = entities.get(ii);
                    Entity e1 = world.entity(eid1);
                    if (e1.isDestroyed()) continue;
                    pos.get(eid1, _p1);
                    float r1 = radius.get(eid1);
                    for (int jj = ii+1; jj < ll; jj++) {
                        int eid2 = entities.get(jj);
                        Entity e2 = world.entity(eid2);
                        if (e2.isDestroyed()) continue;
                        pos.get(eid2, _p2);
                        float r2 = radius.get(eid2), dr = r2+r1;
                        float dist2 = _p1.distanceSq(_p2);
                        if (dist2 <= dr*dr) {
                            collide(e1, e2);
                            break; // don't collide e1 with any other entities
                        }
                    }
                }
            }

            @Override protected boolean isInterested (Entity entity) {
                return entity.has(pos) && entity.has(radius);
            }

            private void collide (Entity e1, Entity e2) {
                switch (mask(e1) | mask(e2)) {
                case SHIP_ASTEROID:
                    explode(e1 instanceof Ship ? e1 : e2, 10, 0.75f);
                    setMessage("Game Over. Press 's' to restart");
                    _wave = -1;
                    break;
                case BULLET_ASTEROID:
                    if (e1 instanceof Asteroid) {
                        ((Asteroid)e1).sunder(AsteroidsWorld.this);
                        e2.destroy();
                    } else {
                        ((Asteroid)e2).sunder(AsteroidsWorld.this);
                        e1.destroy();
                    }
                    break;
                // TODO: asteroid asteroid?
                default: break; // nada
                }
            }

            private int mask (Entity e) {
                if (e instanceof Ship)     return SHIP;
                if (e instanceof Asteroid) return ASTEROID;
                if (e instanceof Bullet)   return BULLET;
                return 0;
            }

            protected static final int SHIP     = (1 << 0);
            protected static final int ASTEROID = (1 << 1);
            protected static final int BULLET   = (1 << 2);

            protected static final int SHIP_ASTEROID = SHIP|ASTEROID;
            protected static final int BULLET_ASTEROID = BULLET|ASTEROID;

            protected final Point _p1 = new Point(), _p2 = new Point();
        };

        // handles progression to next wave
        public final System waver = new System(this, 0) {
            @Override protected void update (int delta, Entities entities) {
                // if the only entity left is the player's ship; move to the next wave
                if (entities.size() == 1 && world.entity(entities.get(0)) instanceof Ship) {
                    startWave(++_wave);
                }
            }

            @Override protected boolean isInterested (Entity entity) {
                return true;
            }
        };

        // handles player input
        public final System controls = new System(this, 1) {
            public static final float ACCEL = 0.01f;
            public static final float ROT = 0.005f;
            public static final float MAX_VEL = 1f;
            public static final int   BULLET_LIFE = 1000; // ms
            public static final float BULLET_VEL = 0.25f;

            /* ctor */ {
                keyDown.connect(new Slot<Key>() {
                    public void onEmit (Key key) {
                        switch (key) {
                        case LEFT:  _angvel = -ROT; break;
                        case RIGHT: _angvel =  ROT; break;
                        case UP:    _accel  =  ACCEL; break;
                        case SPACE: fireBullet(); break;
                        case S:     if (_wave == -1) startWave(0); break;
                        default: break;
                        }
                    }
                });
                keyUp.connect(new Slot<Key>() {
                    public void onEmit (Key key) {
                        switch (key) {
                        case LEFT:  _angvel = 0; break;
                        case RIGHT: _angvel = 0; break;
                        case UP:    _accel  = 0; break;
                        default: break;
                        }
                    }
                });
            }

            public void fireBullet () {
                float ang = sprite.get(_ship.id).rotation();
                float vx = vel.getX(_ship.id), vy = vel.getY(_ship.id);
                float bvx = vx+BULLET_VEL*FloatMath.cos(ang), bvy = vy+BULLET_VEL*FloatMath.sin(ang);
                new Bullet(AsteroidsWorld.this, pos.getX(_ship.id), pos.getY(_ship.id),
                           bvx, bvy, ang, now + BULLET_LIFE);
                // vel.set(_ship.id, vx-bvx/100, vy-bvy/100); // decrease ship's velocity a smidgen
            }

            @Override protected void update (int delta, Entities entities) {
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid = entities.get(ii);
                    spin.set(eid, _angvel);
                    Vector v = _vel;
                    if (_accel != 0) {
                        Layer s = sprite.get(eid);
                        float ang = s.rotation();
                        vel.get(eid, v);
                        v.x = MathUtil.clamp(v.x + FloatMath.cos(ang)*_accel, -MAX_VEL, MAX_VEL);
                        v.y = MathUtil.clamp(v.y + FloatMath.sin(ang)*_accel, -MAX_VEL, MAX_VEL);
                        vel.set(eid, v);
                    }
                }
            }

            @Override protected void wasAdded (Entity entity) {
                super.wasAdded(entity);
                if (entity instanceof Ship) _ship = (Ship)entity;
            }

            @Override protected boolean isInterested (Entity entity) {
                return entity instanceof Ship;
            }

            protected float _angvel, _accel;
            protected Vector _vel = new Vector();
            protected Ship _ship;
        };

        public AsteroidsWorld (GroupLayer stage, float swidth, float sheight) {
            this.stage = stage;
            this.swidth = swidth;
            this.sheight = sheight;

            keyboard().setListener(new Keyboard.Adapter() {
                public void onKeyDown (Keyboard.Event event) {
                    keyDown.emit(event.key());
                }
                public void onKeyUp (Keyboard.Event event) {
                    keyUp.emit(event.key());
                }
            });
        }

        public void setMessage (String text) {
            if (_msg != null) _msg.destroy();
            if (text != null) {
                _msg = MSG_CFG.toLayer(text);
                _msg.setDepth(1);
                stage.addAt(_msg, (swidth-_msg.width())/2, (sheight-_msg.height())/2);
            }
        }

        public void attract () {
            for (int ii = 0; ii < 5; ii++) new Asteroid(
                this, Size.LARGE, rando.getFloat(swidth), rando.getFloat(sheight));
            setMessage("Press 's' to start.");
            _wave = -1;
        }

        public void startWave (int wave) {
            // if this is wave 0, destroy any existing entities and add our ship
            if (wave == 0) {
                Iterator<Entity> iter = entities();
                while (iter.hasNext()) iter.next().destroy();
                new Ship(this, swidth/2, sheight/2);
                setMessage(null);
            }
            for (int ii = 0, ll = Math.min(10, wave+2); ii < ll; ii++) {
                float x = rando.getFloat(swidth), y = rando.getFloat(sheight);
                // TODO: make sure x/y doesn't overlap ship
                new Asteroid(this, Size.LARGE, x, y);
            }
            _wave = wave;
        }

        public void explode (Entity target, int frags, float maxvel) {
            float x = pos.getX(target.id), y = pos.getY(target.id);
            // create a bunch of bullets going in random directions from the ship
            for (int ii = 0; ii < frags; ii++) {
                float ang = rando.getInRange(-FloatMath.PI, FloatMath.PI);
                float vel = rando.getInRange(maxvel/3, maxvel);
                float vx = FloatMath.cos(ang)*vel, vy = FloatMath.sin(ang)*vel;
                new Bullet(AsteroidsWorld.this, x, y, vx, vy, ang, now + 300/*ms*/);
            }
            // and destroy the target
            target.destroy();
        }

        @Override public void update (int delta) {
            now += delta;
            super.update(delta);
        }

        protected int _wave = -1;
        protected ImageLayer _msg;
    }

    abstract class Body extends Entity {
        public final AsteroidsWorld world;

        protected Body (AsteroidsWorld world, Component... comps) {
            super(world, comps);
            this.world = world;
        }

        @Override public String toString () {
            String name = getClass().getName();
            name = name.substring(name.lastIndexOf('$')+1);
            return name + "@" + world.pos.getX(id) + "/" + world.pos.getY(id);
        }
    }

    enum Size {
        TINY(20), SMALL(40), MEDIUM(60), LARGE(80);
        public final int size;
        Size (int size) { this.size = size; }
    };

    class Asteroid extends Body {
        public static final float MAXVEL = 0.02f;
        public static final float MAXSPIN = 0.001f;

        public final Size size;

        public Asteroid (AsteroidsWorld world, Size size, float x, float y) {
            this(world, size, x, y, world.rando.getInRange(-MAXVEL, MAXVEL),
                 world.rando.getInRange(-MAXVEL, MAXVEL));
        }

        public Asteroid (AsteroidsWorld world, Size size, float x, float y, float vx, float vy) {
            super(world, world.sprite, world.opos, world.pos, world.vel, world.spin, world.radius);

            this.size = size;

            float side = size.size;
            int iidx = world.rando.getInt(8);
            float ah = asteroids.height();
            ImageLayer layer = graphics().createImageLayer(asteroids.subImage(iidx*ah, 0, ah, ah));
            layer.setOrigin(ah/2, ah/2);
            layer.setScale(side/ah);
            layer.setRotation(world.rando.getFloat(MathUtil.TAU));

            world.sprite.set(id, layer);
            world.spin.set(id, world.rando.getInRange(-MAXSPIN, MAXSPIN));
            world.opos.set(id, x, y);
            world.pos.set(id, x, y);
            world.vel.set(id, vx, vy);
            world.radius.set(id, side*0.425f);
        }

        public void sunder (AsteroidsWorld world) {
            Size smaller;
            switch (size) {
            default:
            case TINY: world.explode(this, 4, 0.25f); return;
            case SMALL: smaller = Size.TINY; break;
            case MEDIUM: smaller = Size.SMALL; break;
            case LARGE: smaller = Size.MEDIUM; break;
            }
            float x = world.pos.getX(id), y = world.pos.getY(id);
            float vx = world.vel.getX(id), vy = world.vel.getY(id);
            // break the asteroid into two pieces, spinning in opposite directions and headed at
            // roughly right angles to the original
            new Asteroid(world, smaller, x, y, -vy, vx);
            new Asteroid(world, smaller, x, y, vy, -vx);
            destroy(); // and destroy ourself
        }
    }

    class Ship extends Body {
        public Ship (AsteroidsWorld world, float x, float y) {
            super(world, world.sprite, world.opos, world.pos, world.vel, world.spin, world.radius);

            CanvasImage bitmap = graphics().createImage(30, 20);
            Path path = bitmap.canvas().createPath();
            path.moveTo(0, 0).lineTo(30, 10).lineTo(0, 20).close();
            bitmap.canvas().setFillColor(0xFFCC99FF).fillPath(path);
            ImageLayer layer = graphics().createImageLayer(bitmap);
            layer.setOrigin(15, 10);
            layer.setRotation(-MathUtil.HALF_PI);

            world.sprite.set(id, layer);
            world.opos.set(id, x, y);
            world.pos.set(id, x, y);
            world.vel.set(id, 0, 0);
            world.radius.set(id, 10);
        }
    }

    class Bullet extends Body {
        public Bullet (AsteroidsWorld world, float x, float y, float vx, float vy, float angle,
                       int expires) {
            super(world, world.sprite, world.opos, world.pos, world.vel, world.radius,
                  world.expires);

            CanvasImage bitmap = graphics().createImage(5, 2);
            bitmap.canvas().setFillColor(0xFFFFFFFF).fillRect(0, 0, 5, 2);
            ImageLayer layer = graphics().createImageLayer(bitmap);
            layer.setOrigin(2.5f, 1);
            layer.setRotation(angle);

            world.sprite.set(id, layer);
            world.opos.set(id, x, y);
            world.pos.set(id, x, y);
            world.vel.set(id, vx, vy);
            world.radius.set(id, 2);
            world.expires.set(id, expires);
        }
    }

    @Override public void wasRemoved () {
        super.wasRemoved();
        _world = null;
        keyboard().setListener(null);
    }

    @Override public void update (int delta) {
        super.update(delta);
        if (_world != null) _world.update(delta);
    }

    @Override public void paint (Clock clock) {
        super.paint(clock);
        if (_world != null) _world.paint(clock);
    }

    @Override protected String name () {
        return "Asteroids";
    }

    @Override protected String title () {
        return "Asteroids Demo";
    }

    @Override protected Group createIface () {
        return new Group(AxisLayout.vertical()) {
            protected void layout () {
                super.layout();
                if (_world == null) {
                    _world = new AsteroidsWorld(layer, size().width(), size().height());
                    _world.attract();
                }
            }
        };
    }

    protected AsteroidsWorld _world;
    protected Group _group;

    protected static final TextConfig MSG_CFG = new TextConfig(0xFFFFFFFF).
        withFont(graphics().createFont("Helvetica", Font.Style.PLAIN, 24));
}
