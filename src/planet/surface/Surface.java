
package planet.surface;

import planet.enums.Layer;
import planet.cells.AtmoCell;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import planet.gui.DisplayAdapter;
import planet.util.Delay;
import static planet.surface.Surface.GEOUPDATE;
import static planet.surface.Surface.planetAge;

/**
 * The Surface is the geology for the planet. It provides a foundation
 * for life to grow on and to influence climate in many ways. Through
 * the placement of continents, the type of surface which provides albedo
 * rating for solar radiation absorbtions, and volcanism which pours out
 * CO2 and water vapor.
 *
 * @author Richard DeSilvey
 */
public abstract class Surface extends SurfaceMap<AtmoCell> {

    /**
     * The average density of the mantel. The units are in kilograms per cubic
     * meter.
     */
    public static float mantel_density = 3700f;

    /**
     * A mutable erosion quantity (works best around 32 - 128) during
     * geological time scales
     */
    public static float erosionAmount;
    
    /**
     * A variable that controls how thick sediment layers need to be
     * before converting them to sedimentary rock.
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
    
    /**
     * 
     */
    public static AtomicInteger absLowestHeight;
    protected int worldSize;
    
    private Delay delay;
    
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
        absLowestHeight = new AtomicInteger(Integer.MAX_VALUE);
    }
    
    /**
     * Constructs a new Surface.
     * @param worldSize The size of the surface
     * @param surfaceDelay The amount of time to delay updating planet age
     * @param threadsDelay The amount of time to delay each frame in milliseconds.
     * @param threadCount The number of threads that will work on the map
     */
    public Surface(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, 10, "Geosphere", threadCount);
        this.worldSize = worldSize;
        delay = new Delay(surfaceDelay);
        reset();
        setupThreads(threadCount, threadsDelay);
    }

    public void reset(){
        planetAge = new AtomicLong(0);
        geologicalTimeStamp = 0;
    }

    public void setDisplay(DisplayAdapter display) {
        this.display = display;
    }
    
    public long getPlanetAge(){
        return planetAge.get();
    }
    
    public void updateMinimumHeight(int x, int y){
        float cellHeight = getCellAt(x, y).getHeight();
        
        if (cellHeight < absLowestHeight.get()){
            absLowestHeight.set((int) cellHeight);
        }
    }
    
    /**
     * Add a uniformed layer on the whole surface.
     * @param type The layer being added
     * @param amount The amount being added
     */
    public void addToSurface(Layer type, float amount){
        for (int x = 0; x < worldSize; x++){
            for (int y = 0; y < worldSize; y++){
                getCellAt(x, y).add(type, amount, true);
            }
        }
    }
    
    @Override
    public void update() {
        
        if (delay.check()){
            long curPlanetAge = planetAge.getAndAdd(ageStep);
            if (curPlanetAge - geologicalTimeStamp > GEOUPDATE){
                // < Update to major geological events go here >

                geologicalTimeStamp = curPlanetAge;
            }
        }
        display.update();
    }
    
    @Override
    public AtmoCell generateCell(int x, int y) {
        return new AtmoCell(x, y);
    }

}


