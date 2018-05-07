/**
 * Given a perfect packing instance with assigned X-coordinates, calculate a solution
 * to assign the Y-coordinates.
 *
 * Used in the absolute placement approach.
 */
public class YCoordinatePacker extends Packer {
    @Override
    public Dataset pack(Dataset dataset) {
        /*
        Keep track of corners (starting with just (0,0) as the initial corner,
        and use a backtracking algorithm to fill rectangles with the correct
        X-coordinate.
         */
        return null;
    }
}
