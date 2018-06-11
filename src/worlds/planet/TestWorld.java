package worlds.planet;

import engine.surface.TerrainSurface;

/**
 * This class is used for testing purposes.
 *
 * @author Richard DeSilvey
 */
public class TestWorld extends Planet {

  public static  int CELL_LENGTH, DEFAULT_CELL_COUNT,
          SURFACE_THREAD_DELAY, DEFAULT_THREAD_COUNT;

  static {
    DEFAULT_THREAD_COUNT = 2;
    SURFACE_THREAD_DELAY = 1;
    DEFAULT_CELL_COUNT = 64;
    CELL_LENGTH = 100000;
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
   * @param worldSize The size chunk for each thread, must be even (i.e. 32, 64,
   * 128, 512 etc.)
   * @param threadCount The number of thread divisions
   */
  public TestWorld(int worldSize, int threadCount) {
    super((worldSize * threadCount), CELL_LENGTH, SURFACE_THREAD_DELAY, threadCount);
    startThreads();
  }

}
