package worlds.planet.geosphere;

import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;
import engine.util.concurrent.AtomicFloat;
import java.util.concurrent.atomic.AtomicLong;
import worlds.MinMaxHeightFactory;
import worlds.planet.geosphere.tasks.CrustConduction;

/**
 * Contains all logic that works on the geology of the planet.
 *
 * @author Richard DeSilvey
 */
public class Lithosphere extends SurfaceMap<Crust> {

  private long ageStamp;
  private final MinMaxHeightFactory mhFactory;
  
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

  static {
    timeStep = 7125000;
  }

  /**
   * Constructs a new Surface with an empty map.
   *
   * @param totalSize The size of the surface
   * @param surfaceThreads Reference to the surface threads
   */
  public Lithosphere(int totalSize, SurfaceThreads surfaceThreads) {
    super("Lithosphere", totalSize, surfaceThreads);
    setupDefaultMap(surfaceThreads.getThreadCount());
    mhFactory = new MinMaxHeightFactory(this);
    surfaceThreads.produceTasks(mhFactory);
    ageStamp = 0;
    reset();
  }

  public void setupConduction(UpperMantle upperMantle) {
    getSurfaceThreads().produceTasks(() -> {
      return new CrustConduction(this, upperMantle);
    });
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
  public Crust generateCell(int x, int y) {
    return new Crust(x, y);
  }

  public float getHighestHeight() {
    return mhFactory.getHighestHeight();
  }

  public float getLowestHeight() {
    return mhFactory.getLowestHeight();
  }

  public long getAgeStamp() {
    return ageStamp;
  }

  public void setAgeStamp(long ageStamp) {
    this.ageStamp = ageStamp;
  }

}
