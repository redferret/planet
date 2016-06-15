package worlds.planet.surface;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import worlds.planet.cells.PlanetCell;
import planet.gui.DisplayAdapter;
import planet.surface.SurfaceMap;
import planet.util.Delay;
import planet.util.Task;

import static worlds.planet.surface.Surface.GEOUPDATE;
import static worlds.planet.surface.Surface.planetAge;
import planet.util.TaskFactory;
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
    private Deque<Task> generalTasks;

    private Delay ageUpdateDelay, threadAverageDelay;

    public final static int HEIGHTMAP = 0;
    public final static int STRATAMAP = 1;
    public final static int LANDOCEAN = 2;

    private static final String NAME = "Surface Map Thread";
    private static final int DEFAULT_THREAD_DELAY = 1;
    
    private MinMaxHeightFactory mhFactory;
    /**
     * Used primarily for erosion algorithms.
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
        set();
        setupThreads(threadCount, threadsDelay);
        mhFactory = new MinMaxHeightFactory();
        produceTasks(mhFactory);
    }

    private void set() {
        threadAverageDelay = new Delay(500);
        display = null;
        generalTasks = new LinkedList<>();
        reset();
    }

    /**
     * Resets the surface to an empty map and resets the planet's age.
     * This method should be calling the <code>setupMap()</code> method.
     */
    public void reset() {
        planetAge = new AtomicLong(0);
        geologicalTimeStamp = 0;
        setupMap();
    }

    public void setDisplay(DisplayAdapter display) {
        this.display = display;
    }

    public long getPlanetAge() {
        return planetAge.get();
    }
    
    public void addTask(Task task){
        generalTasks.add(task);
    }
    
    @Override
    public void update() {

        updatePlanetAge();

        if (display != null) {
            display.update();
        }
        if (threadAverageDelay.check()) {
            super.update();
        }
        
        performTasks();
        
    }

    private void updatePlanetAge() {
        if (ageUpdateDelay.check()) {
            long curPlanetAge = planetAge.getAndAdd(timeStep);
            if (curPlanetAge - geologicalTimeStamp > GEOUPDATE) {
                // < Update to major geological events go here >
                
                geologicalTimeStamp = curPlanetAge;
            }
        }
    }

    private void performTasks() {
        int worldSize = getGridWidth();
        generalTasks.forEach(task -> {
            if (task.check()) {
                for (int y = 0; y < worldSize; y++) {
                    for (int x = 0; x < worldSize; x++) {
                        task.perform(x, y);
                    }
                }
            }
        });
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

        private class MinMaxHeightTask implements Task {

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
                if (delayTask.check()){
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
                    return true;
                }else{
                    return false;
                }
            }
        }
    }
}
