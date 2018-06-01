package engine;

import com.jme3.app.SimpleApplication;
import com.jme3.renderer.RenderManager;
import worlds.planet.TestWorld;

/**
 * Main entry point
 */
public class App extends SimpleApplication {

  private static TestWorld world;
  
  public static void main(String[] args) {
    
    world = new TestWorld();
    world.play();
    
    App app = new App();
    app.showSettings = false;
    app.start();
  }

  @Override
  public void simpleInitApp() {
    inputManager.setCursorVisible(true);
  }

  @Override
  public void simpleUpdate(float tpf) {
    
  }

  @Override
  public void simpleRender(RenderManager rm) {

  }

  @Override
  public void destroy() {
    super.destroy();
    world.shutdown();
  }
  
}
