

package planet;

import java.util.logging.Level;
import java.util.logging.Logger;
import planet.surface.generics.SurfaceMap;

/**
 * This class is used for testing purposes.
 * @author Richard DeSilvey
 */
public class TestWorld extends Planet {

    private static final int CELL_SIZE_M, MAIN_DELAY, DEFAULT_SIZE, 
            PLANET_DELAY, THREAD_COUNT;
    private static boolean firstMsg = false;
    
    static{
        CELL_SIZE_M = 6;
        MAIN_DELAY = 1;
        THREAD_COUNT = 1;
        PLANET_DELAY = 1000;
        DEFAULT_SIZE = 256;
    }
    
    /**
     * Constructs a default test world with a size of 256
     */
    public TestWorld(){
        this(DEFAULT_SIZE);
    }
    
    /**
     * Constructs a new test world for the simulation.
     * @param worldSize The size of the world squared.
     */
    public TestWorld(int worldSize) {
        super(worldSize, CELL_SIZE_M, MAIN_DELAY, PLANET_DELAY, THREAD_COUNT);
        
        initPlanet();
        
        startThreads();
        play();
    }

    @Override
    public void update() {
        if (!firstMsg){
            Logger.getLogger(SurfaceMap.class.getName()).log(Level.INFO, 
                    "First update made on World");
            firstMsg = !firstMsg;
        }
        
        try {
            updateSurface();
        } catch (Exception e) {
            Logger.getLogger(TestWorld.class.getName()).log(Level.SEVERE,
                    "An exception was caught while updating the surface");
            e.printStackTrace();
        }
    }

}
