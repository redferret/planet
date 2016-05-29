package planet;

import planet.util.Tools;

/**
 * This class is used for testing purposes.
 *
 * @author Richard DeSilvey
 */
public class TestWorld extends Planet {

    private static final int CELL_SIZE_M, DEFAULT_SIZE,
            PLANET_DELAY, DEFAULT_THREAD_COUNT, AGE_STEP_DELAY;

    static {
        CELL_SIZE_M = 12;
        DEFAULT_THREAD_COUNT = 1;
        PLANET_DELAY = 1;
        AGE_STEP_DELAY = 250;
        DEFAULT_SIZE = 256;
    }

    /**
     * Constructs a default test world with a size of 256 with one thread.
     */
    public TestWorld() {
        this(DEFAULT_THREAD_COUNT);
    }

    /**
     * Constructs a new test world for the simulation with a given thread 
     * divisor on a default sized world.
     *
     * @param threadCount The size of the world squared.
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
        super(worldSize * threadCount, CELL_SIZE_M, AGE_STEP_DELAY, PLANET_DELAY, threadCount);
        startThreads();
    }
}
