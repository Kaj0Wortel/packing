
package packing.generator;

import packing.data.Dataset;
import packing.genetic.Population;
import packing.packer.Packer;
import packing.packer.PackerFactory;

import java.awt.*;
import java.util.Arrays;
import java.util.Comparator;

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
        dataset.setRotation(Dataset.LONGEST_SIDE_VERTIAL);

        int height = dataset.getHeight();
        int width;
        int minWidth = 0;
        int minArea = 0;

        for (Dataset.Entry entry : dataset) {
            Rectangle rect = entry.getRec();
            minArea += rect.width * rect.height;
            minWidth = Math.max(minWidth, rect.width);
        }

        if (minArea % height != 0) {
            minArea = minArea - (minArea % height) + height;
        }

        best = generateUpperBound(dataset);
        width = best.getWidth();

        System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", best.getWidth(), best.getHeight(),
                100 * (best.getArea() - minArea) / (double) best.getArea());

        Population population = new Population(best, packerFactory, dataset.getHeight());
//        population.setMaxWidth(width);
        population.setTarget(minArea);

        Dataset current;

        while (best.getArea() > minArea && width > minWidth) {
            population.calculateFitness();
            current = population.getBest();

            if (current.getArea() < best.getArea()) {
                    System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", current.getWidth(), current.getHeight(),
                            100 * (current.getArea() - minArea) / (double) current.getArea());
                    best = current;
            }

            population.performSelection();
            population.performMutation();
        }
    }
    
}
