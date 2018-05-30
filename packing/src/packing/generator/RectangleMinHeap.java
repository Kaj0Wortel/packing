package packing.generator;
/**
 * A class to represent a min-heap for rectangles based on their area
 * An adaptation of https://gist.github.com/flexelem/70b120ac9bf2965f419f
 * made to work for rectangles.
 */

import java.awt.Rectangle;
import java.util.ArrayList;

public class RectangleMinHeap {

    private ArrayList<Rectangle> list;

    public RectangleMinHeap() {

        this.list = new ArrayList<Rectangle>();
    }

    public RectangleMinHeap(ArrayList<Rectangle> items) {

        this.list = items;
        buildHeap();
    }

    public void insert(Rectangle item) {

        list.add(item);
        int i = list.size() - 1;
        int parent = parent(i);

        while (parent != i && 
                (list.get(i).getWidth() * list.get(i).getHeight()) // area of current rectangle
                < (list.get(parent).getWidth() * list.get(parent).getHeight())) { // area of parent rectangle

            swap(i, parent);
            i = parent;
            parent = parent(i);
        }
        //System.out.println(item.width + "Width and Height" + item.height);
          //  System.out.println(list.get(0));
    }

    public void buildHeap() {

        for (int i = list.size() / 2; i >= 0; i--) {
            minHeapify(i);
        }
    }

    public Rectangle extractMin() {

        if (list.size() == 0) {

            throw new IllegalStateException("MinHeap is EMPTY");
        } else if (list.size() == 1) {

            Rectangle min = list.remove(0);
            //System.out.println(min + "min and item" + list.get(0));
            return min;
        }

        // remove the last item ,and set it as new root
        Rectangle min = list.get(0);
      // System.out.println(min + "min");
        //System.out.println(min.height);
        Rectangle lastItem = list.remove(list.size() - 1);
        list.set(0, lastItem);

        // bubble-down until heap property is maintained
        minHeapify(0);

        // return min key
        return min;
    }

    //Our implementation will never need to decrease a key, since we use rectangles
    //whose areas only increase
    /*public void decreaseKey(int i, int key) {

        if (list.get(i) < key) {

            throw new IllegalArgumentException("Key is larger than the original key");
        }

        list.set(i, key);
        int parent = parent(i);

        // bubble-up until heap property is maintained
        while (i > 0 && list.get(parent) > list.get(i)) {

            swap(i, parent);
            i = parent;
            parent = parent(parent);
        }
    }*/

    private void minHeapify(int i) {

        int left = left(i);
        int right = right(i);
        int smallest = -1;

        // find the smallest rectangle area between current node and its children.
        if (left <= list.size() - 1 && 
                (list.get(left).getWidth() * list.get(left).getHeight()) // area of left rectangle 
                < (list.get(i).getWidth() * list.get(i).getHeight())) {  // area of current rectangle
            smallest = left;
        } else {
            smallest = i;
        }

        if (right <= list.size() - 1 && 
                (list.get(right).getWidth() * list.get(right).getHeight()) // area of right rectangle 
                < (list.get(smallest).getWidth() * list.get(smallest).getHeight())) { // area of currently smallest rectangle
            smallest = right;
        }

        // if the smallest key is not the current key then bubble-down it.
        if (smallest != i) {

            swap(i, smallest);
            minHeapify(smallest);
        }
    }

    public Rectangle getMin() {

        return list.get(0);
    }

    public boolean isEmpty() {

        return list.size() == 0;
    }

    private int right(int i) {

        return 2 * i + 2;
    }

    private int left(int i) {

        return 2 * i + 1;
    }

    private int parent(int i) {

        if (i % 2 == 1) {
            return i / 2;
        }

        return (i - 1) / 2;
    }

    private void swap(int i, int parent) {

        Rectangle temp = list.get(parent);
        list.set(parent, list.get(i));
        list.set(i, temp);
    }

}