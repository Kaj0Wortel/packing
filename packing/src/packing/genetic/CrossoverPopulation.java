
package packing.genetic;


//##########
// Java imports
import java.util.LinkedList;
import java.util.List;
import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;

/**
 * Populatin supporting crossover by using polish-notation.
 */
public class CrossoverPopulation
        extends Population {
    // The size of the population.
    public static final int POPULATION_SIZE = 200;
    
    // The mutation rate for every mutation.
    public static final double MUTATION_RATE = 0.1;
    
    protected List<CrossInstance> list = new LinkedList<>();
    
    /**
     * Class representing an instance of the population.
     */
    public class CrossInstance
            extends Population.Instance<CrossInstance> {
        
        @Override
        public void crossover(CrossInstance other) {
            
        }
        
        @Override
        public void mutate() {
            
        }
        
        @Override
        public void calculateFitness(Packer packer) {
            
        }
        
        @Override
        public int compareTo(CrossInstance o) {
            return 0;
        }
        
        @Override
        public CrossInstance clone() {
            return null;
        }
        
    }
    
    
    public CrossoverPopulation(Dataset dataset, int height) {
        
    }
    
}
