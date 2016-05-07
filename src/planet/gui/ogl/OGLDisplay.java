

package planet.gui.ogl;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import planet.gui.DisplayAdapter;
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
        camera = new EulerCamera.Builder().setPosition(-5.4f, 19.2f,
            33.2f).setRotation(30, 61, 0).setAspectRatio(aspectRatio).setFieldOfView(60).build();
        
        try {
            // Sets the width of the display to 640 and the height to 480
            Display.setDisplayMode(new DisplayMode(640, 480));
            // Sets the title of the display to "Episode 2 - Display"
            Display.setTitle("Episode 2 - Display");
            // Creates and shows the display
            Display.create();
            
        } catch (LWJGLException e) {
            e.printStackTrace();
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
        Mouse.setGrabbed(Mouse.isButtonDown(0));

        if (Mouse.isGrabbed()) {
            camera.processMouse(1, 80, -80);
        }
        camera.processKeyboard(16, 1);
    }

    @Override
    public void update() {
        checkInput();
    }
    
    
    
}
