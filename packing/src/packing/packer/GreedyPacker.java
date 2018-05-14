
package packing.packer;


// Packing imports
import packing.data.*;


import java.awt.Rectangle;
import java.util.SortedSet;
import java.util.TreeSet;

class GreedyPacker extends Packer {
    class Space {
        int x = 0;
        int y = 0;
        int width = 0;
        int height = 0;

        Space left = null;
        Space right = null;
        Space top = null;
        Space bottom = null;

        Space bottomRoot = null;
        Space leftRoot = null;

        boolean isEmpty;

        public Space(int x, int y, int width, int height, Space left, Space right, Space top, Space bottom, boolean isEmpty) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            setLeft(left);
            setRight(right);
            setTop(top);
            setBottom(bottom);
            this.isEmpty = isEmpty;
            this.bottomRoot = (bottom == null) ? this : bottom.bottomRoot;
            this.leftRoot = (left == null) ? this : left.leftRoot;
        }

        public Space(int x, int y, int width, int height) {
            this(x, y, width, height, null, null, null, null, true);
        }

        public String toString() {
            return String.format("Space<x=%d, y=%d, width=%d, height=%d, %s>", x, y, width, height, (isEmpty) ? "empty" : "filled");
        }

        /**
         * Check if rectangle fits at bottom-left corner.
         */
        public boolean checkRectangle(Rectangle rect) {
            if (!isEmpty) return false;

            if (rect.width <= this.width && rect.height <= this.height) {
                return true;
            } else if (rect.width <= this.width) {
                return getTop() != null && getTop().checkRectangle(new Rectangle(rect.width, rect.height - this.height));
            } else if (rect.height <= this.height) {
                return getRight() != null && getRight().checkRectangle(new Rectangle(rect.width - this.width, rect.height));
            } else {
                // First split into left/right, then split remaining rectangle in top/bottom and check them.
                return getRight() != null && getTop() != null
                        && getRight().checkRectangle(new Rectangle(rect.width - this.width, rect.height))
                        && getTop().checkRectangle(new Rectangle(width, rect.height - this.height));
            }
        }

        /**
         * @return the x
         */
        public int getX() {
            return x;
        }

        /**
         * @param x the x to set
         */
        public void setX(int x) {
            this.x = x;
        }

        /**
         * @return the y
         */
        public int getY() {
            return y;
        }

        /**
         * @param y the y to set
         */
        public void setY(int y) {
            this.y = y;
        }

        /**
         * @return the width
         */
        public int getWidth() {
            return width;
        }

        /**
         * @param width the width to set
         */
        public void setWidth(int width) {
            this.width = width;
        }

        /**
         * @return the height
         */
        public int getHeight() {
            return height;
        }

        /**
         * @param height the height to set
         */
        public void setHeight(int height) {
            this.height = height;
        }

        /**
         * @return the bottom
         */
        public Space getBottom() {
            return bottom;
        }

        /**
         * @param bottom the bottom to set
         */
        public void setBottom(Space bottom) {
            this.bottom = bottom;
            if (bottom != null) {
                bottom.top = this;
            }
        }

        /**
         * @return the top
         */
        public Space getTop() {
            return top;
        }

        /**
         * @param top the top to set
         */
        public void setTop(Space top) {
            this.top = top;
            if (top != null) {
                top.bottom = this;
            }
        }

        /**
         * @return the left
         */
        public Space getLeft() {
            return left;
        }

        /**
         * @param left the left to set
         */
        public void setLeft(Space left) {
            this.left = left;
            if (left != null) {
                left.right = this;
            }
        }

        /**
         * @return the right
         */
        public Space getRight() {
            return right;
        }

        /**
         * @param right the right to set
         */
        public void setRight(Space right) {
            this.right = right;
            if (right != null) {
                right.left = this;
            }
        }

        /**
         * @return is empty
         */
        public boolean isEmpty() {
            return isEmpty;
        }

        public void setEmpty(boolean empty) {
            isEmpty = empty;
        }
    }

    private int width;
    private int height;
    private Space root;
    private SortedSet<Space> queue = new TreeSet<>((o1, o2) -> {
        return (Integer.compare(o1.x, o2.x) != 0) ? Integer.compare(o1.x, o2.x) : Integer.compare(o1.y, o2.y);
    });

    public GreedyPacker(int width, int height) {
        this.width = width;
        this.height = height;
        this.root = new Space(0, 0, width, height);
        this.queue.add(this.root);
    }

    private void splitRow(Space space, int offset) {
        Space previous = null;
        for (space = space.leftRoot; space != null; space = space.getRight()) {
            Space newSpace = new Space(
                    space.x, space.y + offset, space.width, space.height - offset,
                    previous, null, space.top, space, space.isEmpty
            );
            if (newSpace.isEmpty) queue.add(newSpace);
            space.setHeight(offset);
            previous = newSpace;
        }
    }

    private void splitColumn(Space space, int offset) {
        Space previous = null;
        for (space = space.bottomRoot; space != null; space = space.getTop()) {
            Space newSpace = new Space(
                    space.x + offset, space.y, space.width - offset, space.height,
                    space, space.right, null, previous, space.isEmpty
            );
            if (newSpace.isEmpty) queue.add(newSpace);
            space.setWidth(offset);
            previous = newSpace;
        }
    }

    private void insertEntry(Rectangle rect, Space space) {
        if (rect.width < space.width) {
            splitColumn(space, rect.width);
        }
        if (rect.height < space.height) {
            splitRow(space, rect.height);
        }
        if (rect.width > space.width) {
            insertEntry(new Rectangle(space.width, rect.height), space);
            insertEntry(new Rectangle(rect.width - space.width, rect.height), space.getRight());
        } else if (rect.height > space.height) {
            insertEntry(new Rectangle(rect.width, space.height), space);
            insertEntry(new Rectangle(rect.width, rect.height - space.height), space.getTop());
        } else {
            // Perfect fit
            space.setEmpty(false);
            queue.remove(space);
        }
    }

    private boolean fitEntry(Dataset.Entry entry) {
        Rectangle rect = entry.getRec();
        for (Space space : queue) {
            if (space.checkRectangle(rect)) {
                entry.setLocation(space.getX(), space.getY());
                insertEntry(rect, space);
                return true;
            }
        }
        return false;
    }

    @Override
    public Dataset pack(Dataset dataset) {
        Dataset clone = dataset.clone();
        clone.setSize(width, height);
        for (Dataset.Entry entry : clone.sorted()) {
            if (!fitEntry(entry)) {
                if (dataset.allowRotation()) {
                    entry.setRotation(!entry.useRotation());
                }
                return null;
            }
        }
        return clone;
    }
}
