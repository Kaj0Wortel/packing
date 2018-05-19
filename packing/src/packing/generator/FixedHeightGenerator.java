package packing.generator;

import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;

import java.util.Arrays;
import java.util.Comparator;

public class FixedHeightGenerator extends Generator {
    public FixedHeightGenerator(PackerFactory factory) {
        super(factory);
    }

    @Override
    public void generateSolution(Dataset dataset) {
        dataset.setRotation(Dataset.NO_ROTATION);

        int height = dataset.getHeight();
        int width = Integer.MAX_VALUE;
        int minArea = 0;
        int numPacked = 0;

        for (Dataset.Entry entry : dataset) {
            minArea += entry.getRec().width * entry.getRec().height;
        }

        if (minArea % height != 0) {
            minArea = minArea - (minArea % height) + height;
        }

        for (Comparator<Dataset.Entry> comparator : Arrays.asList(Dataset.SORT_HEIGHT, Dataset.SORT_AREA, Dataset.SORT_WIDTH, Dataset.SORT_LONGEST_SIDE)) {
            dataset.setOrdering(comparator);
            Packer packer = packerFactory.create(width, height);
            Dataset packed = packer.pack(dataset);
            numPacked++;

            if (packed != null) {
                if (best == null || packed.getArea() < best.getArea()) {
//                    System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", packed.getWidth(), packed.getHeight(),
//                            100 * (packed.getArea() - minArea) / (double) packed.getArea());
                    best = packed;
                    width = packed.getWidth();
                }
            }
        }

        try {
            while (height * width > minArea) {
                dataset.shuffle();
                dataset.setRotation(Dataset.RANDOM_ROTATION);

                Packer packer = packerFactory.create(width, height);
                Dataset packed = packer.pack(dataset);
                numPacked++;

                if (packed != null) {
                    if (packed.getArea() < best.getArea()) {
//                        System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", packed.getWidth(), packed.getHeight(),
//                                100 * (packed.getArea() - minArea) / (double) packed.getArea());
                        best = packed;
                        width = packed.getWidth();
                    }
                }
            }
        } finally {
//            System.err.printf("Generated %d packings...\n", numPacked);
        }
    }
}
