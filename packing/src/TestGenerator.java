
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

        dataset.setSize(width, height);

        Dataset best = dataset;
        while (!stopped && minWidth <= width) {
            if (width * height < minArea) {
                ++height;
                continue;
            } else if (width * height > best.getWidth() * best.getHeight()) {
                --width;
                continue;
            }

            Dataset clone = dataset.clone();
            clone.setSize(width, height);
            Sheet sheet = new Sheet(new Rectangle(width, height));

            boolean fit = true;

            for (Dataset.Entry entry : clone) {
                if (!sheet.add(entry)) {
                    fit = false;
                    break;
                }
                if (stopped) return best;
            }

            if (fit) {
                best = clone;
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