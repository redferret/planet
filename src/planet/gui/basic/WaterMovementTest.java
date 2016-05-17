

package planet.gui.basic;

import java.util.Deque;
import java.util.LinkedList;
import javax.swing.JFrame;
import planet.TestWorld;
import planet.enums.Layer;
import planet.gui.DisplayAdapter;
import planet.surface.PlanetSurface;
import planet.util.SurfaceThread;

/**
 *
 * @author Richard DeSilvey
 */
public class WaterMovementTest extends JFrame implements DisplayAdapter {
    
    private Frame renderFrame;
    private TestWorld testWorld;
    private Deque<Integer> averages;
    private int totalAvg;
    
    private static final int SIZE = 512;
    
    public WaterMovementTest(){
        super("Test World");
        
        averages = new LinkedList<>();
        totalAvg = 0;
        
        renderFrame = new Frame(SIZE, SIZE);
        
        testWorld = new TestWorld(33, 3);
        testWorld.getSurface().setDisplay(this);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
        
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);
        
        SurfaceThread.suppressMantelHeating = true;
        PlanetSurface surface = (PlanetSurface) testWorld.getSurface();
        surface.addToSurface(Layer.SHALE, 100000);
        surface.getCellAt(49, 45).addOceanMass(1000000);
        
        testWorld.play();
    }
    
    @Override
    public void update() {
        renderFrame.repaint();
        
        averages.add(testWorld.getSurface().getAverageThreadTime());
        final int SAMPLES = 10;
        
        if (averages.size() == SAMPLES){
            totalAvg = 0;
            while(!averages.isEmpty()){
                totalAvg += averages.poll();
            }
            totalAvg /= SAMPLES;
        }
        
        long age = testWorld.getSurface().getPlanetAge();
        float lowestHeight = testWorld.getSurface().getLowestHeight();
        setTitle("Age: " + age + " F:" + totalAvg + " L:" + lowestHeight);
    }
    
    public static void main(String[] args){
        new WaterMovementTest();
    }
    
}