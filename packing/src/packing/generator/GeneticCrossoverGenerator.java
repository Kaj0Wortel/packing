
package packing.generator;

// Packing imports
import packing.data.Dataset;
import packing.genetic.CrossoverPopulation;
import packing.genetic.Population;
import packing.packer.Packer;
import packing.packer.PackerFactory;


//##########


/**
 * Generator for the genetic solver with crossover.
 */
public class GeneticCrossoverGenerator
        extends Generator {
    
    public GeneticCrossoverGenerator(PackerFactory pf) {
        super(pf);
    }
    
    
    @Override
    public void generateSolution(Dataset dataset) {
        generateUpperBound(dataset);
        
        for (int i = 4; i < Runtime.getRuntime().availableProcessors(); i++) {
            createRunnable(dataset.clone()).run();
        }
        
        createRunnable(dataset).run();
    }
    
    /**
     * 
     * @param dataset
     * @return 
     */
    private Runnable createRunnable(Dataset dataset) {
        return () -> {
            Population pop = new CrossoverPopulation(dataset);
            
            while (true) {
                pop.calculateFitness();
                pop.performSelection();
                pop.performMutation();
                updateBest(pop.getBest());
            }
        };
    }
    
    /**
     * Updates the best dataset if a new best was found.
     * 
     * @param dataset 
     */
    private void updateBest(Dataset dataset) {
        synchronized(best) {
            if (dataset.getArea() < best.getArea()) {
                best = dataset;
            }
        }
    }
    
}
