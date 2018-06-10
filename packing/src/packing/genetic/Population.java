
package packing.genetic;


// Packing imports
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
}
