//
// Triple Play - utilities for use in PlayN-based games
// Copyright (c) 2011-2018, Triple Play Authors - All rights reserved.
// http://github.com/threerings/tripleplay/blob/master/LICENSE

package tripleplay.ui;

import org.junit.Test;
import static org.junit.Assert.*;

import react.Signal;

import pythagoras.f.IDimension;

import playn.core.*;
// import playn.java.JavaPlatform;
import tripleplay.ui.layout.AxisLayout;
import tripleplay.ui.layout.BorderLayout;

public class ElementTest
{
    // static {
    //     JavaPlatform.Config config = new JavaPlatform.Config();
    //     config.headless = true;
    //     JavaPlatform.register(config);
    // }

    static class StubGraphics extends Graphics
    {
        StubGraphics (Platform plat) {
            super(plat, null, null);
        };

        public void setScale (Scale scale) {
            scaleChanged(scale);
        }

        public IDimension screenSize () { return null; }
        protected Canvas createCanvasImpl (Scale scale, int pw, int ph) { return null; }
        public Path createPath () { return null; }
        public Gradient createGradient (Gradient.Config config) { return null; }
        public TextLayout layoutText (String text, TextFormat format) { return null; }
        public TextLayout[] layoutText (String text, TextFormat format, TextWrap wrap) { return null; }
    }

    public static Platform stub = new StubPlatform() {
        Graphics gfx = new StubGraphics(this);

        public Graphics graphics () {
            return gfx;
        }
    };

    public final Signal<Clock> frame = Signal.create();

    static class TestGroup extends Group
    {
        public int added, removed;

        public TestGroup () {
            super(AxisLayout.vertical());
        }

        public void assertAdded (int count) {
            assertTrue(added == count && removed == count - 1);
        }

        public void assertRemoved (int count) {
            assertTrue(removed == count && added == count);
        }

        @Override protected void wasAdded () {
            super.wasAdded();
            added++;
        }

        @Override protected void wasRemoved () {
            super.wasRemoved();
            removed++;
        }
    }

    Interface iface = new Interface(stub, frame);

    Root newRoot () {
        return iface.createRoot(AxisLayout.vertical(), Stylesheet.builder().create());
    }

    /** Tests the basic functionality of adding and removing elements and that the wasAdded
     * and wasRemoved members are called as expected. */
    @Test public void testAddRemove () {
        Root root = newRoot();

        TestGroup g1 = new TestGroup(), g2 = new TestGroup();
        g1.assertRemoved(0);

        root.add(g1);
        g1.assertAdded(1);

        g1.add(g2);
        g1.assertAdded(1);
        g2.assertAdded(1);

        g1.remove(g2);
        g1.assertAdded(1);
        g2.assertRemoved(1);

        root.remove(g1);
        g1.assertRemoved(1);
        g2.assertRemoved(1);

        g1.add(g2);
        g1.assertRemoved(1);
        g2.assertRemoved(1);

        root.add(g1);
        g1.assertAdded(2);
        g2.assertAdded(2);
    }

    /** Tests that a group may add a child into another group whilst being removed and that the
     * child receives the appropriate calls to wasAdded and wasRemoved. Similarly tests for
     * adding the child during its own add. */
    @Test public void testChildTransfer () {

        class Pa extends TestGroup
        {
            TestGroup child1 = new TestGroup();
            TestGroup child2 = new TestGroup();
            TestGroup brother = new TestGroup();

            @Override protected void wasRemoved () {
                // hand off the children to brother
                brother.add(child1);
                super.wasRemoved();
                brother.add(child2);
            }

            @Override protected void wasAdded () {
                // steal the children back from brother
                add(child1);
                super.wasAdded();
                add(child2);
            }
        }

        Root root = newRoot();
        Pa pa = new Pa();
        pa.assertRemoved(0);

        root.add(pa);
        pa.assertAdded(1);
        pa.child1.assertAdded(1);
        pa.child2.assertAdded(1);

        root.remove(pa);
        pa.assertRemoved(1);
        pa.child1.assertRemoved(1);
        pa.child2.assertRemoved(1);

        root.add(pa.brother);
        pa.child1.assertAdded(2);
        pa.child2.assertAdded(2);

        root.add(pa);
        pa.assertAdded(2);
        pa.child1.assertAdded(3);
        pa.child2.assertAdded(3);

        root.remove(pa);
        pa.assertRemoved(2);
        pa.child1.assertAdded(4);
        pa.child2.assertAdded(4);
    }

