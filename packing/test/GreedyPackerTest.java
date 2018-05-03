public class GreedyPackerTest extends PackerTest {

    public GreedyPackerTest() {
    }

    @Override
    protected Packer createInstance(int width, int height) {
        return new GreedyPacker(width, height);
    }
}
