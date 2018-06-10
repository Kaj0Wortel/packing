
package packing.packer;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.data.MergedEntryDataset;
import packing.data.MergedEntryDataset.MergedEntry;
import packing.data.PolishDataset;
import packing.data.PolishDataset.Direction;
import packing.data.PolishDataset.Operator;
import packing.gui.ShowDataset;
import packing.tools.MultiTool;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Stack;


/**
 *
 */
public class PolishPacker extends Packer {
    
    
    public PolishPacker() {
        
    }
    
    
    @Override
    public PolishDataset pack(Dataset dataset) {
        PolishDataset pd;
        if (dataset instanceof PolishDataset) {
            pd = (PolishDataset) dataset;
            
        } else {
            dataset.shuffle();
            pd = new PolishDataset(dataset);
            pd.init();
        }
        
        MergedEntryDataset med = new MergedEntryDataset(dataset); // TODO
        
        Iterator<CompareEntry> it = pd.listIterator();
        Stack<CompareEntry> entryStack = new Stack<>();
        while (it.hasNext()) {
            CompareEntry entry = it.next();
            if (entry instanceof Operator) {
                // If the entry is an operator, merge the last two elements.
                Operator op = (Operator) entry;
                CompareEntry e2 = entryStack.pop();
                CompareEntry e1 = entryStack.pop();
                
                Rectangle rec = e1.getRec();
                if (op.getDirection() == Direction.UP) {
                    e2.setLocation(rec.x, rec.y + rec.height);
                    System.err.println("UP");
                    
                } else { // dir == Direction.RIGHT.
                    e2.setLocation(rec.x + rec.width, rec.y);
                    System.err.println("RIGHT");
                }
                
                // Merge the entries.
                MergedEntry me = med.merge(e1, e2);
                
                // Push the element on the stack.
                entryStack.push(me);
                
            } else {
                // Push the element on the stack.
                entryStack.push(entry);
            }
        }
        
        // Update the bounds of the dataset.
        // Note that at this point the merged entry dataset has exactly one
        // entry containing all entries of the dataset.
        Rectangle meRec = med.iterator().next().getRec();
        pd.setSize(meRec.width, meRec.height);
        
        return pd;
    }
    
    
    public static void main(String[] args) {
        Dataset data = new Dataset(-1, true, 5);
        data.add(1, 1);
        data.add(2, 2);
        data.add(3, 3);
        data.add(4, 4);
        /*
        PolishDataset test = new PolishDataset(data);
        test.init();
        test.setSize(5, 5);
        new ShowDataset(test);
        */
        /**/
        PolishDataset result = new PolishPacker().pack(data);
        MultiTool.sleepThread(1000);
        System.out.println(result.toShortString());
        System.out.println("width=" + result.getWidth());
        System.out.println("height=" + result.getHeight());
        new ShowDataset(result);
        /**/
    }
    
    
}
