package engine.surface;

import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import engine.surface.tasks.SetParentThreads;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import worlds.planet.Util;
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
public abstract class SurfaceMap<C extends Cell> extends TerrainQuad {
  
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

  /**
   * The map containing the references to each data point on the surface.
   */
  protected Map<Integer, C> map;
  
  /*
    Threads
  */
  private final SurfaceThreads surfaceThreads;
//  private final MThread hotSpotThread;
  
  private TerrainLodControl control;
  
  /**
   * Create a new SurfaceMap. SurfaceThreads and Map need to be initialized
   * separably.
   *
   * @param totalSize The size of this entire terrain (on one side). Power of 2
   * plus 1 (eg. 513, 1025, 2049...)
   * @param surfaceThreads
   */
  public SurfaceMap(int totalSize, SurfaceThreads surfaceThreads) {
    super("surface", 65, totalSize, null);
    displaySetting = 0;
    this.surfaceThreads = surfaceThreads;
  }
  
  public final SurfaceThreads getSurfaceThreads() {
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
  
  public void bindCameraForLODControl(Camera camera) {
    control = new TerrainLodControl(this, camera);
    control.setLodCalculator(new DistanceLodCalculator(getPatchSize(), 1.5f));
    addControl(control);
  }
  
  public void bindTerrainToNode(Node rootNode) {
    setLocalTranslation(0, -50, 0);
    setLocalScale(2f, 1f, 2f);
    rootNode.attachChild(this);
  }
  
  /**
   * Update the terrain's height based on the temperature of the mantle.
   * @param scale Scale the height with this value
   * @param cellData
   */
  public void updateTerrainHeight(float scale, TerrainHeightValue cellData) {
    List<Vector2f> locs = new ArrayList<>();
    List<Float> heights = new ArrayList<>();
    map.values().forEach(cell -> {
      float height = cellData.getHeightValue(cell) * scale;
      Vector2f pos = cell.getGridPosition();
      locs.add(Util.scalePositionForTerrain(pos.getX(), pos.getY(), getTerrainSize()));
      heights.add(height);
    });
    setHeight(locs, heights);
  }
  
  public void updateVertexColors(float colorMap[][], MapBounds bounds) {
    List<TerrainPatch> patches = new ArrayList<>();
    getAllTerrainPatches(patches);
    patches.forEach(patch -> {
      float[] heightMap = patch.getHeightMap();
      float[] colorArray = new float[heightMap.length * 4];
      
      // Iterate over the heightMap, plug the height values into a function
      // to get a color and set that into the colorArray.
      int colorIndex = 0;
      for (int h = 0; h < heightMap.length; h++) {
        float height = heightMap[h];
        int colorMapIndex = bounds.getIndex(height);
        float[] color = colorMap[colorMapIndex];
        colorArray[colorIndex++] = color[0];// red
        colorArray[colorIndex++] = color[1];// green
        colorArray[colorIndex++] = color[2];// blue
        colorArray[colorIndex++] = color[3];// alpha
      }
      
      patch.getMesh().setBuffer(VertexBuffer.Type.Color, 4, colorArray);
      
    });
  }
  
  /**
   * Using a ConcurrentHashMap as the Map data structure.
   *
   * @param threadCount The number of threads being used.
   */
  protected void setupDefaultMap(int threadCount) {
    final float loadFactor = 1.0f;
    int terrainSize = getTerrainSize();
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
    int tSize = getTerrainSize();
    return tSize * tSize;
  }

  /**
   * A separate method used for initializing the map. This method should be
   * called after the engine is created or if the map needs to be reset.
   */
  protected void buildMap() {
    int terrainSize = getTerrainSize();
    map.clear();
    Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "Setting up map");
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
  
  @Override
  public int getTerrainSize() {
    return super.getTerrainSize() - 1;
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
    int len = getTerrainSize();
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
    return (getTerrainSize() * y) + x;
  }

  /**
   * Calculates the X coordinate based on the width (w) of the map and the given
   * index.
   *
   * @param index The index of the element
   * @return The x coordinate
   */
  public int calcX(int index) {
    return index % getTerrainSize();
  }

  /**
   * Calculates the Y coordinate based on the width (w) of the map and the given
   * index.
   *
   * @param index The index of the element
   * @return The y coordinate
   */
  public int calcY(int index) {
    return index / getTerrainSize();
  }

}
