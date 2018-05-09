
package packing.packer;


public class GreedyPackerFactory extends PackerFactory {
    @Override
    public Packer create(int width, int height) {
        return new GreedyPacker(width, height);
    }
}
