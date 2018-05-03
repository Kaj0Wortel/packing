import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public abstract class PackerTest {

    protected Packer instance;
    protected Dataset dataset;

    public PackerTest() {
    }

    abstract Packer createInstance(int width, int height);

    @BeforeEach
    public void setUp() {
        dataset = new TestDataset(-1, false, 1);
        dataset.add(new Rectangle(5, 10));
    }

    @Test
    public void testPackDataset() {
        instance = createInstance(10, 10);
        Dataset packed = instance.pack(dataset);
        assertNotNull(packed);
        // Make sure it's a clone
        assertNotEquals(packed, dataset);
    }

    @Test
    public void testPackNullOnFailure() {
        instance = createInstance(2, 2);
        Dataset packed = instance.pack(dataset);
        assertNull(packed);
    }
}