package planet.surface;

import planet.cells.Cell;
import planet.util.SurfaceThread;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import planet.Planet;
import planet.gui.RenderInterface;
import planet.util.Boundaries;
import planet.util.MThread;

/**
 * The SurfaceMap is a generic map for all the systems on the planet. The map
 * contains generic cells. The RenderInterface is not required for this class
 * but it gives two methods for rendering each map. This implementation may
 * change in the future when new graphic rendering capabilities change.
 *
 * @author Richard DeSilvey
 * @param <CellType> The high-level cell living in this map.
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

    public final static int MAX_HEIGHT_INDEX = 50;

    public int displaySetting;

    /**
     * The ratio for indexing onto the height map array, by taking a cell height
     * and dividing it by this value will give the proper index to the height
     * map.
     */
    public static int heightIndexRatio = 50 / MAX_HEIGHT_INDEX;

    /**
     * The map containing the references to each data point on the surface.
     */
    private Map<Integer, CellType> map;

    /**
     * Helper threads that would work on the map.
     */
    protected List<SurfaceThread> threads;

    private int prevSubThreadAvg;

    /**
     * Create a new map
     *
     * @param planetWidth The size of the map
     * @param delay The number of frames to delay updating
     * @param threadName The name of the thread
     * @param threadCount The number of threads that will work on the map
     */
    public SurfaceMap(int planetWidth, int delay, String threadName, int threadCount) {

        super(delay, threadName, true);

        int capacity = planetWidth * planetWidth;
        map = new ConcurrentHashMap<>(capacity, 1, threadCount);
        threads = new ArrayList<>();
        prevSubThreadAvg = 0;
        displaySetting = 0;
    }

    /**
     * Starts all the threads
     */
    public final void startAll() {

        for (int i = 0; i < threads.size(); i++) {
            MThread thread = threads.get(i);

            if (thread != null) {
                thread.start();
            }
        }
    }

    /**
     * Pauses all the threads
     */
    public final void pauseAll() {
        for (int i = 0; i < threads.size(); i++) {
            MThread thread = threads.get(i);

            if (thread != null) {
                thread.pause();
            }
        }
    }

    /**
     * Plays all the threads
     */
    public final void playAll() {
        for (int i = 0; i < threads.size(); i++) {
            MThread thread = threads.get(i);

            if (thread != null) {
                thread.play();
            }
        }
    }

    /**
     * Sets all the threads to this delay.
     *
     * @param delay The amount of time to set all threads to delay each frame in
     * milliseconds.
     */
    public final void setThreadsDelay(int delay) {
        for (int i = 0; i < threads.size(); i++) {
            MThread thread = threads.get(i);

            if (thread != null) {
                thread.setDelay(delay);
            }
        }
    }

    /**
     * When a new world is created at runtime certain configurations need to be
     * reset or re-initialized.
     */
    public abstract void reset();

    /**
     * Should return a new instance of an Object of type Cell. This method is
     * called by the super class when constructing the surface.
     *
     * @param x The x coordinate of the cell
     * @param y The y coordinate of the cell
     * @return The newly created cell for the SurfaceMap.
     */
    public abstract CellType generateCell(int x, int y);

    /**
     * Override this method and call the <code>super.update()</code> to make
     * additional updates. The <code>super.update()</code> must be called for
     * the map to function properly.
     */
    public void update() {
        checkSubThreads();
    }

    private void checkSubThreads() {
        int sleeping = 0;
        int expected = threads.size();

        if (expected > 0) {

            int avg = 0;

            boolean paused;
            for (int i = 0; i < expected; i++) {
                paused = threads.get(i).paused();

                if (paused) {
                    sleeping++;
                }
                avg += threads.get(i).timeLapse();
            }

            if (sleeping == expected) {
                playAll();
            }

            prevSubThreadAvg = avg / expected;
        }
    }

    public int getAverageThreadTime() {
        return prevSubThreadAvg;
    }

    /**
     * A separate method used for initializing the grid. This method should be
     * called after the engine is created where each surface map is setup
     * individually. This is due to the global reference to the engine.
     */
    protected void setupMap() {
        int gridSize = Planet.self().getGridWidth();

        int totalCells = (gridSize * gridSize);
        int flagUpdate = totalCells / 2;
        int generated = 0;
        // Initialize the map
        Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "Setting up map");
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                setCellAt(generateCell(x, y));
                generated++;
                if (generated % flagUpdate == 0) {
                    double finished = (double) generated / (double) totalCells;
                    Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO,
                            "Cells created: {0}% finished", finished * 100);
                }
            }
        }
    }

    /**
     * Method needs to be called internally from the Surface class.
     *
     * @param threadDivision The value given is the dimensions of the threads. A
     * value n would yield an nxn grid of threads. Each controlling a section of
     * the surface map. Each thread is a SurfaceThread.
     * @param delay The thread delay for each frame in miliseconds.
     */
    public final void setupThreads(int threadDivision, int delay) {

        int w = Planet.self().getGridWidth() / threadDivision;
        int c = 0;
        Boundaries bounds;
        String name;
        Logger.getLogger(getName()).log(Level.INFO, "Setting up threads");
        for (int y = 0; y < threadDivision; y++) {
            for (int x = 0; x < threadDivision; x++, c++) {
                name = "SubThread: " + c;
                bounds = new Boundaries(w * x, w * (x + 1), w * y, w * (y + 1));
                SurfaceThread thread = new SurfaceThread(delay, bounds, name);
                threads.add(thread);
            }
        }
    }

    /**
     * Get a cell using the provided coordinates.
     *
     * @param x The x coordinate of the cell
     * @param y The y coordinate of the cell
     * @return Returns the cell at the specified X and Y location.
     */
    public final CellType getCellAt(int x, int y) {

        int width = Planet.self().getGridWidth();
        int index = (width * y) + x;

        return getCellAt(index);
    }

    /**
     * Fetches a cell at the given index from the map.
     * @param index The index
     * @return The cell that maps to the given index.
     */
    public final CellType getCellAt(int index){
        return map.get(index);
    }
    
    /**
     * Returns the cell of this map using the position of the given cell.
     *
     * @param cell The cell to use a look up of it's coordinates?
     * @return The cell at the location of the cell given at the parameter.
     */
    public final CellType getCellAt(Cell cell) {
        return getCellAt(cell.getX(), cell.getY());
    }

    /**
     * This method is reserved for internal use only by the SurfaceMap during
     * initialization.
     *
     * @param cell The cell that is being added to the map
     */
    private void setCellAt(CellType cell) {
        int x = cell.getX(), y = cell.getY();
        int width = Planet.self().getGridWidth();
        int index = (width * y) + x;
        map.put(index, cell);
    }

    @Override
    public List<Integer[]> getCellData(int x, int y) {

        List<Integer[]> settings = new ArrayList<>();
        CellType cell = getCellAt(x, y);

        return cell.render(settings);
    }
}
