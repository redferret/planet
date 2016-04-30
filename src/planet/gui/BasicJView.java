

package planet.gui;

import javax.swing.JFrame;
import planet.TestWorld;

/**
 *
 * @author Richard DeSilvey
 */
public class BasicJView extends JFrame {
    
    private Frame renderFrame;
    private TestWorld testWorld;
    
    private static final int SIZE = 512;
    
    public BasicJView(){
        super("Test World");
        
        testWorld = new TestWorld();
        renderFrame = new Frame(SIZE, SIZE);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
        
        setLocationRelativeTo(null);
        pack();
        setVisible(true);
        
        testWorld.start();
    }
    
}
