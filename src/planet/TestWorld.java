

package planet;


/**
 * This class is used for testing purposes.
 * @author Richard DeSilvey
 */
public class TestWorld extends Planet {

    private static final int CELL_SIZE_M, DEFAULT_SIZE, 
            PLANET_DELAY, THREAD_COUNT, AGE_STEP_DELAY;
    
    static{
        CELL_SIZE_M = 6;
        THREAD_COUNT = 1;
        PLANET_DELAY = 10;
        AGE_STEP_DELAY = 100;
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
        super(worldSize, CELL_SIZE_M, AGE_STEP_DELAY, PLANET_DELAY, THREAD_COUNT);
        startThreads();
    }
}
