
public class TestGenerator extends Generator {
    private boolean stopped = false;
    
    @Override
    public Dataset generate(Dataset dataset) {
        try {
            while (!stopped) {
                Thread.sleep(10);
            }
            
        } catch (InterruptedException e) {
            
        }
        
        return dataset;
    }
    
    @Override
    public void interrupt() {
        System.err.println("interrupted!");
        stopped = true;
    }
    
}