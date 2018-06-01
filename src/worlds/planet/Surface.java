package worlds.planet;

import com.jme3.math.Vector2f;
import java.util.concurrent.atomic.AtomicLong;

import engine.surface.SurfaceMap;
import engine.util.Vec2;
import engine.util.task.BasicTask;
import engine.util.task.Task;
import java.util.ArrayList;
import java.util.List;
import worlds.MinMaxHeightFactory;

import static worlds.planet.Util.checkBounds;

/**
 * The first abstraction for the surface of a Planet.
 * @author Richard DeSilvey
 */
public abstract class Surface extends SurfaceMap<PlanetCell> {

  /**
   * The number of years that pass for each step of erosion
   */
  public static long GEOUPDATE;

  private long geologicalTimeStamp;

  /**
   * The age of the planet in years
   */
  public static AtomicLong planetAge;
  
  /**
   * The number of years that pass for each update to the geosphere
   */
  public static long timeStep;

  public final static int HEIGHTMAP = 0;
  public final static int STRATAMAP = 1;
  public final static int LANDOCEAN = 2;

  private static final int DEFAULT_THREAD_DELAY = 50;

  private final MinMaxHeightFactory mhFactory;

  static {
    timeStep = 7125000;
  }

  /**
   * Constructs a new Surface with an empty map.
   *
   * @param planetWidth The size of the surface
   * @param ageStepDelay The amount of time to delay updating planet age
   * @param threadsDelay The amount of time to delay each frame in milliseconds.
   * @param threadCount The number of threads that will work on the map
   */
  public Surface(int planetWidth, int ageStepDelay, int threadsDelay, int threadCount) {
    super(planetWidth, DEFAULT_THREAD_DELAY);
    setupThreads(threadCount, threadsDelay);
    setupDefaultMap(planetWidth + 1, threadCount);
    mhFactory = new MinMaxHeightFactory(this);
    produceTasks(mhFactory);
    reset();
  }

  /**
   * Resets the surface to an empty map and resets the planet's age. This method
   * should be calling the <code>buildMap()</code> method.
   */
  @Override
  public final void reset() {
    planetAge = new AtomicLong(0);
    geologicalTimeStamp = 0;
    buildMap();
  }

  public long getPlanetAge() {
    return planetAge.get();
  }

  public void updatePlanetAge() {
    long curPlanetAge = planetAge.getAndAdd(timeStep);
    if (curPlanetAge - geologicalTimeStamp > GEOUPDATE) {
      geologicalTimeStamp = curPlanetAge;
    }
  }

  @Override
  public PlanetCell generateCell(int x, int y) {
    return new PlanetCell(x, y);
  }

  public float getHighestHeight() {
    return mhFactory.getHighestHeight();
  }

  public float getLowestHeight() {
    return mhFactory.getLowestHeight();
  }

}
