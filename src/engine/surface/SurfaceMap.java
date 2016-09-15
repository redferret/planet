package engine.surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.gui.RenderInterface;
import engine.util.Point;
import engine.util.task.Boundaries;
import engine.util.concurrent.TaskRunner;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import engine.util.concurrent.AtomicData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The SurfaceMap is a generic map for all the systems on the planet. The map
 * contains generic cells. The RenderInterface is not required for this class
 * but it gives two methods for rendering each map. This implementation may
 * change in the future when new graphic rendering capabilities change. A
 * SurfaceMap also by default doesn't instantiate any SurfaceThreads, therefore
 * the <code>setupThreads(int threadDivision, int delay)</code> needs to be
 * called after this super class is created.
 *
 * @author Richard DeSilvey
 * @param <CellType> The highest level abstraction of the cell i.e. PlanetCell
 */
public abstract class SurfaceMap<CellType extends Cell> extends TaskRunner implements RenderInterface {

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
    private Map<Integer, AtomicData<CellType>> map;
    protected List<SurfaceThread> threadReferences;
    private List<Integer[]> data;
    private int prevSubThreadAvg;
    private AtomicInteger gridWidth;
    private ExecutorService threadPool;
    private CyclicBarrier waitingGate;
    private ResourceGuard guard;

    /**
     * Create a new SurfaceMap. SurfaceThreads and Map need to be initialized
     * separably.
     *
     * @param mapWidth The number of cells = mapWidth * mapWidth
     * @param delay The number of frames to delay updating
     * @param threadCount The number of threads that will work on the map, this
     * will not setup the threads, this only tells this map how many threads
     * will work on the ConcurrentHashMap.
     */
    public SurfaceMap(int mapWidth, int delay) {
        super(delay, true);
        gridWidth = new AtomicInteger(mapWidth);
        threadReferences = new ArrayList<>();
        guard = new ResourceGuard();
        data = new ArrayList<>();
        prevSubThreadAvg = 0;
        displaySetting = 0;
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
        Map<Integer, AtomicData<CellType>> defaultMap = new ConcurrentHashMap<>(capacity, loadFactor, threadCount);
        setMap(defaultMap);
    }

    public void setMap(Map<Integer, AtomicData<CellType>> map) {
        this.map = map;
    }

    /**
     * Starts all the threads and "Initiates an orderly shutdown in which
     * previously submitted tasks are executed, but no new tasks will be
     * accepted. Invocation has no additional effect if already shut down."
     */
    public final void startSurfaceThreads() {
        threadPool.shutdown();
    }

    /**
     * Pauses all the threads
     */
    public final void pauseSurfaceThreads() {
        threadReferences.forEach(thread -> {
            thread.pause();
        });
    }

    /**
     * Plays all the threads
     */
    public final void playSurfaceThreads() {
        threadReferences.forEach(thread -> {
            thread.play();
        });
    }

    /**
     * Sets all the threads to this delay.
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
    public abstract CellType generateCell(int x, int y);

    /**
     * Override this method and call the <code>super.update()</code> to make
     * additional updates.
     */
    public void update() {
        checkSubThreads();
    }

