

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
        testWorld.start();
        
        renderFrame = new Frame(SIZE, SIZE);
        
        setLocationRelativeTo(null);
        
        add(renderFrame);
        pack();
        setVisible(true);
    }
    
}
