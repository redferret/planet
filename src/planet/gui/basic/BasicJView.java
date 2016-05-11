

package planet.gui.basic;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Deque;
import java.util.LinkedList;
import javax.swing.JFrame;
import planet.Planet;
import planet.Planet.TimeScale;
import planet.TestWorld;
import planet.enums.Layer;
import planet.gui.DisplayAdapter;
import planet.surface.Hydrosphere;
import planet.surface.PlanetSurface;
import planet.surface.Surface;

/**
 *
 * @author Richard DeSilvey
 */
public class BasicJView extends JFrame implements DisplayAdapter {
    
    private Frame renderFrame;
    private TestWorld testWorld;
    private Deque<Integer> averages;
    private int totalAvg;
    
    private static final int SIZE = 512;
    
    public BasicJView(){
        super("Test World");
        
        averages = new LinkedList<>();
        totalAvg = 0;
        
        renderFrame = new Frame(SIZE, SIZE);
        
        testWorld = new TestWorld(50);
        testWorld.getSurface().setDisplay(this);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
        
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);
        
        testWorld.setTimescale(TimeScale.None);
        PlanetSurface surface = (PlanetSurface) testWorld.getSurface();
        surface.addToSurface(Layer.BASALT, 100000);
        surface.addToSurface(Layer.SANDSTONE, 50000);
        for (int x = 0; x < 30; x++) {
            for (int y = 0; y < 50; y++){
                surface.getCellAt(x, y).add(Layer.SHALE, 1000000, true);
            }
        }
        
        for (int x = 0; x < 30; x++){
            surface.getCellAt(x, 10).add(Layer.SHALE, 100000, true);
        }
        
        for (int y = 0; y < 50; y++){
            surface.getCellAt(0, y).add(Layer.SHALE, 100000, true);
        }
        
        for (int x = 0; x < 30; x++){
            surface.getCellAt(x, 29).add(Layer.SHALE, 100000, true);
        }
        surface.getCellAt(11, 11).addOceanMass(1000);

        
        testWorld.play();
    }
    
    @Override
    public void update() {
        renderFrame.repaint();
        
        averages.add(testWorld.getSurface().getAverageThreadTime());
        
        if (averages.size() == 10){
            totalAvg = 0;
            while(!averages.isEmpty()){
                totalAvg += averages.poll();
            }
            totalAvg /= 25;
        }
        
        setTitle("Age: " + testWorld.getSurface().getPlanetAge() 
                + " F:" + totalAvg + " L:" + testWorld.getSurface().getLowestHeight());
    }
    
    public static void main(String[] args){
        new BasicJView();
    }
    
}

class KeyController extends KeyAdapter {

    @Override
    public void keyReleased(KeyEvent e) {
        
        switch(e.getKeyCode()){
            
            case KeyEvent.VK_INSERT:
                Hydrosphere.drawOcean = !Hydrosphere.drawOcean;
                break;
            
            case KeyEvent.VK_PAGE_UP:
                Planet.self().getSurface().displaySetting++;
                
                if (Planet.self().getSurface().displaySetting > 2){
                    Planet.self().getSurface().displaySetting = 2;
                }
                break;
                
            case KeyEvent.VK_PAGE_DOWN:
                Planet.self().getSurface().displaySetting--;
                
                if (Planet.self().getSurface().displaySetting < 0){
                    Planet.self().getSurface().displaySetting = 0;
                }
                break;
        }
    }
    
}

class JAdapter extends WindowAdapter {
    @Override
    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }
}