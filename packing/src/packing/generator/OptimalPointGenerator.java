
package packing.generator;


// Packing imports
import packing.data.Dataset;


// Java imports
import java.awt.Point;
import java.awt.Rectangle;

import java.util.Stack;
import packing.data.IgnoreDoubleDataset;
import packing.generator.OptimalPointGenerator.LinkAction;
import packing.gui.ShowDataset;
import packing.packer.PackerFactory;
import packing.tools.Logger;
import packing.tools.MultiTool;
import packing.tools.StreamLogger;


/**
 * Keeps track of all points of the possible locations of the rectangles.
 */
public class OptimalPointGenerator extends Generator {
    // Stack for keeping track of the linked list manipulations
    // over the iterations.
    final private Stack<LinkAction> nodeActions = new Stack<LinkAction>();
    // Stack for keeping track of the current node over the iterations.
    final private Stack<PointNode> curNodeStack = new Stack<PointNode>();
    
    // These points represent the fist and the last elements of the chain.
    private PointNode last;
    private PointNode first;
    
    // The current node that is being processed.
    private PointNode curNode;
    
    // The current width of the dataset.
    private int width = 0;
    
    // The dataset to generate.
    private Dataset dataset;
    // Dataset that ignores doubles.
    private IgnoreDoubleDataset doubleDataset;
    
    // Variables to keep track of the amount of wasted space.
    private int wastedSpace = 0;
    private int wastedWidth = 0;
    private int wastedHeight = 0;
    private int totalInputArea;
    // Stack for keeping track of the total amount of wasted space
    // over the iterations.
    private Stack<Integer> wastedSpaceStack = new Stack<>();

    private ShowDataset showDataset = null;
    
    /**-------------------------------------------------------------------------
     * PointNode class for creating a local linked list.
     * Note that {@link java.util.LinkedList} cannot be used since this linked
     * list does not support adding elements after a specified element.
     */
    protected class PointNode {
        final private Point point;
        protected PointNode next = null;
        protected PointNode prev = null;
        
        /**
         * Simple constructor that creates a single node without links.
         * 
         * @param point the point data for this link. Cannot be {@code null}.
         */
        protected PointNode(Point point) {
            this.point = point;
        }
        
        /**
         * Constructor that creates a single node with links.
         * 
         * @param prev the previous chain element. Can be {@code null}.
         * @param point the point data for this link. Cannot be {@code null}.
         * @param next the next chain element. Can be {@code null}.
         */
        protected PointNode(PointNode prev, Point point, PointNode next) {
            this.point = point;
            this.next = next;
            this.prev = prev;
        }
        
        public int getMaxHeight() {
            return (next == null
                    ? Integer.MAX_VALUE
                    : next.point.y - point.y);
        }
        
