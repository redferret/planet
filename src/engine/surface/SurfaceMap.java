package engine.surface;

import com.jme3.material.Material;
import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import engine.util.concurrent.MThread;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import engine.util.Vec2;
import engine.util.task.Boundaries;
import engine.util.task.Task;
import engine.util.task.TaskAdapter;
import engine.util.task.TaskFactory;

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
public abstract class SurfaceMap<C extends Cell> extends AbstractHeightMap {
  
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
  protected List<MThread> threadReferences;
  private final List<Integer[]> data;
  private int prevSubThreadAvg;
  private ExecutorService threadPool;
  private CyclicBarrier waitingGate;
  
  protected final TerrainQuad terrain;
  private TerrainLodControl control;
  
  /**
   * Create a new SurfaceMap. SurfaceThreads and Map need to be initialized
   * separably.
   *
   * @param mapWidth The number of cells = mapWidth * mapWidth
   * @param delay The number of frames to delay updating
   */
  public SurfaceMap(int mapWidth, int delay) {
    this.size = mapWidth;
    threadReferences = new ArrayList<>();
    data = new ArrayList<>();
    prevSubThreadAvg = 0;
    displaySetting = 0;
    terrain = new TerrainQuad("surface", 65, mapWidth + 1, null);
  }

  public void bindMaterial(Material material) {
    terrain.setMaterial(material);
  }
  
  public void bindCameraForLODControl(Camera camera) {
    control = new TerrainLodControl(terrain, camera);
    control.setLodCalculator( new DistanceLodCalculator(65, 1.1f) ); // patch size, and a multiplier
    terrain.addControl(control);
  }
  
  public void bindTerrain(Node rootNode) {
    terrain.setLocalTranslation(0, -100, 0);
    terrain.setLocalScale(2f, 1f, 2f);
    rootNode.attachChild(terrain);
  }
  
  /**
   * Using a ConcurrentHashMap as the Map data structure.
   *
   * @param planetWidth The width of the map.
   * @param threadCount The number of threads being used.
   */
  protected void setupDefaultMap(int planetWidth, int threadCount) {
    final float loadFactor = 1.0f;
    final int capacity = planetWidth * planetWidth;
    Map<Integer, C> defaultMap = new ConcurrentHashMap<>(capacity, loadFactor, threadCount);
    setMap(defaultMap);
  }

  public void setMap(Map<Integer, C> map) {
    this.map = map;
  }

  /**
   * Starts all the threads and "Initiates an orderly shutdown in which
   * previously submitted tasks are executed, but no new tasks will be accepted.
   * Invocation has no additional effect if already shut down."
   * the main thread is also started.
   */
  public final void startThreads() {
    threadPool.shutdown();
  }

  /**
   * Pauses all the threads
   */
  public final void pauseThreads() {
    threadReferences.forEach(thread -> {
      thread.pause();
    });
  }

  /**
   * Plays all the threads
   */
  public final void playThreads() {
    threadReferences.forEach(thread -> {
      thread.play();
    });
  }
  
  /**
   * Sets all the threads to this delay except the main thread
   *
   * @param delay The amount of time to set all threads to delay each frame in
   * milliseconds.
   */
  public final void setThreadsDelay(int delay) {
    threadReferences.forEach(thread -> {
      thread.setDelay(delay);
    });
  }

  /**
   * When a new world is created certain configurations need to be reset or
   * re-initialized when a new world or surface. It's best to call the
   * <code>buildMap()</code> method here as it will re-create the map.
   */
  public abstract void reset();

  /**
   * A factory method that should return a new instance of a Cell. This method
   * is called by the super class when constructing the surface.
   *
   * @param x The x coordinate of the cell
   * @param y The y coordinate of the cell
   * @return The newly created cell for the SurfaceMap.
   */
  public abstract C generateCell(int x, int y);

  /**
   * Adds the Task instance to each thread. The thread will be assigned to the
   * task allowing access to the parent thread via the task.
   *
   * @param task The task being added to each thread.
   */
  public void addTaskToThreads(Task task) {
    threadReferences.forEach(thread -> {
      thread.addTask(task);
    });
  }

  /**
   * Produces individual instances of a Task for each thread using the given
   * instance of a TaskFactory.
   *
   * @param factory The factory that will produce a Task for each thread.
   */
  public void produceTasks(TaskFactory factory) {
    threadReferences.forEach(thread -> {
      Task producedTask = factory.buildTask();
      thread.addTask(producedTask);
    });
  }

  public void checkSubThreads() {
    int avg = 0;
    for (MThread thread : threadReferences) {
      avg = thread.timeLapse();
    }
    prevSubThreadAvg = avg / threadReferences.size();
  }

  public final int getTotalNumberOfCells() {
    return size * size;
  }

  /**
   * This method will check if all
   * threads have finished their iteration. If all threads have finished their
   * iteration then this method will signal all threads to run and return true,
   * otherwise this method will return false.<br>
   * One could wait for the threads until they are ready to run again, this
   * will ensure that all threads are ready to run in synch with each other.
   * If they are not continuously running this method can be used to run each
   * thread.
   * <br>
   * <code>while(!synchronizeThreads()){<br>// Block until threads are done and ready to run again
   * <br>}</code>
   *
   * @return True if all the threadReferences were signaled to run.
   */
  public boolean synchronizeThreads() {
    int sleeping = 0;
    int expected = threadReferences.size();

    if (expected > 0) {
      for (int i = 0; i < expected; i++) {
        boolean paused = threadReferences.get(i).isPaused();
        if (paused) {
          sleeping++;
        }
      }
      if (sleeping == expected) {
        playThreads();
        return true;
      }
    }
    return false;
  }

