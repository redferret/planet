

package planet.gui.basic;

import java.util.Deque;
import java.util.LinkedList;
import javax.swing.JFrame;
import planet.TestWorld;
import planet.enums.Layer;
import planet.gui.DisplayAdapter;
import planet.surface.PlanetSurface;
import planet.util.Delay;
import planet.util.Task;
import planet.util.TaskAdapter;

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

        testWorld = new TestWorld(30, 1);
        testWorld.getSurface().setDisplay(this);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
        
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);
        
        PlanetSurface.suppressMantelHeating = true;
        PlanetSurface surface = (PlanetSurface) testWorld.getSurface();
        surface.addToSurface(Layer.SHALE, 10000000);
        
        flatErosionTest();
        
        testWorld.play();
    }
    
    private void flatErosionTest(){
        PlanetSurface surface = (PlanetSurface) testWorld.getSurface();
        
        for (int y = 4; y < 20; y++)
            surface.getCellAt(0, y).addToStrata(Layer.BASALT, 10000000, true);
        
        for (int y = 4; y < 20; y++){
            for (int x = 0; x < 10; x++){
                surface.getCellAt(x, y).addToStrata(Layer.SANDSTONE, 1000000, true);
                surface.getCellAt(x, y).addToStrata(Layer.SEDIMENT, 10000, true);
            }
        }
        for (int x = 0; x < 10; x++){
            surface.getCellAt(x, 3).addToStrata(Layer.BASALT, 10000000, true);
        }
        for (int x = 0; x < 10; x++){
            surface.getCellAt(x, 20).addToStrata(Layer.BASALT, 10000000, true);
        }
        testWorld.getSurface().addTask(new AddWaterTask());
        testWorld.getSurface().addTask(new EmptyWaterTask());
    }
    
    private class EmptyWaterTask extends TaskAdapter {
        @Override
        public void perform(int x, int y) {
            if (x == 0 && y == 6){
                testWorld.getSurface().getCellAt(25, 6).addOceanMass(-100000);
            }
        }
    }
    
    private class AddWaterTask implements Task {
        private Delay timer;
        public AddWaterTask(){
            timer = new Delay(40000, false);
        }
        @Override
        public void perform(int x, int y) {
            if (x == 1 && y == 4){
                testWorld.getSurface().getCellAt(1, 11).addOceanMass(1);
            }
        }

        @Override
        public boolean check() {
            return !timer.check();
        }
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