    /**
     * Adds the Task instance to each thread.
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
            producedTask.taskThread = thread;
            thread.addTask(producedTask);
        });
    }

    private void checkSubThreads() {
        int avg = 0;
        for (SurfaceThread thread : threadReferences) {
            avg = thread.timeLapse();
        }
        prevSubThreadAvg = avg / threadReferences.size();
    }

    public final int getGridWidth() {
        return gridWidth.get();
    }

    public final int getTotalNumberOfCells() {
        int gw = getGridWidth();
        return gw * gw;
    }

    /**
     * If all the threads are not continuous then this method will check if all
     * threads have finished their iteration. If all threads have finished their
     * iteration then this method will signal all threads to run and return
     * true, otherwise this method will return false.
     *
     * @return True if all the threadReferences were signaled to run.
     */
    public boolean synchronizeThreads() {
        int sleeping = 0;
        int expected = threadReferences.size();

        if (expected > 0) {
            for (int i = 0; i < expected; i++) {
                boolean paused = threadReferences.get(i).paused();
                if (paused) {
                    sleeping++;
                }
            }
            if (sleeping == expected) {
                playSurfaceThreads();
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
        int cellCountWidth = gridWidth.get();
        int totalCells = (cellCountWidth * cellCountWidth);
        int flagUpdate = totalCells / 2;
        int generated = 0;
        // Initialize the map
        map.clear();
        Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "Setting up map");
        for (int x = 0; x < cellCountWidth; x++) {
            for (int y = 0; y < cellCountWidth; y++) {
                setCell(generateCell(x, y));
                generated++;
                logMapSetup(generated, flagUpdate, totalCells);
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
     * surface with multiple threads working on the same Map it is recommended
     * to setup the Map by calling the <code>setupDefaultMap()</code> method.
     * This will setup the Map as a ConcurrentHashMap. Otherwise the Map data
     * structure needs to be able to handle multiple threads accessing it's
     * contents at the same time similar to how the ConcurrentHashMap functions.
     *
     * @param threadDivision The value given is the dimensions of the threads. A
     * value n would yield an nxn grid of threads. Each controlling a section of
     * the surface map. Each thread is a SurfaceThread.
     * @param delay The thread delay for each frame in miliseconds.
     */
    public final void setupThreads(int threadDivision, int delay) {

        int threadCount = threadDivision * threadDivision;
        waitingGate = new CyclicBarrier(threadCount);
        int w = gridWidth.get() / threadDivision;
        Boundaries bounds;
        threadPool = Executors.newFixedThreadPool(threadCount + 1);
        for (int y = 0; y < threadDivision; y++) {
            for (int x = 0; x < threadDivision; x++) {
                int lowerX = w * x;
                int upperX = w * (x + 1);
                int lowerY = w * y;
                int upperY = w * (y + 1);
                bounds = new Boundaries(lowerX, upperX, lowerY, upperY);
                SurfaceThread thread = new SurfaceThread(delay, bounds, waitingGate);
                threadPool.submit(thread);
                threadReferences.add(thread);
            }
        }
        threadPool.submit(this);
    }

    /**
     * Shuts down all threads in the pool.
     */
    public void killAllThreads() {
        threadPool.shutdownNow();
    }

    /**
     * Performs a basic get at the given locations (x and y) without waiting for
     * the resource if it is being used by another thread and this method will
     * return null if the data doesn't exist or if it is locked by another
     * thread.
     *
     * @param x The x coordinate of the cell
     * @param y The y coordinate of the cell
     * @return Returns the cell at the specified X and Y location, null if the
     * data doesn't exist or if the data is locked by another thread.
     */
    public CellType getCellAt(int x, int y) {
        int index = calcIndex(x, y, gridWidth.get());
        return getCellAt(index);
    }

    /**
     * Performs a basic get at the given locations (x and y) without waiting for
     * the resource if it is being used by another thread and this method will
     * return null if the data doesn't exist or if it is locked by another
     * thread.
     *
     * @param index The index
     * @return Returns the cell at the specified X and Y location, null if the
     * data doesn't exist or if the data is locked by another thread.
     */
    public CellType getCellAt(int index) {
        return guard.getCellAt(index);
    }

    public CellType waitForCell(CellType cell) {
        int x = cell.getX(), y = cell.getY();
        return waitForCellAt(x, y);
    }

    /**
     * Gets a cell using the provided coordinates, if the cell is being worked
     * on by another thread then the caller to this method will wait until the
     * resource is avaliable.
     *
     * @param x The x coordinate of the cell
     * @param y The y coordinate of the cell
     * @return Returns the cell at the specified X and Y location.
     */
    public CellType waitForCellAt(int x, int y) {
        int index = calcIndex(x, y, gridWidth.get());
        return waitForCellAt(index);
    }

    /**
     * Gets a cell at the given index in the map, if the cell is being worked on
     * by another thread then the caller to this method will wait until the
     * resource is avaliable.
     *
     * @param index The index
     * @return The cell that maps to the given index.
     */
    public CellType waitForCellAt(int index) {
        return guard.waitForCellAt(index);
    }

    /**
     * When a thread gets data from this Map the data is locked until it is
     * released by the same thread working on the cell returned by this Map.
     *
     * @param cell The cell to be released to other threads waiting to access
     * it.
     */
    public void release(CellType cell) {
        guard.release(cell);
    }

    /**
     * Waits for the resources given by the array of indexes.
     *
     * @param cellIndexes Each cell's index
     * @return The list of requested resources.
     */
    public List<CellType> waitForCells(int... cellIndexes) {
        return guard.waitForCells(cellIndexes);
    }

    /**
     * Waits for the resources given by the List of cell positions.
     *
     * @param cellPositions Each cell's position
     * @return The list of requested resources.
     */
    public List<CellType> waitForCells(Point... cellPositions) {
        return guard.waitForCells(cellPositions);
    }

    /**
     * This method is reserved for internal use only by the SurfaceMap during
     * initialization.
     *
     * @param cell The cell that is being added to the map
     */
    private void setCell(CellType cell) {
        int x = cell.getX(), y = cell.getY();
        int index = calcIndex(x, y, gridWidth.get());
        map.put(index, new AtomicData<>(cell));
    }

    /**
     * Calculates the index for the element located at (x, y) based on the width
     * of a square map.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     * @param w The width of the map
     * @return The index corresponding to the x and y location
     */
    public static int calcIndex(int x, int y, int w) {
        return (w * y) + x;
    }

    /**
     * Calculates the X coordinate based on the width (w) of the map and the
     * given index.
     *
     * @param index The index of the element
     * @param w The width of the map
     * @return The x coordinate
     */
    public static int calcX(int index, int w) {
        return index % w;
    }

    /**
     * Calculates the Y coordinate based on the width (w) of the map and the
     * given index.
     *
     * @param index The index of the element
     * @param w The width of the map
     * @return The y coordinate
     */
    public static int calcY(int index, int w) {
        return index / w;
    }

    /**
     * If the resource is currently locked the cell is skipped.
     */
    @Override
    public List<Integer[]> getCellData(int x, int y) {

        data.clear();
        CellType cell = getCellAt(x, y);
        List<Integer[]> tempData = new ArrayList<>();
        if (cell != null) {
            tempData = cell.render(data);
            release(cell);
        }
        return tempData;
    }

    /**
     * Will allow for ranges of data to be returned as well as single data
     * points in the map.
     *
     * @author Richard DeSilvey
     */
    private class ResourceGuard {

        private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
        private final Lock guard;

        public ResourceGuard() {
            guard = readWriteLock.writeLock();
        }

        public List<CellType> waitForCells(Point[] cellPositions) {
            // TODO Auto-generated method stub
            return null;
        }

        public List<CellType> waitForCells(int[] cellIndexes) {
            // TODO Auto-generated method stub
            return null;
        }

        /**
         * Performs a basic get at the given locations (x and y) without waiting
         * for the resource if it is being used by another thread and this
         * method will return null if the data doesn't exist or if it is locked
         * by another thread.
         *
         * @param index The index
         * @return Returns the cell at the specified X and Y location, null if
         * the data doesn't exist or if the data is locked by another thread.
         */
        public CellType getCellAt(int index) {
            AtomicData<CellType> lock = map.get(index);
            return (lock == null)
                    ? null : lock.getData();
        }

        /**
         * Gets a cell at the given index in the map, if the cell is being
         * worked on by another thread then the caller to this method will wait
         * until the resource is avaliable.
         *
         * @param index The index
         * @return The cell that maps to the given index.
         */
        public CellType waitForCellAt(int index) {
            AtomicData<CellType> lock = map.get(index);
            return (lock == null)
                    ? null : lock.waitForData();
        }

        /**
         * When a thread gets data from this Map the data is locked until it is
         * released by the same thread working on the cell returned by this Map.
         *
         * @param cell The cell to be released to other threads waiting to
         * access it.
         */
        public void release(CellType cell) {
            if (cell == null) {
                throw new IllegalArgumentException("Cannot release a null reference");
            }
            int x = cell.getX(), y = cell.getY();
            int index = calcIndex(x, y, gridWidth.get());
            AtomicData<CellType> lock = map.get(index);
            if (lock != null) {
                lock.unlock(cell);
            }
        }

    }
}
