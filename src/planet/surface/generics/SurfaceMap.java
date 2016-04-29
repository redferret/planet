package planet.surface.generics;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import planet.Planet;
import planet.util.Boundaries;
import planet.util.MThread;

/**
 * The SurfaceMap is a generic map for all the systems on the planet. The map
 * contains generic cells. The RenderInterface is not required for this
 * class but it gives two methods for rendering each map. This implementation
 * may change in the future when new graphic rendering capabilities change.
 *
 * @author Richard DeSilvey
 * @param <CellType> The high-level cell living in this map.
 */
public abstract class SurfaceMap<CellType extends Cell> extends MThread implements RenderInterface{

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

    public final static int HEIGHTMAP = 0;
    public final static int STRATAMAP = 1;
    public final static int LANDOCEAN = 2;
    public final static int MAX_HEIGHT_INDEX = 50;
    
    public int displaySetting;
    
    /**
     * The ratio for indexing onto the height map array,
     * by taking a cell height and dividing it by this value will
     * give the proper index to the height map.
     */
    public static int heightIndexRatio  = 50 / MAX_HEIGHT_INDEX;
    
    /**
     * The map containing the references to each data point on the surface.
     * Hashtable is used because it's thread safe.
     */
    private Map<Integer, Cell> map;

    /**
     * Helper threads that would work on the map.
     */
    protected List<MThread> threads;

    private int prevSubThreadAvg;
    
    /**
     * Create a new map
     *
     * @param width Width of the map
     * @param height Height of the map
     * @param delay The number of frames to delay updating
     * @param threadName The name of the thread
     */
    public SurfaceMap(int width, int height, int delay, String threadName) {

        super(delay, threadName, true);

        map = new ConcurrentHashMap<>();
        threads = new ArrayList<>();
        prevSubThreadAvg = 0;
        displaySetting = HEIGHTMAP;
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
     * @param delay The amount of time to set all threads to delay each frame
     * in milliseconds.
     */
    public final void setDelay(int delay) {
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
     * Should return a new instance of a SurfaceThread. This method is called by
     * the super class when threads are being constructed.
     *
     * @param delay The delay the new thread should have
     * @param bounds The boundaries the thread should have
     * @param name The name of the thread
     * @return The newly constructed thread
     */
    public abstract SurfaceThread generateSurfaceThread(int delay, Boundaries bounds, String name);

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
    public final void setupMap() {
        int gridSize = Planet.self().getGridSize();

        // Initialize the map
        Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, "Setting up map");
        for (int x = 0; x < gridSize; x++) {
            for (int y = 0; y < gridSize; y++) {
                setCellAt(generateCell(x, y));
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

        int w = Planet.self().getGridSize() / threadDivision;
        int h = Planet.self().getGridSize() / threadDivision;
        int c = 0;
        Boundaries bounds;
        String name;
        Logger.getLogger(getName()).log(Level.INFO, "Setting up threads");
        for (int y = 0; y < threadDivision; y++) {
            for (int x = 0; x < threadDivision; x++, c++) {
                name = "SubThread: " + c;
                bounds = new Boundaries(w * x, w * (x + 1), h * y, h * (y + 1));
                SurfaceThread thread = generateSurfaceThread(delay, bounds, name);

                if (thread != null) {
                    threads.add(thread);
                }
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

        int width = Planet.self().getGridSize();
        int index = (width * y) + x;

        return (CellType) map.get(index);
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
    protected final void setCellAt(CellType cell) {
        int x = cell.getX(), y = cell.getY();
        int width = Planet.self().getGridSize();
        int index = (width * y) + x;
        map.put(index, cell);
    }

    
    @Override
    public List<Integer[]> getCellSettings(int x, int y) {
        
        List<Integer[]> settings = new ArrayList<>();
        
        return getCellAt(x, y).render(settings);
    }
}
