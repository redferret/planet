

package worlds.gui.ogl;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import engine.gui.DisplayAdapter;
import static org.lwjgl.opengl.GL11.GL_FILL;
import static org.lwjgl.opengl.GL11.GL_FRONT;
import static org.lwjgl.opengl.GL11.GL_LINE;
import static org.lwjgl.opengl.GL11.GL_POINT;
import static org.lwjgl.opengl.GL11.GL_POLYGON_MODE;
import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL11.glPolygonMode;

/**
 *
 * @author Richard DeSilvey
 */
public class OGLDisplay implements DisplayAdapter {
    
    private int width, height;
    private EulerCamera camera;
    
    public OGLDisplay(int width, int height){
        
        float aspectRatio = width*1f / height*1f;
        camera = new EulerCamera.EulerFactory()
                .setPosition(-5.4f, 19.2f, 33.2f)
                .setRotation(30, 61, 0)
                .setAspectRatio(aspectRatio)
                .setFieldOfView(60).build();
        
        try {
            Display.setDisplayMode(new DisplayMode(640, 480));
            Display.setTitle("Planet");
            Display.create();
            
        } catch (LWJGLException e) {
            Logger.getLogger(OGLDisplay.class.getName()).log(Level.SEVERE, 
                    "LWJGLException caught: {0}", e.getMessage());
            Display.destroy();
        }
    }
    
    private void checkInput() {
        while (Keyboard.next()) {
            if (Keyboard.getEventKeyState()) {

                if (Keyboard.getEventKey() == Keyboard.KEY_P) {
                    // Switch between normal mode, point mode, and wire-frame mode.
                    switch(glGetInteger(GL_POLYGON_MODE)){
                        case GL_LINE:
                            glPolygonMode(GL_FRONT, GL_FILL);
                            break;
                        case GL_FILL:
                            glPolygonMode(GL_FRONT, GL_POINT);
                            break;
                        case GL_POINT:
                            glPolygonMode(GL_FRONT, GL_LINE);
                            break;
                    }
                }
            }
        }
        if (Mouse.isButtonDown(0)) {
            camera.processMouse(1, 80, -80);
        }
        camera.processKeyboard(16, 1);
    }

    @Override
    public void update() {
        checkInput();
    }
    
    
    
}
