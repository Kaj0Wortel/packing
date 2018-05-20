package packing.generator;

import packing.data.Dataset;
import packing.packer.Packer;
import packing.packer.PackerFactory;

public class FixedHeightGenerator extends Generator {
    public FixedHeightGenerator(PackerFactory factory) {
        super(factory);
    }

    @Override
    public void generateSolution(Dataset dataset) {
        dataset.setRotation(Dataset.NO_ROTATION);

        int height = dataset.getHeight();
        int width;
        int minArea = 0;

        for (Dataset.Entry entry : dataset) {
            minArea += entry.getRec().width * entry.getRec().height;
        }

        if (minArea % height != 0) {
            minArea = minArea - (minArea % height) + height;
        }

        best = generateUpperBound(dataset);
        width = best.getWidth();

        while (height * width > minArea) {
            // Random Search
            dataset.shuffle();
            dataset.setRotation(Dataset.RANDOM_ROTATION);

            Packer packer = packerFactory.create(width, height);
            Dataset packed = packer.pack(dataset);

            if (packed != null) {
                if (packed.getArea() < best.getArea()) {
                    best = packed;
                    width = packed.getWidth();
                }
            }
        }

    }
}
