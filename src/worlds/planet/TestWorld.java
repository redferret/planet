package worlds.planet;

/**
 * This class is used for testing purposes.
 *
 * @author Richard DeSilvey
 */
public class TestWorld extends Planet {

    private static final int CELL_SIZE, DEFAULT_SIZE,
            SURFACE_THREAD_DELAY, DEFAULT_THREAD_COUNT, PLANET_SURFACE_DELAY;

    static {
        CELL_SIZE = 10;
        DEFAULT_THREAD_COUNT = 1;
        SURFACE_THREAD_DELAY = 1;
        PLANET_SURFACE_DELAY = 250;
        DEFAULT_SIZE = 200;
    }

    /**
     * Constructs a default test world.
     */
    public TestWorld() {
        this(DEFAULT_THREAD_COUNT);
    }

    /**
     * Constructs a new test world for the simulation with a given thread 
     * divisor on a default sized world.
     *
     * @param threadCount The number of threads for a default sized world.
     */
    public TestWorld(int threadCount) {
        this(DEFAULT_SIZE, threadCount);
    }
    
    /**
     * Constructs a new test world for the simulation with a specified number
     * of threads. Each thread will get a chunk size.
     *
     * @param worldSize The size chunk for each thread
     * @param threadCount The number of thread divisions
     */
    public TestWorld(int worldSize, int threadCount) {
        super(worldSize * threadCount, CELL_SIZE, PLANET_SURFACE_DELAY, SURFACE_THREAD_DELAY, threadCount);
        startThreads();
    }
}
