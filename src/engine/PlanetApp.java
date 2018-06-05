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
import static engine.states.SurfaceState.*;
import engine.states.WorldState;

/**
 * Main entry point
 */
public class PlanetApp extends SimpleApplication {

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
    
    flyCam.setMoveSpeed(500f);
    flyCam.setZoomSpeed(20f);
    
    setDisplayStatView(false);
    inputManager.setCursorVisible(true);
    
    DirectionalLight light = new DirectionalLight();
    light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
    rootNode.addLight(light);

    this.getCamera().setLocation(getCamera().getLocation().add(0, 300, 250));
    
    inputManager.addMapping(LITHOSPHERE, new KeyTrigger(KeyInput.KEY_1));
    inputManager.addMapping(UPPER_MANTLE, new KeyTrigger(KeyInput.KEY_2));
    inputManager.addMapping(LOWER_MANTLE, new KeyTrigger(KeyInput.KEY_3));
    inputManager.addMapping(CORE, new KeyTrigger(KeyInput.KEY_4));
    inputManager.addMapping("playpause", new KeyTrigger(KeyInput.KEY_P));
    inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
    inputManager.addListener(actionListener, "wireframe", "playpause",
            LITHOSPHERE, UPPER_MANTLE,LOWER_MANTLE, CORE);
    
    stateManager.attach(new WorldState());
    stateManager.attach(new SurfaceState());
  }
  
  private final ActionListener actionListener = (String name, boolean pressed, float tpf) -> {
    if (!pressed) {
      switch(name) {
        case "wireframe":
          stateManager.getState(SurfaceState.class).negWireFramed();
          break;
        case "playpause":
          stateManager.getState(WorldState.class).setIsPaused();
          break;
        case LITHOSPHERE:
          stateManager.getState(SurfaceState.class).renderSurface(LITHOSPHERE);
          break;
        case UPPER_MANTLE:
          stateManager.getState(SurfaceState.class).renderSurface(UPPER_MANTLE);
          break;
        case LOWER_MANTLE:
          stateManager.getState(SurfaceState.class).renderSurface(LOWER_MANTLE);
          break;
        case CORE:
          stateManager.getState(SurfaceState.class).renderSurface(CORE);
          break;
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
  }

}
