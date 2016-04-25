

package planet.surface.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import planet.Planet;
import planet.surface.generics.SurfaceMap;

/**
 * This class is used for testing purposes.
 * @author Richard DeSilvey
 */
public class World extends Planet {

    private static final int CELL_SIZE_KM, MAIN_DELAY;
    private static boolean firstMsg = false;
    
    static{
        CELL_SIZE_KM = 6;
        MAIN_DELAY = 1;
    }
    /**
     * Constructs a new test world for the simulation.
     * @param worldSize The size of the world squared.
     */
    public World(int worldSize) {
        super(worldSize, CELL_SIZE_KM, MAIN_DELAY);
        
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
            Logger.getLogger(World.class.getName()).log(Level.SEVERE,
                    "An exception was caught while updating the surface");
            e.printStackTrace();
        }
    }

}
