public class SheetPackerFactory extends PackerFactory {
    @Override
    public Packer create(int width, int height) {
        return new Sheet(width, height);
    }
}
