//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2014, Three Rings Design, Inc. - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.demo.entity;

import java.util.Iterator;
import java.util.Random;

import playn.core.*;
import playn.scene.*;

import pythagoras.f.FloatMath;
import pythagoras.f.MathUtil;
import pythagoras.f.Point;
import pythagoras.f.Vector;

import react.Signal;
import react.Slot;
import react.UnitSlot;

import tripleplay.ui.Group;
import tripleplay.ui.Root;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.util.Randoms;
import tripleplay.util.StyledText;
import tripleplay.util.TextStyle;

import tripleplay.entity.Component;
import tripleplay.entity.Entity;
import tripleplay.entity.System;
import tripleplay.entity.World;

import tripleplay.demo.DemoScreen;

public class AsteroidsDemo extends DemoScreen
{
    public final Image asteroids = assets().getImage("images/asteroids.png");

    enum Size {
        TINY(20), SMALL(40), MEDIUM(60), LARGE(80);
        public final int size;
        Size (int size) { this.size = size; }
    }

    class AsteroidsWorld extends World {
        public final GroupLayer stage;
        public final float swidth, sheight;
        public final Randoms rando = Randoms.with(new Random());

        public static final int SHIP     = (1 << 0);
        public static final int ASTEROID = (1 << 1);
        public static final int BULLET   = (1 << 2);

        public final Component.IMask type = new Component.IMask(this);
        public final Component.XY opos = new Component.XY(this);
        public final Component.XY pos = new Component.XY(this);
        public final Component.XY vel = new Component.XY(this); // pixels/ms
        public final Component.Generic<Layer> sprite = new Component.Generic<Layer>(this);
        public final Component.Generic<Size> size = new Component.Generic<Size>(this);
        public final Component.FScalar spin = new Component.FScalar(this); // rads/ms
        public final Component.FScalar radius = new Component.FScalar(this);
        public final Component.IScalar expires = new Component.IScalar(this);

        public final Signal<Key> keyDown = Signal.create();
        public final Signal<Key> keyUp = Signal.create();

        public int now; // ms elapsed since world start, used by expirer/expires

        // handles updating entity position based on entity velocity
        public final System mover = new System(this, 0) {
            @Override protected void update (Clock clock, Entities entities) {
                Point p = _pos;
                Vector v = _vel;
                int delta = clock.dt;
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
                float alpha = clock.alpha;
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
                float dt = clock.dt;
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
            @Override protected void update (Clock clock, Entities entities) {
                int now = AsteroidsWorld.this.now;
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid = entities.get(ii);
                    if (expires.get(eid) <= now) world.entity(eid).close();
                }
            }

            @Override protected boolean isInterested (Entity entity) {
                return entity.has(expires);
            }
        };

