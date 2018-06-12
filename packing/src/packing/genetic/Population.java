
package packing.genetic;


// Packing imports
import packing.data.Dataset;
import packing.genetic.RandomPopulation.RandomInstance;
import packing.packer.Packer;

//##########

/**
 * General population interface.
 */
public abstract class Population {
    
    public abstract class Instance<C extends Instance>
            implements Comparable<C>, packing.tools.Cloneable {
        /**
         * Performs a crossover with the provided instance.
         * 
         * @param other instance that is used in the crossover.
         */
        public abstract C crossover(C other);
        
        /**
         * Mutates the current instance.
         */
        public abstract void mutate();
        
        /**
         * Calculates the fitness of the instance.
         * 
         * @param packer the packer to use for c
         *     public void calculateFitness(Packer packer) {alculating the fitness.
         */
        public abstract void calculateFitness(Packer packer);
        
        
        @Override
        public abstract C clone();
    }
    
    /**
     * Calculates the fitness of all instances.
     * If the instance if the best one seen so far, update best.
     */
    public abstract void calculateFitness();
    
    /**
     * Perform selection to create the next generation. Use an elitist strategy
     * (i.e. always keep the best instance from the previous generation). Then
     * randomly select parents from the previous generation with probability
     * proportional to the inverse of its rank.
     */
    public abstract void performSelection();
    
    /**
     * Performs a mutation for all instances.
     */
    public abstract void performMutation();
    
    /**
     * @return the dataset of the best instance of this population.
     */
    public abstract Dataset getBest();
    
}
