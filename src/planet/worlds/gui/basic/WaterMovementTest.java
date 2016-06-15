

package planet.worlds.gui;

import java.util.Deque;
import java.util.LinkedList;
import javax.swing.JFrame;
import planet.worlds.planet.cells.PlanetCell;
import planet.worlds.TestWorld;
import planet.worlds.planet.enums.Layer;
import planet.gui.DisplayAdapter;
import planet.worlds.planet.surface.PlanetSurface;
import planet.util.Delay;
import planet.util.Task;
import planet.util.TaskAdapter;

/**
 * This test shows the movement of water over a surface and the erosion of
 * that surface. Water will be added and subtracted with the constructed
 * Tasks. After a given amount of time water will no longer be added and the
 * rest of the water will drain away.
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
        
        PlanetSurface.suppressAtmosphere = true;
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
                surface.getCellAt(x, y).addToStrata(Layer.SANDSTONE, 1500000, true);
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
                
                PlanetSurface surface = (PlanetSurface) testWorld.getSurface();
                PlanetCell cell1 = surface.getCellAt(25, 6);
                PlanetCell cell2 = surface.getCellAt(25, 7);
                
                addWaterToCell(cell1, -100000);
                addWaterToCell(cell2, -100000);
                
            }
        }

        private void addWaterToCell(PlanetCell cell, float amount) {
            cell.addOceanMass(amount);
            cell.getSedimentBuffer().removeAllSediments();
            cell.getSuspendedSedimentBuffer().resetBuffer();
        }
    }
    
    private class AddWaterTask implements Task {
        // Time the test for only 40000 frames then stop tasking.
        private Delay timer;
        public AddWaterTask(){
            timer = new Delay(10000, false);
        }
        @Override
        public void perform(int x, int y) {
            if (x == 1 && y == 4){
                testWorld.getSurface().getCellAt(1, 11).addOceanMass(100);
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