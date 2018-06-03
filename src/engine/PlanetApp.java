package engine;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import engine.states.SurfaceState;
import engine.states.WorldState;
import engine.util.Delay;

/**
 * Main entry point
 */
public class PlanetApp extends SimpleApplication {

  private SurfaceStateManager surfaceStateManager;
  
  public static void main(String[] args) {
    
    PlanetApp app = new PlanetApp();
    
    app.showSettings = false;
    app.settings = new AppSettings(true);
//    app.settings.setFullscreen(true);
    app.settings.setTitle("Planet");
    app.start();
  }
  
  @Override
  public void simpleInitApp() {
    
    surfaceStateManager = new SurfaceStateManager(this);
    
    flyCam.setMoveSpeed(400f);
    flyCam.setZoomSpeed(20f);
    
    setDisplayStatView(false);
    inputManager.setCursorVisible(true);
    
    DirectionalLight light = new DirectionalLight();
    light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
    rootNode.addLight(light);

    this.getCamera().setLocation(getCamera().getLocation().add(0, 300, 250));
    
    inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
    inputManager.addListener(actionListener, "wireframe");
    
    stateManager.attach(new WorldState());
    surfaceStateManager.attach(new SurfaceState());
    surfaceStateManager.start();
  }
  
  public SurfaceStateManager getSurfaceStateManager() {
    return surfaceStateManager;
  }
  
  private final ActionListener actionListener = new ActionListener() {
    @Override
    public void onAction(String name, boolean pressed, float tpf) {
      if (name.equals("wireframe") && !pressed) {
        surfaceStateManager.getState(SurfaceState.class).negWireFramed();
      }
    }
  };

  @Override
  public void simpleUpdate(float tpf) {

  }

  @Override
  public void simpleRender(RenderManager rm) {

  }

  @Override
  public void destroy() {
    super.destroy();
    surfaceStateManager.cleanup();
  }

}
