
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import planet.util.Delay;

/**
 * Tests the Delay class.
 * @author Richard DeSilvey
 */
public class DelayTest {
    
    private static final int FRAMES_TO_SKIP = 3;
    
    @Test
    public void noResetOnDelayTest(){
        
        final boolean reset = false;
        Delay testDelay = new Delay(FRAMES_TO_SKIP, reset);
        
        boolean expectedReturns[] = {false, false, true, true, true};
        
        for (boolean b : expectedReturns){
            assertTrue(b == testDelay.check());
        }
        
        testDelay.reset();
        
        assertTrue(testDelay.check() == false);
        
    }
    
    /**
     * Tests a delay object that resets for every x number of frames.
     */
    @Test
    public void resetDelayTest(){
        Delay testDelay = new Delay(FRAMES_TO_SKIP);
        
        boolean expectedReturns[] = {false, false, true, false, false, true};
        
        for (boolean b : expectedReturns){
            assertTrue(b == testDelay.check());
        }
        
    }
    
}
