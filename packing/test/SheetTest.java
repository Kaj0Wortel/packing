public class SheetTest extends PackerTest {
    @Override
    Packer createInstance(int width, int height) {
        return new Sheet(width, height);
    }
}
