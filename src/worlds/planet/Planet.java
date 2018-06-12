package worlds.planet;

import com.jme3.math.Vector2f;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.surface.SurfaceMap;
import engine.concurrent.SurfaceThreads;
import engine.surface.TerrainSurface;
import java.util.List;
import worlds.planet.geosphere.Core;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.UpperMantle;
import worlds.planet.geosphere.tasks.ApplyNewTemperatures;
import worlds.planet.geosphere.tasks.HotSpotManager;
import worlds.planet.geosphere.tasks.MagmaFlow;
import worlds.planet.geosphere.tasks.RadioactiveDecay;

/**
 * The class that encapsulates a surface and keeps track of the timescale.
 *
 * @author Richard DeSilvey
 */
public abstract class Planet {

  protected TimeScale timescale;
  private static Planet current;
  private final SurfaceThreads surfaceThreads;
  private final Lithosphere lithosphere;
  private final UpperMantle upperMantle;
  private final Core core;
  private final int terrainWidth;
  
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
   * @param surfaceThreadsDelay The delay for each thread in milliseconds
   * @param threadCount The number of threadReferences that work on the map
   */
  public Planet(int totalSize, int cellLength, int surfaceThreadsDelay, int threadCount) {
    Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "New Planet");
    current = this;
    PlanetCell.area = cellLength * cellLength;
    PlanetCell.length = cellLength;
    terrainWidth = totalSize;
    timescale = TimeScale.None;
    surfaceThreads = new SurfaceThreads();
    surfaceThreads.setupThreads(totalSize, threadCount, surfaceThreadsDelay);
    
    lithosphere = new Lithosphere(totalSize, surfaceThreads);
    upperMantle = new UpperMantle(totalSize, surfaceThreads);
    core = new Core(totalSize, surfaceThreads);
    
    upperMantle.setupConduction(core, lithosphere);
    lithosphere.setupConduction(upperMantle);
    core.setupConduction(upperMantle);
    
    applyTasks(lithosphere, upperMantle, core);
    
    surfaceThreads.produceTasks(() -> {
      return new RadioactiveDecay(lithosphere, upperMantle);
    });
    surfaceThreads.produceTasks(() -> {
      return new HotSpotManager(core);
    });
    surfaceThreads.produceTasks(() -> {
      return new MagmaFlow(upperMantle);
    });
    
    List<Vector2f> positions = Util.fillPoints(new Vector2f(64, 64), 30);
    positions.forEach(position -> {
      upperMantle.getCellAt(position).addToMagma(3000);
    });
  }

  public int getTerrainWidth() {
    return terrainWidth;
  }
  
  public final void applyTasks(SurfaceMap... surfaces) {
    for (SurfaceMap surface : surfaces) {
      surfaceThreads.produceTasks(() -> {
        return new ApplyNewTemperatures(surface);
      });
    }
  }
  
  protected final void startThreads() {
    surfaceThreads.startThreads();
  }

  public void setIsPaused(boolean paused) {
    if (paused) {
      surfaceThreads.pauseThreads();
    } else {
      surfaceThreads.playThreads();
    }
  }

  public final void shutdown() {
    surfaceThreads.killAllThreads();
  }
  
  public Lithosphere getLithosphere() {
    return lithosphere;
  }
  
  public UpperMantle getUpperMantle() {
    return upperMantle;
  }
  
  public Core getCore() {
    return core;
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
