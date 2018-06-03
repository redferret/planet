package engine;

import com.jme3.app.SimpleApplication;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.renderer.RenderManager;
import com.jme3.system.AppSettings;
import engine.util.Delay;
import worlds.planet.TestWorld;

/**
 * Main entry point
 */
public class App extends SimpleApplication {

  private static TestWorld world;
  private boolean wireframe = true;
  private final Delay delay = new Delay(500);
  
  public static void main(String[] args) {
    world = new TestWorld();
    App app = new App();
    
    app.showSettings = false;
    app.settings = new AppSettings(true);
//    app.settings.setFullscreen(true);
    app.settings.setTitle("Planet");
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(400f);
    flyCam.setZoomSpeed(20f);
    
    setDisplayStatView(false);
    inputManager.setCursorVisible(true);
    
    
    Material basicMaterial = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    basicMaterial.setBoolean("VertexColor", true);
//    basicMaterial.setColor("Color", ColorRGBA.Blue);
    basicMaterial.getAdditionalRenderState().setWireframe(wireframe);
    world.getSurface().setMaterial(basicMaterial);
    world.getSurface().bindCameraForLODControl(getCamera());
    world.getSurface().bindTerrainToNode(rootNode);
    
    DirectionalLight light = new DirectionalLight();
    light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
    rootNode.addLight(light);

    inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
    inputManager.addListener(actionListener, "wireframe");
    
    world.play();
  }
  private final ActionListener actionListener = new ActionListener() {

    @Override
    public void onAction(String name, boolean pressed, float tpf) {
      if (name.equals("wireframe") && !pressed) {
        wireframe = !wireframe;
        world.getSurface().getMaterial()
                .getAdditionalRenderState().setWireframe(wireframe);
      }
    }
  };

  @Override
  public void simpleUpdate(float tpf) {
    if (delay.check()) {
      world.getSurface().updateTerrainForMantleTemp(0.01f);
      world.getSurface().updateVertexColors();
    }
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