        // checks for collisions (modeling everything as a sphere)
        public final System collider = new System(this, 1) {
            @Override protected void update (Clock clock, Entities entities) {
                // simple O(n^2) collision check; no need for anything fancy here
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid1 = entities.get(ii);
                    Entity e1 = world.entity(eid1);
                    if (e1.isDisposed()) continue;
                    pos.get(eid1, _p1);
                    float r1 = radius.get(eid1);
                    for (int jj = ii+1; jj < ll; jj++) {
                        int eid2 = entities.get(jj);
                        Entity e2 = world.entity(eid2);
                        if (e2.isDisposed()) continue;
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
                switch (type.get(e1.id) | type.get(e2.id)) {
                case SHIP_ASTEROID:
                    explode(type.get(e1.id) == SHIP ? e1 : e2, 10, 0.75f);
                    setMessage("Game Over. Press 's' to restart");
                    _wave = -1;
                    break;
                case BULLET_ASTEROID:
                    if (type.get(e1.id) == ASTEROID) {
                        sunder(e1);
                        e2.close();
                    } else {
                        sunder(e2);
                        e1.close();
                    }
                    break;
                // TODO: asteroid asteroid?
                default: break; // nada
                }
            }

            protected static final int SHIP_ASTEROID = SHIP|ASTEROID;
            protected static final int BULLET_ASTEROID = BULLET|ASTEROID;

            protected final Point _p1 = new Point(), _p2 = new Point();
        };

        // handles progression to next wave
        public final System waver = new System(this, 0) {
            @Override protected void update (Clock clock, Entities entities) {
                // if the only entity left is the player's ship; move to the next wave
                if (entities.size() == 1 && type.get(entities.get(0)) == SHIP) {
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
                    @Override public void onEmit (Key key) {
                        switch (key) {
                        case LEFT:  _angvel = -ROT;   break;
                        case RIGHT: _angvel =  ROT;   break;
                        case UP:    _accel  =  ACCEL; break;
                        case SPACE: if (_wave >=  0) fireBullet(); break;
                        case S:     if (_wave == -1) startWave(0); break;
                        default: break;
                        }
                    }
                });
                keyUp.connect(new Slot<Key>() {
                    @Override public void onEmit (Key key) {
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
                int sid = _ship.id;
                float ang = sprite.get(sid).rotation();
                float vx = vel.getX(sid), vy = vel.getY(sid);
                float bvx = vx+BULLET_VEL*FloatMath.cos(ang), bvy = vy+BULLET_VEL*FloatMath.sin(ang);
                createBullet(pos.getX(sid), pos.getY(sid), bvx, bvy, ang, now + BULLET_LIFE);
                vel.set(sid, vx-bvx/100, vy-bvy/100); // decrease ship's velocity a smidgen
            }

            @Override protected void update (Clock clock, Entities entities) {
                Vector v = _vel;
                for (int ii = 0, ll = entities.size(); ii < ll; ii++) {
                    int eid = entities.get(ii);
                    spin.set(eid, _angvel);
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
                _ship = entity;
            }

            @Override protected boolean isInterested (Entity entity) {
                return type.get(entity.id) == SHIP;
            }

            protected Vector _vel = new Vector();
            protected float _angvel, _accel;
            protected Entity _ship;
        };

        public AsteroidsWorld (GroupLayer stage, float swidth, float sheight) {
            this.stage = stage;
            this.swidth = swidth;
            this.sheight = sheight;

            closeOnHide(input().keyboardEvents.connect(new Keyboard.KeySlot() {
                @Override public void onEmit (Keyboard.KeyEvent event) {
                    (event.down ? keyDown : keyUp).emit(event.key);
                }
            }));
        }

        public void setMessage (String text) {
            if (_msg != null) _msg.close();
            if (text != null) {
                _msg = StyledText.span(graphics(), text, MSG_STYLE).toLayer();
                _msg.setDepth(1);
                stage.addAt(_msg, (swidth-_msg.width())/2, (sheight-_msg.height())/2);
            }
        }

        public void attract () {
            for (int ii = 0; ii < 5; ii++) createAsteroid(
                Size.LARGE, rando.getFloat(swidth), rando.getFloat(sheight));
            setMessage("Press 's' to start.");
            _wave = -1;
        }

        public void startWave (int wave) {
            // if this is wave 0, destroy any existing entities and add our ship
            if (wave == 0) {
                Iterator<Entity> iter = entities();
                while (iter.hasNext()) iter.next().close();
                createShip(swidth/2, sheight/2);
                setMessage(null);
            }
            for (int ii = 0, ll = Math.min(10, wave+2); ii < ll; ii++) {
                float x = rando.getFloat(swidth), y = rando.getFloat(sheight);
                // TODO: make sure x/y doesn't overlap ship
                createAsteroid(Size.LARGE, x, y);
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
                createBullet(x, y, vx, vy, ang, now + 300/*ms*/);
            }
            // and destroy the target
            target.close();
        }

        @Override public void update (Clock clock) {
            now += clock.dt;
            super.update(clock);
        }

        protected String typeName (int id) {
            switch (type.get(id)) {
            case     SHIP: return "ship";
            case   BULLET: return "bullet";
            case ASTEROID: return "asteroid";
            default:       return "unknown:" + type.get(id);
            }
        }

        protected String toString (int id) {
            return typeName(id) + ":" + id + "@" + pos.getX(id) + "/" + pos.getY(id);
        }

        protected Entity createShip (float x, float y) {
            Entity ship = create(true);
            ship.add(type, sprite, opos, pos, vel, spin, radius);

            Canvas canvas = graphics().createCanvas(30, 20);
            Path path = canvas.createPath();
            path.moveTo(0, 0).lineTo(30, 10).lineTo(0, 20).close();
            canvas.setFillColor(0xFFCC99FF).fillPath(path);
            ImageLayer layer = new ImageLayer(canvas.toTexture());
            layer.setOrigin(15, 10);
            layer.setRotation(-MathUtil.HALF_PI);

            int id = ship.id;
            type.set(id, SHIP);
            sprite.set(id, layer);
            opos.set(id, x, y);
            pos.set(id, x, y);
            vel.set(id, 0, 0);
            radius.set(id, 10);
            return ship;
        }

        protected static final float MAXVEL = 0.02f;
        protected static final float MAXSPIN = 0.001f;

        protected Entity createAsteroid (Size size, float x, float y) {
            return createAsteroid(size, x, y, rando.getInRange(-MAXVEL, MAXVEL),
                                  rando.getInRange(-MAXVEL, MAXVEL));
        }

        protected Entity createAsteroid (Size sz, float x, float y, float vx, float vy) {
            Entity ast = create(true);
            ast.add(type, size, sprite, opos, pos, vel, spin, radius);

            float side = sz.size;
            int iidx = rando.getInt(8);
            float ah = asteroids.height();
            ImageLayer layer = new ImageLayer(asteroids.region(iidx*ah, 0, ah, ah));
            layer.setOrigin(ah/2, ah/2);
            layer.setScale(side/ah);
            layer.setRotation(rando.getFloat(MathUtil.TAU));

            int id = ast.id;
            type.set(id, ASTEROID);
            size.set(id, sz);
            sprite.set(id, layer);
            spin.set(id, rando.getInRange(-MAXSPIN, MAXSPIN));
            opos.set(id, x, y);
            pos.set(id, x, y);
            vel.set(id, vx, vy);
            radius.set(id, side*0.425f);
            return ast;
        }

        protected Entity createBullet (float x, float y, float vx, float vy, float angle, int exps) {
            Entity bullet = create(true);
            bullet.add(type, sprite, opos, pos, vel, radius, expires);

            Canvas canvas = graphics().createCanvas(5, 2);
            canvas.setFillColor(0xFFFFFFFF).fillRect(0, 0, 5, 2);
            ImageLayer layer = new ImageLayer(canvas.toTexture());
            layer.setOrigin(2.5f, 1);
            layer.setRotation(angle);

            int id = bullet.id;
            type.set(id, BULLET);
            sprite.set(id, layer);
            opos.set(id, x, y);
            pos.set(id, x, y);
            vel.set(id, vx, vy);
            radius.set(id, 2);
            expires.set(id, exps);
            return bullet;
        }

        protected void sunder (Entity ast) {
            Size smaller;
            switch (size.get(ast.id)) {
            default:
            case TINY: explode(ast, 4, 0.25f); return;
            case SMALL: smaller = Size.TINY; break;
            case MEDIUM: smaller = Size.SMALL; break;
            case LARGE: smaller = Size.MEDIUM; break;
            }
            float x = pos.getX(ast.id), y = pos.getY(ast.id);
            float vx = vel.getX(ast.id), vy = vel.getY(ast.id);
            // break the asteroid into two pieces, spinning in opposite directions and headed at
            // roughly right angles to the original
            createAsteroid(smaller, x, y, -vy, vx);
            createAsteroid(smaller, x, y, vy, -vx);
            ast.close(); // and destroy ourself
        }

        protected int _wave = -1;
        protected ImageLayer _msg;
    }

    @Override protected String name () {
        return "Asteroids";
    }

    @Override protected String title () {
        return "Asteroids Demo";
    }

    @Override protected Group createIface (Root root) {
        // create a new world once our root is validated so that we know our size
        root.validated.connect(new UnitSlot() {
            public void onEmit () {
                AsteroidsWorld world = new AsteroidsWorld(layer, size().width(), size().height());
                closeOnHide(world.connect(update, paint));
                world.attract();
            }
        }).once();;
        return new Group(AxisLayout.vertical());
    }

    protected AsteroidsWorld _world;
    protected Group _group;

    protected static final TextStyle MSG_STYLE = TextStyle.DEFAULT.
        withTextColor(0xFFFFFFFF).withFont(new Font("Helvetica", 24));
}
