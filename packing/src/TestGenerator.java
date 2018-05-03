
// Java imports
import java.awt.Rectangle;


public class TestGenerator extends Generator {
    private boolean stopped = false;
    
    @Override
    public Dataset generate(Dataset dataset) {
        Sheet sheet = new Sheet(new Rectangle(100, 50));
        
        for (Dataset.Entry entry : dataset) {
            sheet.add(entry);
            if (stopped) return dataset;
        }
        dataset.setSize(100, 50);
        return dataset;
    }
    
    @Override
    public void interrupt() {
        //System.err.println("interrupted!");
        stopped = true;
    }
    
}