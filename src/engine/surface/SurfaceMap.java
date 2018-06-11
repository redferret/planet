package engine.surface;

import engine.concurrent.SurfaceThreads;
import com.jme3.math.Vector2f;
import engine.surface.tasks.SetParentThreads;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import static worlds.planet.geosphere.Lithosphere.planetAge;

/**
 * The SurfaceMap is a generic map for all the systems on the planet. The map
 * contains generic cells that implement the Cell class. 
 * A SurfaceMap by default doesn't setup any surface threads, therefore
 * the <code>setupThreads(int threadDivision, int delay)</code> needs to be
 * called after this super class is created. This class also extends
 * AbstractHeightMap to render the surface.
 *
 * @author Richard DeSilvey
 * @param <C> The highest level abstraction of the cell i.e. PlanetCell
 */
public abstract class SurfaceMap<C extends Cell> {
  
  /**
   * The direction look up list for X values
   */
  public static final int[] DIR_X_INDEX = {-1, 0, 1, 1, 1, 0, -1, -1};
  /**
   * The direction look up list for Y values
   */
  public static final int[] DIR_Y_INDEX = {-1, -1, -1, 0, 1, 1, 1, 0};
  /**
   * The direction look up list for X values
   */
  public static final int[] HDIR_X_INDEX = {0, 1, 0, -1};
  /**
   * The direction look up list for Y values
   */
  public static final int[] HDIR_Y_INDEX = {-1, 0, 1, 0};

  public int displaySetting;

  private final String surfaceName;
  
  /**
   * The map containing the references to each data point on the surface.
   */
  protected Map<Integer, C> map;
  
  /*
    Threads
  */
  private final SurfaceThreads surfaceThreads;
//  private final MThread hotSpotThread;
  private final int totalSize;
  private final String name;
  /**
   * Create a new SurfaceMap. SurfaceThreads and Map need to be initialized
   * separably.
   *
   * @param surfaceName The name of this surface. i.e. Geosphere
   * @param totalSize The size of this entire terrain (on one side). Power of 2
   * plus 1 (eg. 513, 1025, 2049...)
   * @param surfaceThreads
   */
  public SurfaceMap(String surfaceName, int totalSize, SurfaceThreads surfaceThreads) {
    this.totalSize = totalSize;
    this.name = surfaceName;
    this.surfaceName = surfaceName;
    displaySetting = 0;
    this.surfaceThreads = surfaceThreads;
  }
  
  public String getSurfaceName() {
    return name;
  }

  public SurfaceThreads getSurfaceThreads() {
    return surfaceThreads;
  }
  
  public void reset() {
    planetAge = new AtomicLong(0);
    surfaceThreads.pauseThreads();
    buildMap();
    surfaceThreads.produceTasks(() -> {
      return new SetParentThreads(this);
    });
    surfaceThreads.playThreads();
  }
  
  /**
   * Using a ConcurrentHashMap as the Map data structure.
   *
   * @param threadCount The number of threads being used.
   */
  protected void setupDefaultMap(int threadCount) {
    final float loadFactor = 1.0f;
    int terrainSize = getSize();
    int totalCells = (terrainSize * terrainSize);
    Map<Integer, C> defaultMap = new ConcurrentHashMap<>(totalCells, loadFactor, threadCount);
    setMap(defaultMap);
  }

  public void setMap(Map<Integer, C> map) {
    this.map = map;
  }

  /**
   * A factory method that should return a new instance of a Cell. This method
   * is called by the super class when constructing the surface.
   *
   * @param x The x coordinate of the cell
   * @param y The y coordinate of the cell
   * @return The newly created cell for the SurfaceMap.
   */
  public abstract C generateCell(int x, int y);

  public final int getTotalNumberOfCells() {
    int tSize = getSize();
    return tSize * tSize;
  }

  /**
   * A separate method used for initializing the map. This method should be
   * called after the engine is created or if the map needs to be reset.
   */
  protected void buildMap() {
    int terrainSize = getSize();
    map.clear();
    Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "Setting up {0}", surfaceName);
    for (int x = 0; x < terrainSize; x++) {
      for (int y = 0; y < terrainSize; y++) {
        C generatedCell = generateCell(x, y);
        if (generatedCell != null) {
          setCell(generatedCell);
        }
      }
    }
  }

  public C getCellAt(Vector2f pos) {
    return getCellAt((int) pos.getX(), (int) pos.getY());
  }
  
  public Map<Integer, C> getMapData() {
    return map;
  }
  
  public int getSize() {
    return totalSize;
  }
  
  /**
   * Performs a basic get at the given locations (x and y) without waiting for
   * the resource if it is being used by another thread and this method will
   * return null if the data doesn't exist.
   *
   * @param x The x coordinate of the cell
   * @param y The y coordinate of the cell
   * @return Returns the cell at the specified X and Y location, null if the
   * data doesn't exist or if the data is locked by another thread.
   */
  public C getCellAt(int x, int y) {
    int len = getSize();
    x = x < 0 ? len - 1 : (x >= len ? 0 : x);
    y = y < 0 ? len - 1 : (y >= len ? 0 : y);
    int index = calcIndex(x, y);
    return getCellAt(index);
  }

  /**
   * Performs a basic get at the given locations (x and y) without waiting for
   * the resource if it is being used by another thread and this method will
   * return null if the data doesn't exist.
   *
   * @param index The index
   * @return Returns the cell at the specified X and Y location, null if the
   * data doesn't exist or if the data is locked by another thread.
   */
  public C getCellAt(int index) {
    C c = map.get(index);
    if (c == null) {
      throw new IllegalArgumentException("The index " 
              + index + " (" + calcX(index) + ", " + calcY(index) + ") doesn't exist");
    }
    return c;
  }

  public List<C> getCells(Vector2f... cellPositions) {

    int[] indexes = new int[cellPositions.length];
    for (int i = 0; i < indexes.length; i++) {
      Vector2f p = cellPositions[i];
      int index = calcIndex((int) p.getX(), (int) p.getY());
      indexes[i] = index;
    }

    return getCells(indexes);
  }

  public List<C> getCells(int... cellIndexes) {
    List<C> selectedCells = new ArrayList<>();
    for (int index : cellIndexes) {
      C c = getCellAt(index);
      selectedCells.add(c);
    }
    return selectedCells;
  }

  /**
   * This method is reserved for internal use only by the SurfaceMap during
   * initialization.
   *
   * @param cell The cell that is being added to the map
   */
  private void setCell(C cell) {
    int x = cell.getX(), y = cell.getY();
    int index = calcIndex(x, y);
    map.put(index, cell);
  }

  /**
   * Calculates the index for the element located at (x, y) based on the width
   * of a square map.
   *
   * @param x The x coordinate
   * @param y The y coordinate
   * @return The index corresponding to the x and y location
   */
  public int calcIndex(int x, int y) {
    return (getSize() * y) + x;
  }

  /**
   * Calculates the X coordinate based on the width (w) of the map and the given
   * index.
   *
   * @param index The index of the element
   * @return The x coordinate
   */
  public int calcX(int index) {
    return index % getSize();
  }

  /**
   * Calculates the Y coordinate based on the width (w) of the map and the given
   * index.
   *
   * @param index The index of the element
   * @return The y coordinate
   */
  public int calcY(int index) {
    return index / getSize();
  }

}
