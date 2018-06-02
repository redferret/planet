package worlds.planet;

/**
 * This class is used for testing purposes.
 *
 * @author Richard DeSilvey
 */
public class TestWorld extends Planet {

  public static  int CELL_LENGTH, DEFAULT_CELL_COUNT,
          SURFACE_THREAD_DELAY, DEFAULT_THREAD_COUNT, PLANET_SURFACE_DELAY;

  static {
    CELL_LENGTH = 200;
    DEFAULT_THREAD_COUNT = 2;
    SURFACE_THREAD_DELAY = 1;
    PLANET_SURFACE_DELAY = 1125;
    DEFAULT_CELL_COUNT = 128;
  }

  /**
   * Constructs a default test world.
   */
  public TestWorld() {
    this(DEFAULT_THREAD_COUNT);
  }

  /**
   * Constructs a new test world for the simulation with a given thread divisor
   * on a default sized world.
   *
   * @param threadCount The number of threads for a default sized world.
   */
  public TestWorld(int threadCount) {
    this(DEFAULT_CELL_COUNT, threadCount);
  }

  /**
   * Constructs a new test world for the simulation with a specified number of
   * threads. Each thread will get a chunk size.
   *
   * @param worldSize The size chunk for each thread
   * @param threadCount The number of thread divisions
   */
  public TestWorld(int worldSize, int threadCount) {
    super(worldSize * threadCount, CELL_LENGTH, PLANET_SURFACE_DELAY, SURFACE_THREAD_DELAY, threadCount);
    startThreads();
  }
}
