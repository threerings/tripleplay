package tripleplay.flump;

/**
 * Defines a Flump symbol.
 */
public interface Symbol
{
    /**
     * The exported name of this symbol.
     */
    String name ();

    /**
     * Create a new instance of this symbol.
     */
    Instance createInstance ();
}
