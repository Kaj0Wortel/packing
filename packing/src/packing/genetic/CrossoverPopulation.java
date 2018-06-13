
package packing.genetic;


// Packing imports
import packing.data.Dataset;
import packing.data.PolishDataset;
import packing.data.PolishDataset.Operator;
import packing.data.CompareEntry;
import packing.packer.Packer;
import packing.packer.PolishPacker;
import packing.tools.MultiTool;


//##########
// Java imports
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import packing.gui.ShowDataset;


/**
 * Population class supporting evaluation, crossover and mutations
 * using reverse polish notation.
 */
public class CrossoverPopulation
        extends Population {
    // The size of the population.
    final public static int POPULATION_SIZE = 200;
    
    // The maximum relative size for operators that might be crossed over.
    final public static double MAX_REL_SIZE = 0.5;
    // The crossover rate for every crossover.
    final public static double CROSSOVER_RATE = 0.2;
    
    // Mutation rate of the amount of swaps performed
    final public static double MUTATE_SWAP_ENTRY_RATE = 0.1;
    // Mutation rate of the number of new randomly generated operators.
    final public static double MUTATE_CHANGE_OPERATOR_RATE = 0.1;
    // Mutation rate of the number of randomly rotated entries.
    final public static double MUTATE_ROTATION_RATE = 0.1;
    
    // Learning rate for fixing the number of discarded solutions with
    // respect to {@link #POPULATION_SIZE}.
    final public static double DISCARD_LEARNING_RATE = 0.25;
    // The increase in population such that the average number of
    // individuals that are not discarded will still match
    // {@link #POPULATION_SIZE}.
    private int repairDiscard = 0;
    
    // The propapility that the fist element is choosen as parent.
    final public static double SELECT_FIRST_CHANCE = 0.1;
    // The power of the exponent to determine the parent.
    private double alpha;
    
    
    
    // Random variable for calculating chances.
    final public static Random random = new Random();
    
    // List containing the current population.
    protected List<CrossInstance> list = new LinkedList<>();
    
    // The best solution so far.
    public Dataset best = null;
    
    // The current dataset.
    final public Dataset dataset;
    // The height of the dataset. Integer.MAX_VALUE if no height limit.
    final public int height;
    
    /**
     * Class representing an instance of the population.
     */
    public class CrossInstance
            extends Population.Instance<CrossInstance> {
        
        // Dataset containing the entries in polish notation.
        final private PolishDataset pd;
        // Maps some operators to their fitness values.
        final private Map<Operator, Double> opMap;
        
        
        /**
         * Creates a new instance from the provided dataset.
         * 
         * @param dataset input dataset.
         * 
         * Note: does not clone the provided dataset.
         */
        CrossInstance(Dataset dataset) {
            if (dataset instanceof PolishDataset) {
                pd = (PolishDataset) dataset;
                
            } else {
                pd = new PolishDataset(dataset);
            }
            this.opMap = new HashMap<>();
        }
        
        private class EntryValue {
            final private CompareEntry entry;
            final private List<Map<Operator, Double>> mapList;
            final private List<Operator> opList;
            
            /**
             * Creates a new EntryValue that stores all multiple entries.
             * 
             * @param entry the entry that is involved in the operation.
             * @param map the map that contains the score of {@code op}.
             * @param op the operator that is involved for this entry.
             */
            private EntryValue(CompareEntry entry, Map<Operator, Double> map,
                    Operator op) {
                this.mapList = new ArrayList<Map<Operator, Double>>();
                this.opList = new ArrayList<Operator>();
                this.entry = entry;
                this.mapList.add(map);
                this.opList.add(op);
            }
            
            /**
             * Adds an entry.
             * 
             * @param map
             * @param op 
             */
            protected void addEntry(Map<Operator, Double> map, Operator op) {
                mapList.add(map);
                opList.add(op);
            }
            
            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof EntryValue)) return false;
                EntryValue val = (EntryValue) obj;
                return entry.getId() == val.entry.getId();
            }
            
            @Override
            public int hashCode() {
                return MultiTool.calcHashCode(entry.getId());
            }
            
            /**
             * Removes all operators from the maps except for one.
             */
            public void removeAllExceptOne() {
                int best = 0;
                double bestVal = 0;
                for (int i = 0; i < mapList.size(); i++) {
                    Double score = mapList.get(i).get(opList.get(i));
                    if (score == null) score = -1.0;
                    double val = score * random.nextDouble();
                    if (val > bestVal) {
                        bestVal = val;
                        best = i;
                    }
                }
                
                for (int i = 0; i < opList.size(); i++) {
                    if (i == best) continue;
                    mapList.get(i).remove(opList.get(i));
                    opList.remove(i);
                }
            }
            
        }
        
        @Override
        public CrossInstance crossover(CrossInstance other) {
            System.out.println("crossover");
            // Obtain all double instances from both sets.
            Set<EntryValue> doubleSet = new HashSet<>();
            Map<Integer, EntryValue> entryMap = new HashMap<>();
            
            Map<Operator, Double>[] opMaps = new Map[] {
                new HashMap<>(opMap),
                new HashMap<>(other.opMap)
            };
            
            for (int i = 0; i < 2; i++) {
                Iterator<Operator> it = opMaps[i].keySet().iterator();
                while (it.hasNext()) {
                    Operator op = it.next();
                    List<CompareEntry> entries = op.listAllEntries();
                    for (CompareEntry entry : entries) {
                        EntryValue ev = entryMap.get(entry.getId());
                        if (ev != null) {
                            ev.addEntry(opMaps[i], op);
                            doubleSet.add(ev);
                            
                        } else {
                            entryMap.put(entry.getId(),
                                    new EntryValue(entry, opMaps[i], op));
                        }
                    }
                }
            }
            
            // Reduce all double instances such that there is at most one
            // instance left for each.
            for (EntryValue value : doubleSet) {
                value.removeAllExceptOne();
            }
            
            // Merge the two available crossover maps into an array.
            List<Operator> opOrder = new LinkedList<Operator>();
            opOrder.addAll(generateOpOrder(opMaps[0]));
            opOrder.addAll(generateOpOrder(opMaps[1]));
            
            // Add all remaining parts as format hints.
            List<CompareEntry>[] hints = new List[opOrder.size()];
            int i = 0;
            for (Operator op : opOrder) {
                List<CompareEntry> hint = op.listAllInvolved();
                hint.add(op);
                hints[i++] = hint;
            }
            
            // Create a new instance using the format hints.
            CrossInstance ci = new CrossInstance(pd.clone());
            ci.pd.regenerate(hints);
            
            return ci;
        }
        
        /**
         * @return a sorted list based on the scores in {@link #opMap}.
         */
        public List<Operator> generateOpOrder(Map<Operator, Double> map) {
            Set<Operator> keySet = map.keySet();
            List<Operator> result = MultiTool.iterableToList(
                    keySet, keySet.size(), new LinkedList<Operator>());
            
            Collections.sort(result,
                    Comparator.comparingDouble(op -> {
                        return map.get(op);
                    })
            );
            
            return result;
        }
        
        @Override
        public void mutate() {
            // Swap entries + operators.
            for (int i = 0; i < pd.size() * MUTATE_SWAP_ENTRY_RATE; i++) {
                pd.swapRandomEntries();
            }
            
            // Mutate operators.
            for (int i = 0; i < pd.size() * MUTATE_CHANGE_OPERATOR_RATE; i++) {
                pd.changeOperator();
            }
            
            // Rotate entries.
            for (int i = 0; i < pd.size() * MUTATE_ROTATION_RATE; i++) {
                pd.randomRotate();
            }
        }
        
        @Override
        public void calculateFitness(Packer packer) {
            opMap.clear();
            packer.pack(pd);
            
            Iterator<Operator> it = pd.operatorIterator();
            while (it.hasNext()) {
                // Get the next operator.
                Operator op = it.next();
                
                // Ignore operators that contain more then {@link #MAX_REL_SIZE}
                // of the entire sheet.
                double sizeRatio = ((double) op.size()) / pd.size();
                if (sizeRatio > MAX_REL_SIZE) continue;
                
                double score = 0.5 - op.wastedRatio();
                // -0.5 <= score <= 0.5
                score *= sizeRatio / MAX_REL_SIZE;
                // -(size ratio) <= score <= (size ratio)
                // Where: 0 < sizeRatio <= MAX_REL_SIZE <= 1
                // ==> 0 < sizeRatio / MAX_REL_SIZE <= 1
                // So: -0.5 < score <= 0.5
                score += 0.5;
                // 0 < score <= 1
                
                opMap.put(op, score);
            }
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
        this.dataset = dataset;
        this.list = new LinkedList<>();
        if (height <= 0) this.height = Integer.MAX_VALUE;
        else this.height = height;
        
        init();
    }
    
    /**
     * Initializes the population.
     */
    private void init() {
        while (list.size() < POPULATION_SIZE + repairDiscard) {
            Dataset clone = dataset.clone();
            clone.shuffle();
            CrossInstance ci = new CrossInstance(clone);
            ci.pd.init();
            list.add(ci);
            
        }
    }
    
    
    @Override
    public void calculateFitness() {
        int discarded = 0;
        Iterator<CrossInstance> it = list.iterator();
        boolean newBest = false;
        while (it.hasNext()) {
            CrossInstance inst = it.next();
            inst.calculateFitness(new PolishPacker());
            
            // If there is a fixed height, check if the solution is allowed.
            // If not, delete the solution.
            if (inst.pd.isFixedHeight() &&
                    inst.pd.getHeight() > inst.pd.getEffectiveHeight()) {
                it.remove();
                discarded++;
            }
            
            // Update {@link #best} when a better solution has been found.
            if (best == null || inst.pd.getArea() < best.getArea()) {
                newBest = true;
                best = inst.pd;
            }
            
            // Update the value for taking the number of discarded
            // instances in account.
            repairDiscard += Math.ceil(
                    (discarded - repairDiscard) * DISCARD_LEARNING_RATE);
        }
        
        if (newBest) {
            best = best.clone();
        }
    }
    
    @Override
    public void performSelection() {
        if (list.size() <= 1) {
            if (best != null) list.add(new CrossInstance(best));
            init();
            return;
        }
        
        List<CrossInstance> newPopulation = new LinkedList<>();
        
        System.out.println("sorting");
        // Sort all entries from big to small area.
        Collections.sort(list, Comparator.comparingInt(cd -> {
            return cd.pd.getArea();
        }));
        System.out.println("sorting done");
        
        // Add the best instance by default.
        if (best != null) list.add(new CrossInstance(best));
        
        // Calculate the alpha value to use in the formula for determining
        // the parents.
        calcAlpha(list.size());
        System.out.println("alpha calced");
        
        // Create the new population.
        while (newPopulation.size() < POPULATION_SIZE + repairDiscard) {
            System.out.println("adding entry");
            CrossInstance parent1 = selectParent(null);
            System.out.println("selected parent 1");
            CrossInstance parent2 = selectParent(parent1);
            System.out.println("selected parent 2");
            newPopulation.add(parent1.crossover(parent2));
        }
        
        // Update the population.
        list = newPopulation;
    }
    
    /**
     * Generates the alpha value for choosing parents.
     * 
     * @param n the number of entries to choose from.
     * 
     * Note: it is assumed that the list is sorted in decreasing(!) order
     *     for the area.
     * 
     * Calculation:
     * We approach the generating using the following formula:
     * {@code (n+1) = N * x^alpha}
     * where:
     * - N = total number of entries.
     * - n = choosen index of the list.
     * - x = random generated value s.t. {@code 0 <= x <= 1}.
     * Now we want {@code SELECT_FIRST_CHANCE} as chance for the first entry.
     * Therefore must hold:
     * {@code (n+1) * (1 - SELECT_FIRST_CHANCE)^alpha = n}
     * {@code ==> (1 - SELECT_FIRST_CHANCE)^alpha = n / (n + 1)}
     * {@code ==> alpha = log_{1 - SELECT_FIRST_CHANCE} (n/(n+1))}
     * 
     * Note that {@code n >= 1} must hold.
     */
    private void calcAlpha(int n) {
        alpha = Math.log(n / (n + 1.0)) / Math.log(1.0 - SELECT_FIRST_CHANCE);
        System.out.println("up=" + Math.log(n / (n + 1.0)));
        System.out.println("log=" + Math.log(1 - SELECT_FIRST_CHANCE));
    }
    
    /**
     * Select a parent used for the crossover operation.
     * Assumes that {@link #list} is sorted based on increasing area.
     * 
     * @param ignore if this entry is selected, try again.
     * @return a parent that can be used in the crossover operation
     * 
     * Note that {@code n >= 2} must hold.
     */
    private CrossInstance selectParent(CrossInstance ignore) {
        CrossInstance ci = null;
        do {
            int index = (int) Math.floor(list.size()
                    * Math.pow(random.nextDouble(), alpha));
            if (index >= list.size()) {
                index = list.size() - 1;
            }
            ci = list.get(index);
        } while (ci == ignore);
        
        return ci;
    }
    
    @Override
    public void performMutation() {
        for (CrossInstance instance : list) {
            instance.mutate();
        }
    }
    
    @Override
    public Dataset getBest() {
        return best;
    }
    
    
    
    
    // TMP
    public CrossInstance create(Dataset ds) {
        return new CrossInstance(ds);
    }
    
    // tmp
    public static void main(String[] args) {
        Dataset ds = new Dataset(-1, false, 4);
        ds.add(1, 1);
        ds.add(2, 2);
        ds.add(3, 3);
        ds.add(4, 4);
        
        CrossoverPopulation cp = new CrossoverPopulation(ds, -1);
        for (int i = 0; i < 4; i++) {
            System.out.println("fitness " + i);
            cp.calculateFitness();
            System.out.println("selection " + i);
            cp.performSelection();
            System.out.println("mutation " + i);
            cp.performMutation();
        }
        new ShowDataset(cp.getBest());
    }
    
    // tmp
    /*
    public static void main(String[] args) {
        // Create dataset.
        Dataset ds = new Dataset(-1, false, 4);
        ds.add(1, 1);
        ds.add(2, 2);
        ds.add(3, 3);
        ds.add(4, 4);
        PolishDataset pd = new PolishDataset(ds);
        pd.init();
        System.out.println("pd: " + pd.toShortString());
        
        
        // Create mapping.
        Map<Operator, Double> map = new HashMap<>();
        Iterator<Operator> opIt = pd.operatorIterator();
        double i = 10;
        while (opIt.hasNext()) {
            map.put(opIt.next(), i--);
        }
        System.out.println("map: " + map);
        
        
        // Create instance.
        //CrossInstance crossInv = new CrossoverPopulation(ds, -1).create(pd);
        
        
        // Generate order + lists.
        Set<Operator> keySet = map.keySet();
        Operator[] opOrder = MultiTool.iterableToArray(
                keySet, Operator.class, keySet.size());

        Arrays.sort(opOrder,
                Comparator.comparingDouble(op -> {
                    return map.get(op);
                })
        );
        LinkedList<Operator> opList = new LinkedList<Operator>();
        
        
        // For both ({@code this} and {@code other}, select which genes
        // should be used in the crossover.
        System.out.println("opOrder: " + Arrays.toString(opOrder));
        for (int j = 0; j < opOrder.length; j++) {
            opList.add(opOrder[j]);
        }
        
        System.out.println("opList: " + opList);
        
        MultiTool.sleepThread(10);
        for (Operator op : opList) {
            System.err.println(op.listAllEntries());
        }
        MultiTool.sleepThread(10);
        //crossInv.filterDoubles(map, map, opList);
        MultiTool.sleepThread(10);
        /*
        System.out.println(MultiTool.disjoint(
                opList.get(0).listAllEntries(),
                opList.get(1).listAllEntries(), CompareEntry.SORT_ID));
        *//*
        System.out.println("new opList: " + opList);
    }
    /**/
}
