

package planet.gui;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import planet.TestWorld;

/**
 *
 * @author Richard DeSilvey
 */
public class BasicJView extends JFrame implements DisplayAdapter {
    
    private Frame renderFrame;
    private TestWorld testWorld;
    
    private static final int SIZE = 512;
    
    public BasicJView(){
        super("Test World");
        
        testWorld = new TestWorld();
        testWorld.getSurface().setDisplay(this);
        
        renderFrame = new Frame(SIZE, SIZE);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
        
        addWindowListener(new JAdapter());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);
        
        testWorld.play();
        // Example of changing the render speed and planet age update
        testWorld.getSurface().setDelay(250);
    }
    
    @Override
    public void repaint() {
        super.repaint();
        setTitle("Age: " + testWorld.getSurface().getPlanetAge());
    }
    
    public static void main(String[] args){
        new BasicJView();
    }
    
}

class JAdapter extends WindowAdapter {
    @Override
    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }
}