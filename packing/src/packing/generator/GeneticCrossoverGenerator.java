
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
        Population pop = new CrossoverPopulation(dataset);
        
        while (true) {
            pop.calculateFitness();
            pop.performSelection();
            pop.performMutation();
            best = pop.getBest();
        }
    }
}
