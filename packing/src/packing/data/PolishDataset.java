
package packing.data;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


/**
 * 
 */
public class PolishDataset
        extends DatasetDecorator {
    
    final private static Random random = new Random();
    
    public static enum Direction {
        RIGHT, UP;
    }
    
    public class Operator
            extends Dataset.Entry {
        final private Direction dir;
        
        public Operator(Direction dir) {
            super(null, -1);
            this.dir = dir;
        }
        
        public Direction getDirection() {
            return dir;
        }
        
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Operator) return false;
            Operator op = (Operator) obj;
            return op.dir == this.dir;
        }
        
        @Override
        public String toString() {
            return (dir == Direction.UP ? "+" : "*");
        }
        
    }
    
    public PolishDataset(Dataset dataset) {
        super(dataset);
        list = new LinkedList<CompareEntry>();
    }
    
    public PolishDataset(Dataset dataset, List<CompareEntry> list) {
        super(dataset);
        this.list = list;
    }
    
    @Override
    public PolishDataset clone() {
        return new PolishDataset(dataset.clone(), new LinkedList<>(list));
    }
    
    /**
     * Initializes the dataset with a random reverse polish notation.
     */
    public void init() {
        list.clear();
        int numOp = 0;
        int numElem = 0;
        int size = size();
        
        Iterator<CompareEntry> it = dataset.iterator();
        
        // Add all elements and (some) operators randomly
        while (numElem < size) {
            if (numElem - 2 >= numOp &&
                    (!it.hasNext() || random.nextBoolean())) {
                // Add an operator.
                numOp++;
                Operator op;
                if (random.nextBoolean()) {
                    op = new Operator(Direction.UP);
                    //list.add(new Operator(Direction.UP));
                } else {
                    op = new Operator(Direction.RIGHT);
                    //list.add(new Operator(Direction.RIGHT));
                }
                System.err.println(op);
                list.add(op);
                
            } else {
                // Add an element.
                list.add(it.next());
                numElem++;
            }
        }
        
        // Add the remaining operators.
        while(numOp < size - 1) {
            // Select an operator.
            numOp++;
            Operator op;
            if (random.nextBoolean()) {
                op = new Operator(Direction.UP);
                //list.add(new Operator(Direction.UP));
            } else {
                op = new Operator(Direction.RIGHT);
                //list.add(new Operator(Direction.RIGHT));
            }
            System.err.println(op);
            list.add(op);
        }
    }
    
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] strs = new String[list.size()];
        //strs[0] =getClass().getSimpleName() + "["
        
        Iterator<CompareEntry> it = list.iterator();
        int i = 0;
        while (it.hasNext()) {
            strs[i++] = it.next().toString();
        }
        
        return getClass().getSimpleName() + "[" + String.join("", strs) + "]";
    }
    
    public static void main(String[] args) {
        Dataset dataset = new Dataset(-1, true, 8);
        dataset.add(new Rectangle(1, 1));
        dataset.add(new Rectangle(2, 2));
        dataset.add(new Rectangle(3, 3));
        dataset.add(new Rectangle(4, 4));
        dataset.add(new Rectangle(5, 5));
        dataset.add(new Rectangle(6, 6));
        dataset.add(new Rectangle(7, 7));
        dataset.add(new Rectangle(8, 8));
        
        PolishDataset pd = new PolishDataset(dataset);
        pd.init();
        System.out.println(pd.toString());
    }
    
}
