package tripleplay.ui;

import org.junit.Test;
import static org.junit.Assert.*;

import playn.java.JavaPlatform;
import tripleplay.ui.layout.AxisLayout;

public class ElementTest
{
    static {
        JavaPlatform.Config config = new JavaPlatform.Config();
        config.headless = true;
        JavaPlatform.register(config);
    }

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

    Interface iface = new Interface();

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
}
