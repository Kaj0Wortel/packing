/**
 * Transforms a dataset into a perfect packing instance, such that any
 * solution perfectly fills in the bounding box.
 *
 * Used in the absolute placement approach.
 */
public class PerfectPackingTransformer extends Packer {
    private Packer yPacker;

    public PerfectPackingTransformer(Packer packer) {
        this.yPacker = packer;
    }
    @Override
    public Dataset pack(Dataset dataset) {
        /*
        Clone the dataset, then add 1x1 rectangles until the total area
        of the rectangles is equal to the total are of the bounding box.
         */
        return null;
    }
}
