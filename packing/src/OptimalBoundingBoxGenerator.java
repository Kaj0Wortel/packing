/**
 * Generates bounding boxes by increasing area, starting at a lower bound
 * for the Dataset instance. When used with an optimal packer, this ensures
 * the final solution is optimal.
 */
public class OptimalBoundingBoxGenerator extends Generator {
    @Override
    public Dataset generate(Dataset dataset) {
        /*
        Calculate the total area of the rectangles as a lower bound.
        Try packing the rectangles into every possible bounding box of
        that size, increasing the area if it doesn't fit. Return when a
        solution is found.

        See Algorithms Overview for ways to generate the bounding boxes.
         */
        return null;
    }

    @Override
    public void interrupt() {

    }
}
