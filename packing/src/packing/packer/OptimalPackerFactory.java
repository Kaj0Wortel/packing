package packing.packer;

public class OptimalPackerFactory extends PackerFactory {
    @Override
    public Packer create(int width, int height) {
        Packer packer = new XCoordinatePacker(new YCoordinatePacker());

        if (width > height) {
            packer = new RotatedPackingTransformer(packer);
        }

        return packer;
    }
}
