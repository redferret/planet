package worlds.planet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import sun.security.jca.GetInstance.Instance;
import worlds.planet.PlanetCell;
import engine.gui.DisplayAdapter;
import engine.surface.SurfaceMap;
import engine.util.concurrent.SurfaceThread;
import engine.util.concurrent.AtomicFloat;
import engine.util.task.Boundaries;
import engine.util.Delay;
import engine.util.Point;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import engine.util.task.TaskManager;

import java.util.logging.Level;
import java.util.logging.Logger;

import static engine.util.Tools.checkBounds;
import static worlds.planet.Surface.GEOUPDATE;
import static worlds.planet.Surface.planetAge;

/**
 * The Surface is the geology for the planet. It provides a foundation for life
 * to grow on and to influence climate in many ways. Through the placement of
 * continents, the type of surface which provides albedo rating for solar
 * radiation absorbtions, and volcanism which pours out CO2 and water vapor.
 *
 * @author Richard DeSilvey
 */
public abstract class Surface extends SurfaceMap<PlanetCell> {

    /**
     * The average density of the mantel. The units are in kilograms per cubic
     * meter.
     */
    public static float mantel_density = 3700f;

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

    private DisplayAdapter display;

    private TaskManager generalTasks;
    
    private Delay ageUpdateDelay, threadAverageDelay;

    public final static int HEIGHTMAP = 0;
    public final static int STRATAMAP = 1;
    public final static int LANDOCEAN = 2;

    private static final int DEFAULT_THREAD_DELAY = 1;
    
    private MinMaxHeightFactory mhFactory;
    
    /**
     * Internal cached Random object for surfaces.
     */
    protected static final Random random;

    static {
        random = new Random();
        timeStep = 125000;
        GEOUPDATE = 250000;
    }

    /**
     * Constructs a new Surface with an empty map.
     *
     * @param planetWidth The size of the surface
     * @param ageStepDelay The amount of time to delay updating planet age
     * @param threadsDelay The amount of time to delay each frame in
     * milliseconds.
     * @param threadCount The number of threads that will work on the map
     */
    public Surface(int planetWidth, int ageStepDelay, int threadsDelay, int threadCount) {
        super(planetWidth, DEFAULT_THREAD_DELAY);
        ageUpdateDelay = new Delay(ageStepDelay);
        setupThreads(threadCount, threadsDelay);
        setupDefaultMap(planetWidth, threadCount);
        mhFactory = new MinMaxHeightFactory();
        produceTasks(mhFactory);
        Boundaries bounds = new Boundaries(0, planetWidth);
        generalTasks = new TaskManager(bounds);
        set();
    }

    private void set() {
        threadAverageDelay = new Delay(2);
        display = null;
        reset();
    }

    /**
     * Resets the surface to an empty map and resets the planet's age.
     * This method should be calling the <code>buildMap()</code> method.
     */
    public void reset() {
        planetAge = new AtomicLong(0);
        geologicalTimeStamp = 0;
        buildMap();
    }

    public void setDisplay(DisplayAdapter display) {
        this.display = display;
    }

    public long getPlanetAge() {
        return planetAge.get();
    }
    
    public void addTask(Task task){
        generalTasks.addTask(task);
    }
    
