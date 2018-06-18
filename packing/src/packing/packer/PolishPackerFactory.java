
package packing.packer;

/**
 * Factory class for the polish packer.
 */
public class PolishPackerFactory
        extends PackerFactory {
    @Override
    public Packer create(int width, int height) {
        return new PolishPacker();
    }
}
