package planet.surface;

import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import planet.cells.PlanetCell;
import planet.gui.DisplayAdapter;
import planet.util.Delay;
import planet.util.SurfaceThread;
import static planet.surface.Surface.GEOUPDATE;
import static planet.surface.Surface.planetAge;

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
     * A mutable erosion quantity (works best around 32 - 128) during geological
     * time scales
     */
    public static float erosionAmount;

    /**
     * A variable that controls how thick sediment layers need to be before
     * converting them to sedimentary rock.
     */
    public static float ssMul = 1.0f;

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
    public static long ageStep;

    private DisplayAdapter display;

    protected int worldSize;

    private Delay ageUpdateDelay, threadAverageDelay;

    public final static int HEIGHTMAP = 0;
    public final static int STRATAMAP = 1;
    public final static int LANDOCEAN = 2;

    /**
     * Used primarily for erosion algorithms.
     */
    protected static final Random rand;

    static {
        rand = new Random();
        erosionAmount = 1;
        ageStep = 100000;
        GEOUPDATE = 100000;
    }

    /**
     * Constructs a new Surface.
     *
     * @param planetWidth The size of the surface
     * @param ageStepDelay The amount of time to delay updating planet age
     * @param threadsDelay The amount of time to delay each frame in
     * milliseconds.
     * @param threadCount The number of threads that will work on the map
     */
    public Surface(int planetWidth, int ageStepDelay, int threadsDelay, int threadCount) {
        super(planetWidth, 1, "Geosphere", threadCount);
        this.worldSize = planetWidth;
        ageUpdateDelay = new Delay(ageStepDelay);
        set();
        setupThreads(threadCount, threadsDelay);
    }

    private void set() {
        threadAverageDelay = new Delay(250);
        display = null;
        reset();
    }

    public void reset() {
        planetAge = new AtomicLong(0);
        geologicalTimeStamp = 0;
        setupMap();
    }

    public void setDisplay(DisplayAdapter display) {
        this.display = display;
    }

    public float getLowestHeight() {
        float lowest = Integer.MAX_VALUE;
        float h;
        for (SurfaceThread thread : threads) {
            h = thread.getPreviousLowestHeight();
            if (h < lowest) {
                lowest = h;
            }
        }

        return lowest;
    }

    public long getPlanetAge() {
        return planetAge.get();
    }

    @Override
    public void update() {

        if (ageUpdateDelay.check()) {
            long curPlanetAge = planetAge.getAndAdd(ageStep);
            if (curPlanetAge - geologicalTimeStamp > GEOUPDATE) {
                // < Update to major geological events go here >

                geologicalTimeStamp = curPlanetAge;
            }
        }

        if (display != null) {
            display.update();
        }
        if (threadAverageDelay.check()) {
            super.update();
        }
    }

    @Override
    public PlanetCell generateCell(int x, int y) {
        return new PlanetCell(x, y);
    }

}