    /** Tests that a group may add a grandchild into another group whilst being removed and that
     * the grandchild receives the appropriate calls to wasAdded and wasRemoved. Similarly tests
     * for adding the grandchild during its own add. */
    @Test public void testGrandchildTransfer () {

        class GrandPa extends TestGroup
        {
            TestGroup child = new TestGroup();
            TestGroup grandchild1 = new TestGroup();
            TestGroup grandchild2 = new TestGroup();
            TestGroup brother = new TestGroup();

            GrandPa () {
                add(child);
            }

            @Override protected void wasRemoved () {
                brother.add(grandchild1);
                super.wasRemoved();
                brother.add(grandchild2);
            }

            @Override protected void wasAdded () {
                child.add(grandchild1);
                super.wasAdded();
                child.add(grandchild2);
            }
        }

        Root root = newRoot();
        GrandPa pa = new GrandPa();
        pa.assertRemoved(0);

        root.add(pa);
        pa.assertAdded(1);
        pa.grandchild1.assertAdded(1);
        pa.grandchild2.assertAdded(1);

        root.remove(pa);
        pa.assertRemoved(1);
        pa.grandchild1.assertRemoved(1);
        pa.grandchild2.assertRemoved(1);

        root.add(pa.brother);
        pa.grandchild1.assertAdded(2);
        pa.grandchild2.assertAdded(2);

        root.add(pa);
        pa.assertAdded(2);
        pa.grandchild1.assertAdded(3);
        pa.grandchild2.assertAdded(3);

        root.remove(pa);
        pa.assertRemoved(2);
        pa.grandchild1.assertAdded(4);
        pa.grandchild2.assertAdded(4);
    }

    private final static float PIXEL_EPSILON = .05f;

    /** Tests alignment of element boundaries to physical pixel coordinates. */
    @Test public void testPixelCoordinates() {
        float factor = 1.333f;
        int nitems = 10;
        int size = 100;

        Scale scale = new Scale(factor);
        ((StubGraphics) stub.graphics()).setScale(scale);

        Root root = newRoot();

        Group g = new Group(AxisLayout.horizontal().gap(0));
        Element els[] = new Element[nitems];
        for (int i = 0; i < nitems; i++) {
            g.add(els[i] = new Shim(size, size));
        }
        root.add(g).pack().validate();

        for (int i = 0; i < nitems; i++) {
            // Check that the virtual coordinates of the first and last pixel
            // of each element map to integer physical pixel coordinates
            float start = scale.scaled(els[i].x());
            float end   = scale.scaled(els[i].x() + els[i].size().width()) - 1;
            assertEquals(Math.round(start), start, PIXEL_EPSILON);
            assertEquals(Math.round(end), end, PIXEL_EPSILON);

            // Check that there are no gaps and no overlapping pixels between
            // adjacent components
            if (i != nitems - 1) {
                float next  = scale.scaled(els[i + 1].x());
                assertEquals(end + 1, next, PIXEL_EPSILON);
            }
        }
    }

    @Test public void testPixelCoordinates2() {

        ((StubGraphics) stub.graphics()).setScale(new Scale(1f));

        class MyShim extends Shim {
            MyShim() {
                super(100, 55);
            }

            void assertCoordsAligned() {
                float x = x(), y = y();
                assertEquals(Math.round(x), x, PIXEL_EPSILON);
                assertEquals(Math.round(y), y, PIXEL_EPSILON);
            }
        }

        MyShim els[] = new MyShim[] { new MyShim(), new MyShim() };
        Group g = new Group(AxisLayout.horizontal().gap(5)).add(els);

        Root root = iface.createRoot(new BorderLayout(), Stylesheet.builder().create()).setSize(300, 100);
        root.add(g.setConstraint(BorderLayout.CENTER)).validate();

        // Check that both elements are initially aligned to physical pixel coordinates
        for (MyShim el : els) {
            el.assertCoordsAligned();
        }

        // Invalidate one of the elements, and trigger a revalidation
        els[0].invalidate();
        root.validate();

        // Check that both elements are still aligned
        for (MyShim el : els) {
            el.assertCoordsAligned();
        }
    }
}