    @Override
    public void update() {

        try {
            updatePlanetAge();
            
            if (display != null) {
                display.update();
            }
            if (threadAverageDelay.check()) {
                super.update();
            }
            
            generalTasks.performTasks();
        } catch (Exception e) {
            Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, null, e);
        }
        
    }

    private void updatePlanetAge() {
        if (ageUpdateDelay.check()) {
            long curPlanetAge = planetAge.getAndAdd(timeStep);
            if (curPlanetAge - geologicalTimeStamp > GEOUPDATE) {
                geologicalTimeStamp = curPlanetAge;
            }
        }
    }

    @Override
    public PlanetCell generateCell(int x, int y) {
        return new PlanetCell(x, y);
    }

    public float getHighestHeight(){
        return mhFactory.getHighestHeight();
    }
    public float getLowestHeight() {
        return mhFactory.getLowestHeight();
    }
    
    /**
     * Adds to the 'lowest' list passed into this method from the list of cells
     * the lowest cells from the cell that is at the end of the cells list.
     * @param lowest The list that will hold the lowest cells from the last cell or
     * element in the 'cells' list.
     * @param cells The list of cells to find the lowest cell from the last element in
     * this list.
     */
    public static void getLowestCells(List<PlanetCell> lowest, List<PlanetCell> cells){
        PlanetCell center = cells.get(cells.size() - 1);
        float geoHeight = center.getHeightWithoutOceans();
        for (int i = 0; i < cells.size() - 1; i++){
            PlanetCell cell = cells.get(i);
            if (cell.getHeightWithoutOceans() < geoHeight){
                lowest.add(cell);
            }
        }
    }

    /**
     * Selects all the positions that are around the position 'from'. This method
     * does not select resources or cells from the map but builds a list
     * of positions to use to select resources.
     * @param from The center position
     * @return The calculated positions around the center point 'from'
     */
    public static Point[] getCellIndexesFrom(Point from){
        int tx, ty, mx, my;
        int x = from.getX(), y = from.getY();
        int xl = DIR_X_INDEX.length;
        Point[] points = new Point[xl+1];
        int worldSize = Planet.instance().getCellLength();
        for (int s = 0; s < xl; s++) {

            tx = x + DIR_X_INDEX[s];
            ty = y + DIR_Y_INDEX[s];

            // Check the boundaries
            mx = checkBounds(tx, worldSize);
            my = checkBounds(ty, worldSize);

            Point p = new Point(mx, my);
            points[s] = p;
        }
        points[xl] = from;
        return points;
    }
    
    private class MinMaxHeightFactory implements TaskFactory {

        private List<MinMaxHeightTask> taskReferences;

        public MinMaxHeightFactory() {
            taskReferences = new ArrayList<>();
        }

        public float getHighestHeight(){
            float highest = Integer.MIN_VALUE;
            
            for (MinMaxHeightTask task : taskReferences) {
                float testHeight = task.getHighestHeight();
                if (testHeight > highest) {
                    highest = testHeight;
                }
            }
            return highest;
        }
        
        public float getLowestHeight() {

            float lowestHeight = Integer.MAX_VALUE;

            for (MinMaxHeightTask task : taskReferences) {
                float testHeight = task.getLowestHeight();
                if (testHeight < lowestHeight) {
                    lowestHeight = testHeight;
                }
            }

            return lowestHeight;
        }

        @Override
        public Task buildResource() {
            MinMaxHeightTask task = new MinMaxHeightTask();
            taskReferences.add(task);
            return task;
        }

        private class MinMaxHeightTask extends Task {

            private Delay delayTask;
            private float absLowestHeight, absHighestHeight;
            private AtomicFloat absLowest, absHighest;

            public void construct() {
            	absLowest = new AtomicFloat(0);
            	absHighest = new AtomicFloat(0);
                delayTask = new Delay(50);
            }

            @Override
            public void before() {
                absLowestHeight = absLowestHeight < 0 ? 0 : absLowestHeight;
                absLowest.set(absLowestHeight);
                
                absHighest.set(absHighestHeight);

                absLowestHeight = Integer.MAX_VALUE;
                absHighestHeight = Integer.MIN_VALUE;
            }
            
            @Override
            public void perform(int x, int y) {
                updateMinMaxHeight(x, y);
            }

            @Override
            public void after(){
            }
            
            private void updateMinMaxHeight(int x, int y) {
            	PlanetCell cell = waitForCellAt(x, y);
                float cellHeight = cell.getHeightWithoutOceans();
                release(cell);
                if (cellHeight < absLowestHeight) {
                    absLowestHeight = cellHeight;
                }
                
                if (cellHeight > absHighestHeight){
                    absHighestHeight = cellHeight;
                }
            }

            public float getHighestHeight(){
                return absHighest.get();
            }
            
            public float getLowestHeight() {
                return absLowest.get();
            }

            @Override
            public boolean check() {
                return delayTask.check();
            }
            
        }
    }
}
