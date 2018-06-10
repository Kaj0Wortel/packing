
package packing.generator;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.genetic.RandomPopulation;
import packing.packer.PackerFactory;


//##########
// Java imports
import java.awt.Rectangle;


/**
 * Generator for the genetic variant.
 */
public class GeneticGenerator
        extends Generator {
    
    public GeneticGenerator(PackerFactory factory) {
        super(factory);
    }
    
    @Override
    public void generateSolution(Dataset dataset) {
        // %%explaination needed%%
        dataset.setRotation(CompareEntry.LONGEST_SIDE_VERTIAL);
        
        int height = dataset.getHeight();
        int width;
        int minWidth = 0;
        int minArea = 0;
        int generation = 0;
        
        for (CompareEntry entry : dataset) {
            Rectangle rect = entry.getRec();
            minArea += rect.width * rect.height;
            minWidth = Math.max(minWidth, rect.width);
        }
        
        if (minArea % height != 0) {
            minArea = minArea - (minArea % height) + height;
        }
        
        best = generateUpperBound(dataset);
        width = best.getWidth();
        
        //System.err.printf("Found initial solution: [%d x %d] (%.5f%% wasted space)\n", best.getWidth(), best.getHeight(),
        //        100 * (best.getArea() - minArea) / (double) best.getArea());
        
        RandomPopulation population = new RandomPopulation(
                best, packerFactory, dataset.getHeight());
        
        Dataset current;
        
        try {
            while (best.getArea() > minArea && width > minWidth) {
                generation++;
                population.calculateFitness();
                current = population.getBest();
                
                if (current.getArea() < best.getArea()) {
                    //System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", current.getWidth(), current.getHeight(),
                    //        100 * (current.getArea() - minArea) / (double) current.getArea());
                    best = current;
                }
                
                population.performSelection();
                population.performMutation();
            }
        } finally {
            //System.err.printf("Generated %d generations...\n", generation);
        }
    }
    
}
