package tripleplay.util;

import playn.core.ImageLayer;
import tripleplay.flump.Instance;
import tripleplay.flump.Movie;
import tripleplay.flump.Texture;

/**
 * Provides utility functions for dealing with Instances
 */
public class Instances
{
    public interface HierarchyListener
    {
        boolean onVisit (String parentLayer, Instance instance, int depth);
    }

    /**
     * Dump the display hierarchy to a String, each component on a newline, children indented
     * two spaces:
     *
     *   forearmRight: container_forearmRight
     *     TOP_tight: forearmRight_tight_TOP
     *       CB: Texture (59.0 x 138.0)
     *       SB: Texture (19.0 x 19.0)
     */
    public static String dumpHierarchy (Instance root)
    {
        final StringBuilder result = new StringBuilder();
        HierarchyListener listener = new HierarchyListener() {
            @Override public boolean onVisit (String parentLayer, Instance instance, int depth) {
                String instanceDesc;
                if (instance instanceof Movie) {
                    instanceDesc = ((Movie) instance).symbol().name();
                } else if (instance instanceof Texture) {
                    ImageLayer tLayer = ((Texture) instance).layer();
                    instanceDesc = "Texture (" + tLayer.width() + " x " + tLayer.height() + ")";
                    if (tLayer.destroyed()) {
                        instanceDesc += " (DESTROYED)";
                    }
                } else if (instance != null) {
                    instanceDesc = instance.toString();
                } else {
                    instanceDesc = "(null)";
                }
                printChild(depth, parentLayer + ": " + instanceDesc);
                return false;
            }
            protected void printChild (int depth, String description) {
                if (depth > 0) {
                    result.append("\n");
                }
                for (int ii = 0; ii < depth; ii ++) {
                    result.append("  ");
                }
                result.append(description);
            }
        };
        applyToHierarchy(root, listener);
        return result.toString();
    }


    /**
     * Call <code>listener.onVisit</code> for <code>root</code> and all its descendants.
     *
     * If <code>callback</code> returns <code>true</code>, traversal will halt.
     *
     * The passed in depth is 0 for <code>root</code>, and increases by 1 for each level of
     * children.
     *
     * @return <code>true</code> if <code>onVisit</code> returned <code>true</code>
     */
    public static boolean applyToHierarchy (Instance root, HierarchyListener listener)
    {
        return applyToHierarchy0("root", root, listener, 0);
    }

    /** Helper for applyToHierarchy */
    protected static boolean applyToHierarchy0 (String layerName,
        Instance root, HierarchyListener listener, int depth)
    {
        if (listener.onVisit(layerName, root, depth)) {
            return true;
        }

        if (root instanceof Movie) {
            Movie movie = (Movie) root;
            depth ++;
            for (String layer : movie.namedLayers().keySet()) {
                for (Instance instance : movie.getInstances(layer)) {
                    if (applyToHierarchy0(layer, instance, listener, depth)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
