
package packing.data;

import java.awt.Rectangle;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;
import java.util.function.Predicate;

public abstract class CompareEntry {
    /**-------------------------------------------------------------------------
     * Variables.
     * -------------------------------------------------------------------------
     */
    // Random used for calculations.
    final protected static Random random = new Random();
    
    // The number denoting the order of occurance in the input.
    final protected int id;
    
    
    /**-------------------------------------------------------------------------
     * Comparators and predicates.
     * -------------------------------------------------------------------------
     */
    // Do not rotate rectangles
    final public static Predicate<CompareEntry> NO_ROTATION = entry -> false;
    
    // Rotate rectangles randomly
    final public static Predicate<CompareEntry> RANDOM_ROTATION = entry -> random.nextBoolean();
    
    // Rotate rectangles so their longest side is vertical
    final public static Predicate<CompareEntry> LONGEST_SIDE_VERTIAL = entry -> entry.getRec().width > entry.getRec().height;
    
    // Sort rectangles by decreasing height
    final public static Comparator<CompareEntry> SORT_HEIGHT = Collections.reverseOrder(
            Comparator.comparingInt((CompareEntry entry) -> entry.getRec().height)
                    .thenComparing((CompareEntry entry) -> entry.getRec().width)
    );
    
    // Sort rectangles by decreasing area
    final public static Comparator<CompareEntry> SORT_AREA = Collections.reverseOrder(
            Comparator.comparingInt((CompareEntry entry) -> entry.getRec().height * entry.getRec().width)
                    .thenComparing((CompareEntry entry) -> entry.getRec().height)
    );
    
    // Sort rectangles by decreasing width
    final public static Comparator<CompareEntry> SORT_WIDTH = Collections.reverseOrder(
            Comparator.comparingInt((CompareEntry entry) -> entry.getRec().width)
                    .thenComparing((CompareEntry entry) -> entry.getRec().height)
    );
    
    // Sort rectangles by the length of their longest side, decreasing
    final public static Comparator<CompareEntry> SORT_LONGEST_SIDE = Collections.reverseOrder(
            Comparator.comparingInt((CompareEntry entry) -> Math.max(entry.getRec().height, entry.getRec().width))
                    .thenComparing((CompareEntry entry) -> Math.min(entry.getRec().height, entry.getRec().width))
    );
    
    // Sort rectangles by id, ascending
    final public static Comparator<CompareEntry> SORT_ID = Comparator.comparingInt(entry -> entry.id);
    
    
    /**-------------------------------------------------------------------------
     * Costructor
     * -------------------------------------------------------------------------
     */
    public CompareEntry(int id) {
        this.id = id;
    }
    
    /**-------------------------------------------------------------------------
     * Functions
     * -------------------------------------------------------------------------
     */
    /**
     * @return the rectangle depending on the default rotation.
     */
    public abstract Rectangle getRec();
    
}
