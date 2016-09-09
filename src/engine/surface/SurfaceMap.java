package engine.surface;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import engine.gui.RenderInterface;
import engine.util.task.Boundaries;
import engine.util.MThread;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import engine.cells.Cell;
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
public abstract class SurfaceMap<CellType extends Cell> extends MThread implements RenderInterface {

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
    private Map<Integer, CellType> map;

    /**
     * Helper threads that would work on the map.
     */
    protected List<SurfaceThread> threads;

    private List<Integer[]> data;

    private int prevSubThreadAvg;

    private AtomicInteger gridWidth;

    private final CyclicBarrier waitingGate;

    /**
     * Create a new SurfaceMap. SurfaceThreads and Map need to be initialized
     * separably.
     *
     * @param mapWidth The number of cells = mapWidth * mapWidth
     * @param delay The number of frames to delay updating
     * @param threadName The name of the thread
     * @param threadCount The number of threads that will work on the map, this
     * will not setup the threads, this only tells this map how many threads
     * will work on the ConcurrentHashMap.
     */
    public SurfaceMap(int mapWidth, int delay, String threadName, int threadCount) {
        super(delay, threadName, true);
        gridWidth = new AtomicInteger(mapWidth);
        threads = new ArrayList<>();
        data = new ArrayList<>();
        prevSubThreadAvg = 0;
        displaySetting = 0;
        waitingGate = new CyclicBarrier(threadCount);
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
        Map<Integer, CellType> defaultMap = new ConcurrentHashMap<>(capacity, loadFactor, threadCount);
        setMap(defaultMap);
    }

    public void setMap(Map<Integer, CellType> map) {
        this.map = map;
    }

    /**
     * Starts all the threads
     */
    public final void startSurfaceThreads() {
        threads.forEach(thread -> {
            thread.start();
        });
    }

    /**
     * Pauses all the threads
     */
    public final void pauseSurfaceThreads() {
        threads.forEach(thread -> {
            thread.pause();
        });
    }

    /**
     * Plays all the threads
     */
    public final void playSurfaceThreads() {
        threads.forEach(thread -> {
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
        threads.forEach(thread -> {
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
        threads.forEach(thread -> {
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
        threads.forEach(thread -> {
            Task producedTask = factory.buildTask();
            producedTask.taskThread = thread;
            thread.addTask(producedTask);
        });
    }

    private void checkSubThreads() {
        int avg = 0;
        for (SurfaceThread thread : threads) {
            avg = thread.timeLapse();
        }
        prevSubThreadAvg = avg / threads.size();
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
     * @return True if all the threads were signaled to run.
     */
    public boolean synchronizeThreads() {
        int sleeping = 0;
        int expected = threads.size();

        if (expected > 0) {
            for (int i = 0; i < expected; i++) {
                boolean paused = threads.get(i).paused();
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
        threads.forEach(thread -> {
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

        int w = gridWidth.get() / threadDivision;
        int c = 0;
        Boundaries bounds;
        String name;
        Logger.getLogger(getName()).log(Level.INFO, "Setting up threads");
        for (int y = 0; y < threadDivision; y++) {
            for (int x = 0; x < threadDivision; x++, c++) {
                name = "SubThread: " + c;
                int lowerX = w * x;
                int upperX = w * (x + 1);
                int lowerY = w * y;
                int upperY = w * (y + 1);
                bounds = new Boundaries(lowerX, upperX, lowerY, upperY);
                SurfaceThread thread = new SurfaceThread(delay, bounds, name, waitingGate);
                threads.add(thread);
            }
        }
    }

    /**
     * This will remove all threads from the surface. You will need to call the
     * <code>setupThreads</code> method to make this surface functional again.
     */
    public void killAllThreads() {
        threads.forEach(thread -> {
            thread.kill();
        });
    }

    /**
     * Gets a cell using the provided coordinates.
     *
     * @param x The x coordinate of the cell
     * @param y The y coordinate of the cell
     * @return Returns the cell at the specified X and Y location.
     */
    public CellType getCellAt(int x, int y) {
        int index = calcIndex(x, y, gridWidth.get());
        return getCellAt(index);
    }

    /**
     * Gets a cell at the given index in the map.
     *
     * @param index The index
     * @return The cell that maps to the given index.
     */
    public CellType getCellAt(int index) {
        return map.get(index);
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
        map.put(index, cell);
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

    @Override
    public List<Integer[]> getCellData(int x, int y) {

        data.clear();
        CellType cell = getCellAt(x, y);

        return cell.render(data);
    }
}
