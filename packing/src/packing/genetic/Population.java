package packing.genetic;

import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Population {
    // The size of the population.
    public static final int POPULATION_SIZE = 200;
    // %%explaination needed%%
    public static final boolean ELITIST = true;
    // The mutation rate for every mutation.
    public static final double MUTATION_RATE = 0.1;
    
    // For generating random values.
    private static Random random = new Random();
    
    // The used packer factory for generating packers.
    final private PackerFactory packerFactory;
    
    // List containing all instances of the population
    private List<Instance> instances;
    // The height that should be used in the evaluation of the instances.
    private int height;
    // The maximum width that should be used in the evaluation of the
    // instances.
    private int maxWidth = Integer.MAX_VALUE;
    // %%explaination needed%%
    private int target;
    // The best instance so far.
    private Instance best = null;
    
    /**
     * Instance class.
     * This class represents an individual of the population.
     */
    public class Instance
            implements Comparable<Instance>, packing.tools.Cloneable {
        // The dataset this instance represents.
        private Dataset dataset;
        // The fitness of the 
        private Double fitness = Double.NaN;
        
        /**
         * Creates a new instance of the provided dataset.
         * 
         * @param dataset the dataset to use.
         */
        public Instance(Dataset dataset) {
            this.dataset = dataset;
        }
        
        /**
         * Mutates the current instance.
         * 
         * %%I think that this part is incorrect%%.
         */
        public void mutate() {
            for (int i = 0; i < dataset.size(); i++) {
                if (random.nextDouble() < MUTATION_RATE) {
                    Dataset.Entry entry = dataset.get(i);
                    
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
        
        /**
         * Performs a crossover with the provided instance.
         * 
         * @param other instance that is used in the crossover.
         */
        public void crossover(Instance other) {
        }
        
        /**
         * Calculates the fitness of the instance.
         * 
         * @param packer the packer to use for calculating the fitness.
         */
        public void calculateFitness(Packer packer) {
            Dataset packed = packer.pack(this.dataset);
            fitness = (packed != null
                    ? 1.0 / (packed.getArea() - target + 1)
                    : 0.0);
            dataset = packed;
        }
        
        @Override
        public int compareTo(Instance o) {
            return Double.compare(this.fitness, o.fitness);
        }
        
        @Override
        public Instance clone() {
            return new Instance(dataset.clone());
        }
        
        /**
         * @return the fitness of this instance.
         */
        public double getFitness() {
            return fitness;
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
    private Population(List<Instance> instances, PackerFactory factory, int height) {
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
    public Population(Dataset dataset, PackerFactory factory, int height) {
        this(new ArrayList<>(POPULATION_SIZE), factory, height);
        instances.add(new Instance(dataset.clone()));
        
        while (instances.size() < POPULATION_SIZE) {
            Dataset clone = dataset.clone();
            clone.shuffle();
            clone.setRotation(Dataset.RANDOM_ROTATION);
            instances.add(new Instance(clone));
        }
    }
    
    /**
     * Calculates the fitness of all instances with the provided
     * height and maximum width.
     * If the instance if the best one seen so far, update best.
     */
    public void calculateFitness() {
        for (Instance instance : instances) {
            Packer packer = packerFactory.create(maxWidth, height);
            instance.calculateFitness(packer);
        }
        
        instances.sort(Collections.reverseOrder());
        best = instances.get(0);
    }
    /**
     * %%exlaination needed%%
     * @param fitnessSum %%exlaination needed%%
     * @return %%exlaination needed%%
     */
    public Instance selectParent(double fitnessSum) {
        double rand = random.nextDouble() * fitnessSum;
        double runningSum = 0;
        for (Instance instance : instances) {
            runningSum += instance.getFitness();
            if (runningSum > rand) {
                return instance.clone();
            }
        }
        throw new RuntimeException("Failed to select a parent. Rounding error?");
    }
    
    /**
     * Creates a selection
     */
    public void performSelection() {
        List<Instance> newInstances = new ArrayList<>(POPULATION_SIZE);
        
        // Calculates the total fitnessSum of the instances.
        double fitnessSum = 0;
        for (Instance instance : instances) {
            fitnessSum += instance.getFitness();
        }
        
        // %%exlaination needed%% Always true???
        if (ELITIST) {
            newInstances.add(instances.get(0).clone());
        }
        
        // %%exlaination needed%%
        while (newInstances.size() < POPULATION_SIZE / 2) {
            newInstances.add(selectParent(fitnessSum));
        }
        
        // %%exlaination needed%%
        while (newInstances.size() < POPULATION_SIZE) {
            Dataset dataset = best.dataset.clone();
            dataset.shuffle();
            dataset.setRotation(Dataset.RANDOM_ROTATION);
            newInstances.add(new Instance(dataset));
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
     * @return the maximum width of %%explaination needed%%.
     */
    public int getMaxWidth() {
        return maxWidth;
    }
    
    /**
     * Sets the maximum width of %%exlaination needed%%.
     * 
     * @param maxWidth the new maximum width.
     */
    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }
    
    /**
     * @return The setted target.
     */
    public int getTarget() {
        return target;
    }
    
    /**
     * Sets the target amount.
     * %%explaination needed%%
     * 
     * @param target 
     */
    public void setTarget(int target) {
        this.target = target;
    }
    
    /**
     * @return the dataset of the best instance of this population.
     */
    public Dataset getBest() {
        return best.dataset;
    }
}   
