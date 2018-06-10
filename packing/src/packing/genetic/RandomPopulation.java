
package packing.genetic;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;


//##########
// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/**
 * Class description here.
 */
public class RandomPopulation
        extends Population {
    // The size of the population.
    public static final int POPULATION_SIZE = 200;

    // The mutation rate for every mutation.
    public static final double MUTATION_RATE = 0.1;
    
    // For generating random values.
    private static Random random = new Random();
    
    // The used packer factory for generating packers.
    final private PackerFactory packerFactory;
    
    // List containing all instances of the population
    private List<RandomInstance> instances;
    // The height that should be used in the evaluation of the instances.
    private int height;
    // The maximum width that should be used in the evaluation of the
    // instances.
    private int maxWidth = Integer.MAX_VALUE;
    // The best instance so far.
    private RandomInstance best = null;
    
    /**
     * Instance class.
     * This class represents an individual of the population.
     */
    public class RandomInstance
            extends Population.Instance<RandomInstance> {
        // The dataset this instance represents.
        private Dataset dataset;
        // The fitness of the dataset
        private Double fitness = Double.NaN;
        
        /**
         * Creates a new instance of the provided dataset.
         * 
         * @param dataset the dataset to use.
         */
        public RandomInstance(Dataset dataset) {
            this.dataset = dataset;
        }
        
        @Override
        public void mutate() {
            for (int i = 0; i < dataset.size(); i++) {
                if (random.nextDouble() < MUTATION_RATE) {
                    CompareEntry entry = dataset.get(i);
                    
                    if (dataset.allowRotation() && random.nextBoolean()) {
                        entry.setRotation(entry.useRotation());
                        
                    } else {
                        int j = random.nextInt(dataset.size() - 1);
                        if (j >= i) j++;
                        
                        dataset.swap(i, j);
                    }
                }
            }
        }
        
        @Override
        public void crossover(RandomInstance other) {
        }
        
        @Override
        public void calculateFitness(Packer packer) {
            Dataset packed = packer.pack(this.dataset);
            fitness = (packed != null
                    ? 1.0 / (packed.getWidth())
                    : 0.0);
            dataset = packed;
        }
        
        @Override
        public int compareTo(RandomInstance o) {
            return Double.compare(this.fitness, o.fitness);
        }
        
        @Override
        public RandomInstance clone() {
            return new RandomInstance(dataset.clone());
        }
        
        /**
         * @return the fitness of this instance.
         */
        public double getFitness() {
            return fitness;
        }

        public String toString() {
            return String.format("<Instance %f, %s>", fitness, dataset);
        }
    }


    /**
     * Craetes a new population with the given instances, packer factory and
     * height.
     * 
     * @param instances the instances used for the population.
     * @param factory the factory for creating a packer.
     * @param height the height the sheet must have.
     */
    private RandomPopulation(List<RandomInstance> instances,
            PackerFactory factory, int height) {
        this.instances = instances;
        this.packerFactory = factory;
        this.height = height;
    }
    
    /**
     * Creates a new population from the given dataset, using the provided
     * packer factory and height.
     * 
     * @param dataset the dataset to generate the population for.
     * @param factory the factory for creating a packer.
     * @param height the height the sheet must have.
     */
    public RandomPopulation(Dataset dataset, PackerFactory factory,
            int height) {
        this(new ArrayList<>(POPULATION_SIZE), factory, height);
        instances.add(new RandomInstance(dataset.clone()));
        
        while (instances.size() < POPULATION_SIZE) {
            Dataset clone = dataset.clone();
            clone.shuffle();
            clone.setRotation(CompareEntry.RANDOM_ROTATION);
            instances.add(new RandomInstance(clone));
        }
    }
    
    /**
     * Calculates the fitness of all instances with the provided
     * height and maximum width.
     * If the instance if the best one seen so far, update best.
     */
    public void calculateFitness() {
        for (RandomInstance instance : instances) {
            Packer packer = packerFactory.create(maxWidth, height);
            instance.calculateFitness(packer);
        }
        
        instances.sort(Collections.reverseOrder());
        best = instances.get(0);
    }
    /**
     * Select a parent from the previous generation, with probability
     * proportional to the inverse of its rank.
     *
     * @param fitnessSum the sum of all fitness scores from the previous generation
     * @return a randomly selected instance from the previous generation
     */
    public RandomInstance selectParent(double fitnessSum) {
        double rand = random.nextDouble() * fitnessSum;
        double runningSum = 0;
        for (int i = 0; i < instances.size(); i++) {
            runningSum += 1.0 / (i + 1);
            if (runningSum > rand) {
                return instances.get(i).clone();
            }
        }
        throw new RuntimeException("Failed to select a parent. Rounding error?");
    }
    
    /**
     * Perform selection to create the next generation. Use an elitist strategy
     * (i.e. always keep the best instance from the previous generation). Then
     * randomly select parents from the previous generation with probability
     * proportional to the inverse of its rank.
     */
    public void performSelection() {
        List<RandomInstance> newInstances = new ArrayList<>(POPULATION_SIZE);
        
        // Calculates the total fitnessSum of the instances.
        double fitnessSum = 0;
        for (int i = 0; i < instances.size(); i++) {
            fitnessSum += 1.0 / (i + 1);
        }

        // Always keep the best instance from the previous generation.
        newInstances.add(instances.get(0).clone());

        // Select instances to create new generation
        while (newInstances.size() < POPULATION_SIZE) {
            newInstances.add(selectParent(fitnessSum));
        }
        
        instances = newInstances;
    }
    
    /**
     * Performs a mutation for all instances.
     */
    public void performMutation() {
        // Mutate all instances except the first/best
        for (int i = 1; i < instances.size(); i++) {
            instances.get(i).mutate();
        }
    }

    /**
     * @return the maximum width used for the packer when packing an instance.
     */
    public int getMaxWidth() {
        return maxWidth;
    }

    /**
     * Sets the maximum width of a solution. Permutations that don't fit within
     * this width get a fitness of 0 and are subsequently discarded.
     *
     * @param maxWidth the new maximum width.
     */
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
    
    /**
     * @return the dataset of the best instance of this population.
     */
    public Dataset getBest() {
        return best.dataset;
    }
}   
