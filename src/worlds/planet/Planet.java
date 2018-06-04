package worlds.planet;

import java.util.logging.Level;
import java.util.logging.Logger;

import engine.surface.SurfaceMap;

/**
 * The class that encapsulates a surface and keeps track of the timescale.
 *
 * @author Richard DeSilvey
 */
public abstract class Planet {

  protected TimeScale timescale;
  private static Planet current;
  private final PlanetSurface planetSurface;

  public static enum TimeScale {
    Geological, Evolutionary, Civilization, None
  }

  static {
    current = null;
  }

  /**
   * Constructs a new Planet.
   *
   * @param totalSize The number of cells of one side of the surface (width) + 1
   * @param cellLength The length of one side of a cell in meters.
   * @param surfaceThreadsDelay How fast does the planet thread(s) update
   * @param threadCount The number of threadReferences that work on the map
   */
  public Planet(int totalSize, int cellLength, int surfaceThreadsDelay, int threadCount) {
    Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "New Planet");
    current = this;
    PlanetCell.area = cellLength * cellLength;
    PlanetCell.length = cellLength;
    timescale = TimeScale.None;
    planetSurface = new PlanetSurface(totalSize, surfaceThreadsDelay, threadCount);
  }

  protected final void startThreads() {
    planetSurface.startThreads();
  }

  public void setIsPaused(boolean paused) {
    if (paused) {
      planetSurface.pauseThreads();
    } else {
      planetSurface.playThreads();
    }
  }

  public final void shutdown() {
    planetSurface.killAllThreads();
  }
  
  public PlanetSurface getSurface() {
    return planetSurface;
  }

  /**
   * References the most recent instantiated instance of this class.
   *
   * @return A reference to the current Planet
   */
  public final static Planet instance() {
    return current;
  }

  public boolean isTimeScale(TimeScale scale) {
    return scale == timescale;
  }

  public final TimeScale getTimeScale() {
    return timescale;
  }

  public void setTimescale(TimeScale timescale) {
    this.timescale = timescale;
  }
  
}