        @Override
        public String toString() {
            return "PointNode [x=" + point.x + ", y=" + point.y
                    + ", maxHeight=" + getMaxHeight() + "]";
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Class to easily execute and revert chain modifications.
     * Note that "left" here means "next" and "right" means "previous".
     * By default, the link action will be executed when created.
     */
    abstract protected class LinkAction {
        /**
         * For executing a chain modification.
         */
        abstract protected void execute();
        
        /**
         * For reverting a chain modification.
         */
        abstract protected void revert();
        
    }
    
    
    /**
     * Class for removing links to the chain between two link nodes.
     * Note that the width should never change when a point is removed.
     * (only ensured by correct usage, not via this class).
     */
    protected class RemoveLinkAction extends LinkAction {
        final protected PointNode removeLeft;
        final protected PointNode removeRight;
        
        /**
         * Single link to be removed.
         * 
         * @param remove 
         */
        protected RemoveLinkAction(PointNode remove) {
            this(remove, remove);
        }
        
        /**
         * Chain to be removed.
         * 
         * @param removeLeft leftmost chain element to be removed.
         * @param removeRight rightmost chain element to be removed.
         */
        protected RemoveLinkAction(PointNode removeLeft,
                                   PointNode removeRight) {
            this.removeLeft = removeLeft;
            this.removeRight = removeRight;
            
            execute();
        }
        
        @Override
        protected void execute() {
            if (removeLeft.next != null)
                removeLeft.next.prev = removeRight.prev;
            if (removeRight.prev != null)
                removeRight.prev.next = removeLeft.next;
            
            // Update the first and last nodes.
            if (removeRight.prev == null) first = removeLeft.next;
            if (removeLeft.next == null) last = removeRight.prev;
        }
        
        @Override
        protected void revert() {
            if (removeLeft.next != null)
                removeLeft.next.prev = removeLeft;
            if (removeRight.prev != null)
                removeRight.prev.next = removeRight;
            
            // Update the first and last nodes.
            if (removeRight.prev == null) first = removeRight;
            if (removeLeft.next == null) last = removeLeft;
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * Class for replacement of a chain part.
     */
    protected class ReplaceLinkAction extends LinkAction {
        final protected PointNode leftOld;
        final protected PointNode rightOld;
        final protected PointNode leftNew;
        final protected PointNode rightNew;
        final protected int prevWidth;
        
        /**
         * Short constructor for a single old node and a single new node.
         * 
         * @param oldNode the node to be replaced.
         * @param newNode the replacing node.
         */
        protected ReplaceLinkAction(PointNode oldNode, PointNode newNode) {
            this(oldNode, oldNode, new PointNode[] {newNode});
        }
        
        /**
         * Short constructor for a single old node and multiple new nodes.
         * 
         * @param oldNode the node to be replaced.
         * @param newNodes chain of replacing nodes.
         */
        protected ReplaceLinkAction(PointNode oldNode, PointNode[] newNodes) {
            this(oldNode, oldNode, newNodes);
        }
        
        /**
         * Full constructor to replace a chain of old nodes with a chain
         * of new nodes.
         * 
         * @param leftOld the leftmost node of the old chain to be replaced.
         * @param rightOld the rightmost node of the old chain to be replaced.
         * @param newNodes all nodes in the new chain, not yet connected.
         */
        protected ReplaceLinkAction(PointNode leftOld, PointNode rightOld,
                                    PointNode[] newNodes) {
            this.leftOld = leftOld;
            this.rightOld = rightOld;
            this.leftNew = newNodes[newNodes.length - 1];
            this.rightNew = newNodes[0];
            this.prevWidth = width;
            
            // Connect the new part amoung each other.
            for (int i = 0; i < newNodes.length - 1; i++) {
                newNodes[i].next = newNodes[i + 1];
                newNodes[i + 1].prev = newNodes[i];
            }
            
            // Make the connection for the new part to the old part, but not
            // yet the other way around.
            leftNew.next  = (leftOld  != null ? leftOld.next  : null);
            rightNew.prev = (rightOld != null ? rightOld.prev : null);
            
            execute();
        }
        
        /**
         * Replace the old part with the new part.
         */
        @Override
        protected void execute() {
            if (leftOld.next != null) leftOld.next.prev = leftNew;
            if (rightOld.prev != null) rightOld.prev.next = rightNew;
            
            // Update the first and last nodes.
            if (rightOld.prev == null) first = rightNew;
            if (leftOld.next == null) last = leftNew;
            
            // Update the width.
            width = Math.max(rightNew.point.x, width);
        }
        
        /**
         * Replace the new part with the old part.
         */
        @Override
        protected void revert() {
            if (leftOld.next != null) leftOld.next.prev = leftOld;
            if (rightOld.prev != null) rightOld.prev.next = rightOld;
            
            // Update the first and last nodes.
            if (rightOld.prev == null) first = rightOld;
            if (leftOld.next == null) last = leftOld;
            
            // Restore the width.
            width = prevWidth;
        }
        
    }
    
    
    /**-------------------------------------------------------------------------
     * @param dataset 
     *//*
    public OptimalPointGenerator(List<Dataset.Entry> dataset) {
        remaining = new LinkedList<Dataset.Entry>(dataset);
    }
    */
    public OptimalPointGenerator(PackerFactory factory) {
        super(factory);
    }
    
    /**
     * Searches for the best points to try rectangles for.
     * 
     * @return {@code null} if there are no available points,
     *     1 point if space below a floating piont is filled in, and
     *     2 points if all space below the floating points has been filled.
     * 
     * Note that when 1 or 2 points are returned, it is guaranteed that
     * there are no other points in the lower left quadrant (including its
     * own x- and y-coord).
     * When 2 points are returned, it is guaranteed that the only bound is
     * set by the maximum height and width of the rectangle.
     */
    private PointNode[] getPoints() {
        Logger.write("curNode (pre): " + curNode);
        // Initialization.
        if (nodeActions.isEmpty() || last == null || curNode == null) {
            Logger.write("(-1)");
            return new PointNode[] {new PointNode(new Point(0, 0))};
        }
        
        // Check whether the current point staisfies the main property by
        // checking whether the point below is on the right. Note that it is
        // assumed that the property holds for the previous point (since
        // no modifications have been made below since the property was valid).
        if (curNode.prev != null && curNode.prev.point.x < curNode.point.x) {
            Logger.write("(0)");
            Logger.write(curNode);
            Logger.write(curNode.prev);
            curNode = curNode.prev;
            return new PointNode[] {curNode};
        }
        
        // If all points below (incl. {@code curNode}) satisfy the
        // main property, then we try to find a point above that doesn't
        // satisfy. If no invallid points, use the upper two points.
        while (curNode.next != null &&
               curNode.next.point.x < curNode.point.x) {
            Logger.write("(1)");
            curNode = curNode.next;
        }
        
        if (curNode.next == null) {
            Logger.write("(2)");
            if (last.prev != null) {
                Logger.write("(2.1)");
                return new PointNode[] {last, last.prev};
                
            } else {
                Logger.write("(2.2)");
                return new PointNode[] {last};
            }
            
        } else {
            Logger.write("(3)");
            Logger.write(curNode);
            // Otherwise we know that {@code next} doesn't satisfy the main
            // property, hence we return {@code node}.
            //curNode.maxHeight = curNode.next.point.y - curNode.point.y;
            return new PointNode[] {curNode};
        }
    }
    
    /**
     * Checks whether the rectangle is allowed to be placed at the current
     * location. If allowed, place the rectangle and replace points as
     * nessecary.
     * 
     * @param entry the entry to be placed.
     * @param node the node describing the point where the entry should be
     *     placed
     * @return whether the entry was placed.
     */
    private boolean checkAndAddEntry(Dataset.Entry entry, PointNode node) {
        Rectangle rec = entry.getRec();
        
        // Check if the rectangle doesn't hit any overhanging rectangles.
        if (node.getMaxHeight() < rec.getHeight()) {
            return false;
        }
        
        // Check if the rectangle fits in the bounding box.
        if (dataset.isFixedHeight() &&
                dataset.getHeight() > node.point.y + rec.height) {
            return false;
        }
        
        // The rectangle is allowed to be placed, so place it.
        rec.x = node.point.x;
        rec.y = node.point.y;
        
        // The points for the upper left and lower right corners.
        Point upLeft = new Point(node.point.x, node.point.y + rec.height);
        Point downRight = new Point(node.point.x + rec.width, node.point.y);
        
        LinkAction la;
        
        PointNode upLeftNode = null;
        PointNode downRightNode = null;
        
        // If the rectangle fits the remaining space at the top (it is exactly
        // below the next point) we can ignore the upper left point.
        if (node.next == null || upLeft.y != node.next.point.y) {
            upLeftNode = new PointNode(upLeft);
        }
        
        // If the rectangle fits the space on the left (it is exactly above
        // the previous point), we can ignore the lower right point.
        if (node.prev == null || downRight.x != node.prev.point.x) {
            downRightNode = new PointNode(downRight);
        }
        
        // If the rectangle exactly fits the remaining space at the top and
        // fits the space on the left (it is exactly below the next point),
        // we can additionally replace the next node.
        if (node.next != null && upLeftNode != null &&
                downRight.y == node.next.point.y) {
            if (downRightNode == null) {
                la = new ReplaceLinkAction(node, node.next,
                        new PointNode[] {upLeftNode});
                curNode = upLeftNode;
                
            } else {
                la = new ReplaceLinkAction(node, node.next,
                        new PointNode[] {downRightNode, upLeftNode});
                curNode = downRightNode;
            }
            
        } else {
            if (downRightNode == null) {
                if (upLeftNode == null) {
                    la = new RemoveLinkAction(node);
                    // This value is non-null since upLeftNode == null,
                    // and the only way for node.next to be null is when
                    // node.next == last, and in that case upLeftNode != null.
                    curNode = node.next;
                    
                } else {
                    la = new ReplaceLinkAction(node, upLeftNode);
                    curNode = upLeftNode;
                }
                
            } else {
                if (upLeftNode == null) {
                    la = new ReplaceLinkAction(node, downRightNode);
                    
                } else {
                    la = new ReplaceLinkAction(node,
                            new PointNode[] {downRightNode, upLeftNode});
                }
                
                curNode = downRightNode;
            }
        }
        
        nodeActions.add(la);
        
        return true;
    }
    
    /**
     * Adds a minimal empty rectangle.
     * The minimal empty rectangle is such that:
     * - The lower left point is the point described by {@code node}.
     * - The y-coord of the upper right corner is described by the y-coord
     *   of the next point.
     * - The x-coord of the upper right corner is described by the minimal
     *   x-coord of the previous and next point.
     * 
     * @param node node of the point to process.
     * @return the executed action for processing the above.
     */
    private LinkAction fillAreaPointAction(PointNode node) {
        PointNode prev = node.prev;
        PointNode next = node.next;
        
        // Only when {@code node == last}.
        if (next == null) throw new IllegalStateException();
        
        int nextX = next.point.x;
        int prevX = (prev != null
                ? prev.point.x
                : Integer.MAX_VALUE);
        
        if (prevX == nextX) { // prev != null
            // If the x-coords are equal, we can additionally remove the
            // {@code next} point.
            Logger.write("[[0]]");
            Logger.write(curNode);
            
            // left x = node.point.x right x is prevX
            wastedWidth = prevX - node.point.x;
            // left bottom y = nodeY right upper y = nextY
            wastedHeight = next.point.y - node.point.y;
            
            LinkAction la = new RemoveLinkAction(node, next);
            curNode = prev;
            Logger.write(curNode);
            return la;
            
        } else if (prevX < nextX) { // prev != null
            // If the previous x-coord is smaller, we can simply remove
            // the point.
            Logger.write("[[1]]");
            Logger.write(curNode);
            
            //left x = node.point.x right x = prevX
            wastedWidth = prevX - node.point.x;
            //bottom y = node.point.y upper y = next.Y
            wastedHeight = wastedHeight = next.point.y - node.point.y;
            
            LinkAction la = new RemoveLinkAction(node);
            curNode = prev;
            Logger.write(curNode);
            return la;
            
        } else { // ==> prevX > nextX
            // If the next x-coord is smaller, then we must replace the current
            // and the next coord with a new point that lies on their lower
            // right intersection.
            PointNode newNode
                    = new PointNode(new Point(next.point.x, node.point.y));
            Logger.write("[[2]]");
            Logger.write(curNode);
            
            //left x = node.point.x right x = nextX
            wastedWidth = nextX - node.point.x;
            //bottom y = node.point.y upper y = next.Y
            wastedHeight = next.point.y - node.point.y;
            
            LinkAction la = new ReplaceLinkAction(next, node,
                    new PointNode[] {newNode});
            curNode = newNode;
            Logger.write(curNode);
            return la;
        }
    }
    
    /**
     * Checks whether the current solution is the best solution found so far.
     * If so, update {@code best} with the new found value.
     * Also update if {@code best} is {@code null}.
     */
    public void checkSolution() {
        dataset.setWidth(width);
        if (!dataset.isFixedHeight()) {
            dataset.setHeight(last.point.y);
        }
        
        if (best == null || best.getArea() > dataset.getArea()) {
            best = dataset.clone();
            Logger.write("New best: " + best.toString());
            
        } else {
            Logger.write("Sol found: " + dataset.toString());
//            if (showDataset == null) {
//                showDataset = new ShowDataset(dataset);
//            } else {
//                showDataset.setDataset(dataset);
//            }
//            MultiTool.sleepThread(500);
        }
    }
    
    @Override
    public void generateSolution(Dataset dataset) {
        this.dataset = dataset;
        doubleDataset = new IgnoreDoubleDataset(dataset);
        
        for (Dataset.Entry entry : dataset) {
            totalInputArea += entry.area();
        }
        
        // wasted space = unfillable space + area of all rectangle and
        // can therefore be initialized as totalInputArea.
        wastedSpace = totalInputArea;
        recursion();
    }
    
    /**
     * The recursive fucntion to generate the solutions.
     * All the magic happens here.
     */
    private void recursion() {
        if (best != null && last != null &&
                last.point.y * width >= best.size()) return;
        
        Logger.write("recursion!");
        Logger.write("first: " + first);
        Logger.write("last: " + last);
        printTree();
        
        PointNode[] nodes = getPoints();
        if (nodes == null) throw new IllegalStateException();
        
        for (int i = 0; i < nodes.length; i++) {
            PointNode node = nodes[i];
            // tmp
            if (node == null) {
                Logger.write("null node found!------------------");
                continue;
            }
            
            // Whether a rectangle that fits in the minimal gap has been found.
            boolean smallSolExists = false;
            // Whether there is at least one rectangle remaining.
            boolean recsAvailable = false;
            
            // The next node.
            PointNode nextNode = node.next;
            PointNode prevNode = node.prev;
            
            for (Dataset.Entry entry : doubleDataset) {
                recsAvailable = true;
                printTree();
                Logger.write("point: " + node);
                Logger.write("entry: " + entry);
                
                Rectangle rec = entry.getRec();
                boolean isSmallWidthEntryUp = nextNode == null || // If this is the last entry, true by default.
                        nextNode.point.x <= node.point.x || // Next point is on the left.
                        nextNode.point.x >= node.point.x + rec.width; // next point >= cur point + rec.
                
                boolean isSmallWidthEntryDown = prevNode == null || // If this is the first entry, true by default.
                        prevNode.point.x <= node.point.x || // Prev point is on the left (should not occur).
                        prevNode.point.x >= node.point.x + rec.width; // prev point >= cur point + rec.
                
                boolean isSmallWidthEntry = nextNode == null || // If this is the first entry, true by default.
                        (isSmallWidthEntryUp && isSmallWidthEntryDown); // If small entry for up and down, then true.
                
                curNodeStack.add(curNode);
                if (checkAndAddEntry(entry, node)) {
                    Logger.write("valid entry!");
                    
                    if (isSmallWidthEntry) {
                        // There exists at least one solution that doesn't
                        // 'stick out' compared to the next and previous point.
                        smallSolExists = true;
                    }
                    
                    recursion();
                    Logger.write("return");
                    nodeActions.pop().revert();
                }
                curNode = curNodeStack.pop();
            }
            
            // If all rectangles have been placed,
            // check the solution and return.
            if (!recsAvailable) {
                checkSolution();
                return;
            }
            
            if (!smallSolExists) {
                Logger.write("no small sol---------------------------------------------");
                LinkAction la = fillAreaPointAction(node);
                
                wastedSpaceStack.add(wastedSpace);
                int currentWastedArea = wastedWidth * wastedHeight;
                wastedSpace += currentWastedArea;
                // If wasted space exceeds the area of the best solution so far,
                // we can simply ignore filling in this area and return.
                // Note that we can also ingore possible remaining points
                // since the rectangles that should be placed are bigger
                // then this area.
                if (best != null && wastedSpace <= best.getArea()) {
                    wastedSpace = wastedSpaceStack.pop();
                    la.revert();
                    return;
                }
                
                curNodeStack.add(curNode);
                recursion();
                Logger.write("return");
                la.revert();
                curNode = curNodeStack.pop();
                wastedSpace = wastedSpaceStack.pop();
            }
            
        }
    }
    
    public void printTree() {
        Logger.write("------------");
        PointNode printNode = first;
        while (printNode != null) {
            Logger.write(printNode);
            printNode = printNode.next;
        }
        Logger.write("------------");
    }
    
    // tmp
    public static void main(String[] args) {
        // Logger setup (to disable logging, comment next line).
        
        Dataset data = new Dataset(-1, false, 6);
        //Logger.setDefaultLogger(new StreamLogger(System.out));
        long startTime = System.currentTimeMillis();
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(4, 3));
        data.add(new Rectangle(3, 4));
        data.add(new Rectangle(10, 10));
        /**//*
        data.add(new Rectangle(1, 1));
        data.add(new Rectangle(2, 2));
        data.add(new Rectangle(3, 3));
        data.add(new Rectangle(4, 4));
        data.add(new Rectangle(5, 5));
        data.add(new Rectangle(6, 6));
        /**//*
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(6, 2));
        data.add(new Rectangle(3, 4));
        data.add(new Rectangle(4, 3));
        /**//*
        data.add(new Rectangle(2, 6));
        data.add(new Rectangle(2, 6));
        //data.add(new Rectangle(6, 2));
        data.add(new Rectangle(4, 3));
        data.add(new Rectangle(3, 4));
        data.add(new Rectangle(10, 10));
        /**/
        //Dataset data = new Dataset(10, false, 1);
        //data.add(new Rectangle(10, 10));

        OptimalPointGenerator generator = new OptimalPointGenerator(null);
        Dataset result = generator.generate(data);
        System.out.println(System.currentTimeMillis() - startTime);
        MultiTool.sleepThread(200);
        System.err.println();
        System.err.println(result);
        if (generator.showDataset == null) {
            generator.showDataset = new ShowDataset(result);
        } else {
            generator.showDataset.setDataset(result);
        }
    }
    
}
