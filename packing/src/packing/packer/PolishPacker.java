
package packing.packer;


// Packing imports
import packing.data.CompareEntry;
import packing.data.Dataset;
import packing.data.MergedEntryDataset;
import packing.data.MergedEntryDataset.MergedEntry;
import packing.data.PolishDataset;
import packing.data.PolishDataset.Direction;
import packing.data.PolishDataset.Operator;
import packing.tools.MultiTool;


//##########
// Java imports
import java.awt.Rectangle;
import java.util.Iterator;
import java.util.Stack;


/**
 * Packer that packs a polish notation dataset.
 */
public class PolishPacker
        extends Packer {
    
    
    public PolishPacker() {
        
    }
    
    
    @Override
    public PolishDataset pack(Dataset dataset) {
        PolishDataset pd;
        MergedEntryDataset med;
        if (dataset instanceof PolishDataset) {
            pd = (PolishDataset) dataset;
            med = new MergedEntryDataset(pd.getDataset());
            
        } else {
            pd = new PolishDataset(dataset);
            med = new MergedEntryDataset(dataset);
        }
        
        //System.err.println("Packing: " + pd.toShortString());
        //System.err.println("Dataset: " + dataset.toString());
        
        Iterator<CompareEntry> it = pd.fullListIterator();
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
                    
                } else { // dir == Direction.RIGHT.
                    e2.setLocation(rec.x + rec.width, rec.y);
                }
                
                // Merge the entries.
                MergedEntry me = med.merge(e1, e2);
                
                // Push the element on the stack.
                entryStack.push(me);
                
                // Set the area and wasted area.
                op.setArea(me.area());
                op.setWastedArea(me.wastedArea());
                
            } else {
                // Push the element on the stack.
                entryStack.push(entry);
                // And reset their position.
                entry.setLocation(0, 0);
            }
        }
        
        // Update the bounds of the dataset.
        // Note that at this point the merged entry dataset has exactly one
        // entry containing all entries of the dataset.
        pd.calcEffectiveSize();
        
        return pd;
    }
    
    
    public static void main(String[] args) {
        Dataset data = new Dataset(-1, true, 5);
        data.add(new Rectangle(1000, 1000, 1, 1));
        data.add(new Rectangle(1000, 1000, 1, 1));
        data.add(new Rectangle(1000, 1000, 1, 1));
        data.add(new Rectangle(1000, 1000, 1, 1));
        data.add(new Rectangle(1000, 1000, 1, 1));
        data.add(new Rectangle(1000, 1000, 1, 1));
        data.add(2, 2);
        data.add(3, 3);
        data.add(4, 4);
        
        data.add(1, 1);
        data.add(2, 2);
        data.add(3, 3);
        data.add(4, 4);
        
        
        PolishDataset in = new PolishDataset(data);
        in.init();
        
        PolishDataset result = new PolishPacker().pack(in);
        System.out.println(result.toShortString());
        System.out.println(result);
        //MultiTool.sleepThread(100);
        /*
        System.out.println(result.toShortString());
        System.out.println("width=" + result.getWidth());
        System.out.println("height=" + result.getHeight());
        Iterator<Operator> opIt = result.operatorIterator();
        while (opIt.hasNext()) {
            System.err.println("entry!");
            CompareEntry[] entries = opIt.next().getEntries();
            System.err.println(entries[0] + ", " + entries[1]);
            //MultiTool.sleepThread(100);
        }*/
        //new ShowDataset(result);
    }
    
    
}
