package worlds.planet.surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import worlds.planet.cells.PlanetCell;
import engine.gui.DisplayAdapter;
import engine.surface.SurfaceMap;
import engine.surface.SurfaceThread;
import engine.util.Boundaries;
import engine.util.Delay;
import engine.util.Task;
import engine.util.TaskFactory;
import engine.util.TaskManager;
import java.util.Deque;
import java.util.logging.Level;
import java.util.logging.Logger;
import worlds.planet.cells.geology.PlanetObject;
import static worlds.planet.surface.Surface.GEOUPDATE;
import static worlds.planet.surface.Surface.planetAge;

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

    private static final String NAME = "SurfaceMap Thread";
    private static final int DEFAULT_THREAD_DELAY = 1;
    
    private MinMaxHeightFactory mhFactory;
    
    private Deque<PlanetObject> objects;
    
    /**
     * Internal cached Random object for surfaces.
     */
    protected static final Random random;

    static {
        random = new Random();
        timeStep = 100000;
        GEOUPDATE = 100000;
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
        super(planetWidth, DEFAULT_THREAD_DELAY, NAME, threadCount);
        ageUpdateDelay = new Delay(ageStepDelay);
        setupThreads(threadCount, threadsDelay);
        setupDefaultMap(planetWidth, threadCount);
        mhFactory = new MinMaxHeightFactory();
        produceTasks(mhFactory);
        Boundaries bounds = new Boundaries(0, planetWidth, 0, planetWidth);
        generalTasks = new TaskManager(bounds);
        set();
    }

    private void set() {
        threadAverageDelay = new Delay(500);
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
    
    private class MinMaxHeightFactory implements TaskFactory {

        private List<MinMaxHeightTask> tasks;

        public MinMaxHeightFactory() {
            tasks = new ArrayList<>();
        }

        public float getHighestHeight(){
            float highest = Integer.MIN_VALUE;
            
            for (MinMaxHeightTask task : tasks) {
                float testHeight = task.getHighestHeight();
                if (testHeight > highest) {
                    highest = testHeight;
                }
            }
            return highest;
        }
        
        public float getLowestHeight() {

            float lowestHeight = Integer.MAX_VALUE;

            for (MinMaxHeightTask task : tasks) {
                float testHeight = task.getLowestHeight();
                if (testHeight < lowestHeight) {
                    lowestHeight = testHeight;
                }
            }

            return lowestHeight;
        }

        @Override
        public Task buildTask() {
            MinMaxHeightTask task = new MinMaxHeightTask();
            tasks.add(task);
            return task;
        }

        private class MinMaxHeightTask extends Task {

            private Delay delayTask;
            private float absLowestHeight, absHighestHeight;
            private AtomicInteger lowestHeightIntPart;
            private AtomicInteger lowestHeightDecPart;
            
            private AtomicInteger highestHeightIntPart;
            private AtomicInteger highestHeightDecPart;

            public MinMaxHeightTask() {
                lowestHeightIntPart = new AtomicInteger(0);
                lowestHeightDecPart = new AtomicInteger(0);
                
                highestHeightIntPart = new AtomicInteger(0);
                highestHeightDecPart = new AtomicInteger(0);
                
                delayTask = new Delay(50);
            }

            @Override
            public void perform(int x, int y) {
                updateMinMaxHeight(x, y);
            }

            private void updateMinMaxHeight(int x, int y) {
                float cellHeight = getCellAt(x, y).getHeightWithoutOceans();

                if (cellHeight < absLowestHeight) {
                    absLowestHeight = cellHeight;
                }
                
                if (cellHeight > absHighestHeight){
                    absHighestHeight = cellHeight;
                }
            }

            public float getHighestHeight(){
                float decPart = highestHeightDecPart.get() / 10f;
                decPart = highestHeightIntPart.get() + decPart;

                return decPart;
            }
            
            public float getLowestHeight() {
                float decPart = lowestHeightDecPart.get() / 10f;
                decPart = lowestHeightIntPart.get() + decPart;

                return decPart;
            }

            @Override
            public boolean check() {
                return delayTask.check();
            }
            
            @Override
            public void before() {
                absLowestHeight = absLowestHeight < 0 ? 0 : absLowestHeight;
                int intPart = (int) absLowestHeight;
                int decPart = (int) ((absLowestHeight - intPart) * 10);

                lowestHeightIntPart.set(intPart);
                lowestHeightDecPart.set(decPart);

                intPart = (int) absHighestHeight;
                decPart = (int) ((absHighestHeight - intPart) * 10);

                highestHeightIntPart.set(intPart);
                highestHeightDecPart.set(decPart);

                absLowestHeight = Integer.MAX_VALUE;
                absHighestHeight = Integer.MIN_VALUE;
            }

            @Override
            public void after() {
            }
        }
    }
}
