import java.awt.Rectangle;
import java.util.*;

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

        boolean isEmpty;

        public Space(int x, int y, int width, int height, Space left, Space right, Space top, Space bottom, boolean isEmpty) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.left = left;
            this.right = right;
            this.top = top;
            this.bottom = bottom;
            this.isEmpty = isEmpty;
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

    int width;
    int height;
    Space root;

    public GreedyPacker(int width, int height) {
        this.width = width;
        this.height = height;
        this.root = new Space(0, 0, width, height);
    }

    public void splitRow(Space space, int offset) {
        List<Space> spaces = new ArrayList<>();
        Space previous = space.getLeft();
        while (previous != null) {
            spaces.add(previous);
            previous = previous.getLeft();
        }
        Collections.reverse(spaces);
        do {
            spaces.add(space);
            space = space.getRight();
        } while (space != null);

        List<Space> newSpaces = new ArrayList<>();
        for (Space oldSpace : spaces) {
            Space newSpace = new Space(
                    oldSpace.x, oldSpace.y + offset, oldSpace.width, oldSpace.height - offset,
                    null, null, oldSpace.top, oldSpace, oldSpace.isEmpty
            );
            newSpaces.add(newSpace);
            if (newSpace.top != null) newSpace.top.setBottom(newSpace);
            oldSpace.setTop(newSpace);
            oldSpace.setHeight(offset);
        }
        for (int i = 0; i < newSpaces.size() - 1; i++) {
            newSpaces.get(i).setRight(newSpaces.get(i + 1));
            newSpaces.get(i + 1).setLeft(newSpaces.get(i));
        }
    }

    public void splitColumn(Space space, int offset) {
        List<Space> spaces = new ArrayList<>();
        Space previous = space.getBottom();
        while (previous != null) {
            spaces.add(previous);
            previous = previous.getBottom();
        }
        Collections.reverse(spaces);
        do {
            spaces.add(space);
            space = space.getTop();
        } while (space != null);

        List<Space> newSpaces = new ArrayList<>();
        for (Space oldSpace : spaces) {
            Space newSpace = new Space(
                    oldSpace.x + offset, oldSpace.y, oldSpace.width - offset, oldSpace.height,
                    oldSpace, oldSpace.right, null, null, oldSpace.isEmpty
            );
            newSpaces.add(newSpace);
            if (newSpace.right != null) newSpace.right.setLeft(newSpace);
            oldSpace.setRight(newSpace);
            oldSpace.setWidth(offset);
        }
        for (int i = 0; i < newSpaces.size() - 1; i++) {
            newSpaces.get(i).setTop(newSpaces.get(i + 1));
            newSpaces.get(i + 1).setBottom(newSpaces.get(i));
        }
    }

    public void insertEntry(Rectangle rect, Space space) {
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
        }
    }

    public boolean fitEntry(Dataset.Entry entry) {
        Space columnStart = root;
        do {
            Space next = columnStart;
            do {
                Rectangle rect = entry.getNormalRec();
                if (next.checkRectangle(rect)) {
                    entry.setLocation(next.getX(), next.getY());
                    insertEntry(rect, next);
                    return true;
                }
                next = next.getTop();
            } while (next != null);
            columnStart = columnStart.getRight();
        } while (columnStart != null);
        return false;
    }

    @Override
    public Dataset pack(Dataset dataset) {
        Dataset clone = dataset.clone();
        clone.setSize(width, height);
        for (Dataset.Entry entry : clone.sorted()) {
            if (!fitEntry(entry)) {
                return null;
            }
        }
        return clone;
    }
}
