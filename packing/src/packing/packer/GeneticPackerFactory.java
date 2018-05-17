
package packing.packer;

/**
 * Factory class to create a GeneticPacker.
 */
public class GeneticPackerFactory extends PackerFactory {
    
    @Override
    public Packer create(int width, int height) {
        return new GeneticPacker(width, height);
    }
}
