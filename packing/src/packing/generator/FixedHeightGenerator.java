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
        // %%explaination needed%%
        // %%THIS IS NOT ALLOWED!!!
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
        
        System.err.printf("Found initial solution: [%d x %d] (%.5f%% wasted space)\n", best.getWidth(), best.getHeight(),
                100 * (best.getArea() - minArea) / (double) best.getArea());
        
        // %%explaination needed%%
        while (height * width > minArea) {
            // Random Search
            dataset.shuffle();
            dataset.setRotation(Dataset.RANDOM_ROTATION);
            
            Packer packer = packerFactory.create(width, height);
            Dataset packed = packer.pack(dataset);
            
            if (packed != null) {
                if (packed.getArea() < best.getArea()) {
                    System.err.printf("Found new solution: [%d x %d] (%.5f%% wasted space)\n", packed.getWidth(), packed.getHeight(),
                            100 * (packed.getArea() - minArea) / (double) packed.getArea());
                    best = packed;
                    width = packed.getWidth();
                }
            }
        }

    }
}
