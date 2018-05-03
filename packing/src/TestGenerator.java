
// Java imports
import java.awt.Rectangle;


public class TestGenerator extends Generator {
    private boolean stopped = false;
    
    @Override
    public Dataset generate(Dataset dataset) {
        int width = 0,
            height = 0,
            minWidth = 0,
            minHeight = 0,
            minArea = 0;
        for (Dataset.Entry entry : dataset) {
            Rectangle rect = entry.getRec();
            width += rect.width;
            minWidth = Math.max(minWidth, rect.width);
            minHeight = Math.max(minHeight, rect.height);
            minArea += rect.width * rect.height;
        }
        height = minHeight;

        Dataset best = null;

        while (!stopped && width >= minWidth) {
            if (width * height < minArea) {
                ++height;
                continue;
            } else if (best != null && width * height > best.getArea()) {
                --width;
                continue;
            }
//            System.out.printf("Packing into [%d x %d] bounding box\n", width, height);
            Packer packer = new Sheet(width, height);
            Dataset packed = packer.pack(dataset);

            if (packed != null) {
                if (best == null || packed.getArea() < best.getArea()) {
                    best = packed;
                }
                --width;
            } else {
                ++height;
            }
        }
        return best;
    }
    
    @Override
    public void interrupt() {
        //System.err.println("interrupted!");
        stopped = true;
    }
    
}