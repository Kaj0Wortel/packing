package packing.genetic;

import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Population {
    public static final int POPULATION_SIZE = 200;

    public static final boolean ELITIST = true;

    public static final double MUTATION_RATE = 0.1;

    private PackerFactory packerFactory;

    private List<Instance> instances;

    private int height;

    private int maxWidth = Integer.MAX_VALUE;

    private int target;

    private static Random random = new Random();

    private Instance best = null;

    public class Instance implements Comparable<Instance>, packing.tools.Cloneable {
        private Dataset dataset;
        private double fitness;


        public Instance(Dataset dataset) {
            this.dataset = dataset;
        }

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

        public void crossover(Instance other) {
        }

        public void calculateFitness(Packer packer) {
            Dataset packed = packer.pack(this.dataset);
            if (packed == null) {
                fitness = 0;
            } else {
                fitness = 1.0 / (packed.getArea() - target + 1);
            }
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

        public double getFitness() {
            return fitness;
        }
    }


    private Population(List<Instance> instances, PackerFactory factory, int height) {
        this.instances = instances;
        this.packerFactory = factory;
        this.height = height;
    }

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

    public void calculateFitness() {
        for (Instance instance : instances) {
            Packer packer = packerFactory.create(maxWidth, height);
            instance.calculateFitness(packer);
        }

        instances.sort(Collections.reverseOrder());
        best = instances.get(0);
    }

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

    public void performSelection() {
        List<Instance> newInstances = new ArrayList<>(POPULATION_SIZE);

        double fitnessSum = 0;
        for (Instance instance : instances) {
            fitnessSum += instance.getFitness();
        }

        if (ELITIST) {
            newInstances.add(instances.get(0).clone());
        }

        while (newInstances.size() < POPULATION_SIZE / 2) {
            newInstances.add(selectParent(fitnessSum));
        }

        while (newInstances.size() < POPULATION_SIZE) {
            Dataset dataset = best.dataset.clone();
            dataset.shuffle();
            dataset.setRotation(Dataset.RANDOM_ROTATION);
            newInstances.add(new Instance(dataset));
        }

        instances = newInstances;
    }

    public void performMutation() {
        // Mutate all instances except the first/best
        for (int i = 1; i < instances.size(); i++) {
            instances.get(i).mutate();
        }
    }

    public int getMaxWidth() {
        return maxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        this.maxWidth = maxWidth;
    }

    public int getTarget() {
        return target;
    }

    public void setTarget(int target) {
        this.target = target;
    }

    public Dataset getBest() {
        return best.dataset;
    }
}
