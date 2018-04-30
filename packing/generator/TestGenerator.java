
package packing.generator;


// Packing imports
import packing.data.Dataset;


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
        System.out.println("it works");
        return dataset;
    }
    
    @Override
    public void interrupt() {
        stopped = true;
    }
    
}