

package planet.gui;

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
import planet.surface.Layer;

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
        
        testWorld = new TestWorld();
        testWorld.getSurface().setDisplay(this);
        
        renderFrame = new Frame(SIZE, SIZE);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
        
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);
        
        testWorld.setTimescale(TimeScale.Geological);
        testWorld.getSurface().addToSurface(Layer.BASALT, 10000);
        
        testWorld.play();
        // Example of changing the render speed and planet age update
        testWorld.getSurface().setDelay(500);
    }
    
    @Override
    public void repaint() {
        renderFrame.repaint();
        
        averages.add(testWorld.getSurface().getAverageThreadTime());
        
        if (averages.size() == 10){
            totalAvg = 0;
            while(!averages.isEmpty()){
                totalAvg += averages.poll();
            }
            totalAvg /= 25;
        }
        
        setTitle("Age: " + testWorld.getSurface().getPlanetAge() + " F:" + totalAvg);
    }
    
    public static void main(String[] args){
        new BasicJView();
    }
    
}

class KeyController extends KeyAdapter {

    @Override
    public void keyReleased(KeyEvent e) {
        
        switch(e.getKeyCode()){
            
            case KeyEvent.VK_PAGE_UP:
                Planet.self().getSurface().displaySetting++;
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