  public void setThreadsAsContinuous(boolean c) {
    threadReferences.forEach(thread -> {
      thread.setContinuous(c);
    });
  }

  /**
   * Gets the average runtime between all threads loaded in the simulation.
   *
   * @return The average runtime between all threads.
   */
  public int getAverageThreadTime() {
    return prevSubThreadAvg;
  }

  /**
   * A separate method used for initializing the map. This method should be
   * called after the engine is created or if the map needs to be reset.
   */
  protected void buildMap() {
    int cellCountWidth = size;
    int totalCells = (cellCountWidth * cellCountWidth);
    int flagUpdate = totalCells / 4;
    int generated = 0;
    // Initialize the map
    map.clear();
    Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "Setting up map");
    for (int x = 0; x < cellCountWidth; x++) {
      for (int y = 0; y < cellCountWidth; y++) {
        C generatedCell = generateCell(x, y);
        if (generatedCell != null) {
          setCell(generatedCell);
          generated++;
          logMapSetup(generated, flagUpdate, totalCells);
        }
      }
    }
  }

  private void logMapSetup(int generated, int flagUpdate, int totalCells) {
    if (generated % flagUpdate == 0) {
      double finished = (double) generated / (double) totalCells;
      Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO,
              "Cells created: {0}% finished", Math.round(finished * 100));
    }
  }

  /**
   * Sets up each individual thread for this surface. If you are using this
   * surface with multiple threads working on the same Map it is recommended to
   * setup the Map by calling the <code>setupDefaultMap()</code> method. This
   * will setup the Map as a ConcurrentHashMap. Otherwise the Map data structure
   * needs to be able to handle multiple threads accessing it's contents at the
   * same time similar to how the ConcurrentHashMap functions.
   *
   * @param threadDivision The value given is the dimensions of the threads. A
   * value n would yield an N x N grid of threads. Each controlling a section of
   * the surface map. Each thread is a SurfaceThread.
   * @param delay The thread delay for each frame in milliseconds.
   */
  public final void setupThreads(int threadDivision, int delay) {

    int threadCount = threadDivision * threadDivision;
    waitingGate = new CyclicBarrier(threadCount);
    int w = size / threadDivision;
    Boundaries bounds;
    threadPool = Executors.newFixedThreadPool(threadCount + 1);
    for (int y = 0; y < threadDivision; y++) {
      for (int x = 0; x < threadDivision; x++) {
        int lowerX = w * x;
        int upperX = w * (x + 1);
        int lowerY = w * y;
        int upperY = w * (y + 1);
        bounds = new Boundaries(lowerX, upperX, lowerY, upperY);
        MThread thread = new MThread(delay, bounds, waitingGate);
        threadPool.submit(thread);
        threadReferences.add(thread);
      }
    }
    addTaskToThreads(new SetParentThreads());
  }

  /**
   * Shuts down all threads in the pool.
   */
  public void killAllThreads() {
    threadReferences.forEach(thread -> {
      thread.kill();
    });
    threadPool.shutdownNow();
  }

  /**
   * Sets up the surface further by setting each cell the parent thread.
   */
  private class SetParentThreads extends TaskAdapter {
    
    @Override
    public void before() {
      this.singleTask = true;
    }

    @Override
    public void perform(int x, int y) {
      getCellAt(x, y).setParentThread(SetParentThreads.this.getThread());
    }

    @Override
    public void after() {
    }
  }
  
  public C getCellAt(Vec2 pos) {
    return getCellAt((int) pos.getX(), (int) pos.getY());
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
    return map.get(index);
  }

  public List<C> getCells(Vec2... cellPositions) {

    int[] indexes = new int[cellPositions.length];
    for (int i = 0; i < indexes.length; i++) {
      Vec2 p = cellPositions[i];
      int index = calcIndex((int) p.getX(), (int) p.getY());
      indexes[i] = index;
    }

    return getCells(indexes);
  }

  public List<C> getCells(int... cellIndexes) {
    List<C> selectedCells = new ArrayList<>();
    for (int index : cellIndexes) {
      C c = getCellAt(index);
      if (data != null) {
        selectedCells.add(c);
      }
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
    return (size * y) + x;
  }

  /**
   * Calculates the X coordinate based on the width (w) of the map and the given
   * index.
   *
   * @param index The index of the element
   * @return The x coordinate
   */
  public int calcX(int index) {
    return index % size;
  }

  /**
   * Calculates the Y coordinate based on the width (w) of the map and the given
   * index.
   *
   * @param index The index of the element
   * @return The y coordinate
   */
  public int calcY(int index) {
    return index / size;
  }

  public List<Integer[]> getCellData(int x, int y) {

    data.clear();
    C cell = getCellAt(x, y);
    List<Integer[]> tempData = new ArrayList<>();
    if (cell != null) {
      tempData = cell.render(data);
    }
    return tempData;
  }

}
