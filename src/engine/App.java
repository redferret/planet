package engine;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import worlds.planet.TestWorld;

/**
 * This is the Main Class of your Game. You should only do initialization here.
 * Move your Logic into AppStates or Controls
 *
 * @author normenhansen
 */
public class App extends SimpleApplication {

  private TestWorld world;
  
  public static void main(String[] args) {
    App app = new App();
    app.showSettings = false;
    app.start();
  }

  @Override
  public void simpleInitApp() {
    
  }

  @Override
  public void simpleUpdate(float tpf) {
  }

  @Override
  public void simpleRender(RenderManager rm) {

  }
}
