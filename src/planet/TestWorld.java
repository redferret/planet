

package planet;


/**
 * This class is used for testing purposes.
 * @author Richard DeSilvey
 */
public class TestWorld extends Planet {

    private static final int CELL_SIZE_M, MAIN_DELAY, DEFAULT_SIZE, 
            PLANET_DELAY, THREAD_COUNT;
    
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
        super(worldSize, CELL_SIZE_M, PLANET_DELAY, THREAD_COUNT);
        initPlanet();
        startThreads();
    }
}
