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
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import engine.util.Delay;
import worlds.planet.TestWorld;

/**
 * Main entry point
 */
public class App extends SimpleApplication {

  private static TestWorld world;
  private Material terrainMaterial;
  private Material wireFrameMat;
  private boolean wireframe = false;
  private final Delay delay = new Delay(500);
  
  public static void main(String[] args) {
    world = new TestWorld();
    App app = new App();
    app.showSettings = false;
    app.settings = new AppSettings(true);
    app.settings.setTitle("Planet");
    app.start();
  }

  @Override
  public void simpleInitApp() {
    flyCam.setMoveSpeed(300f);
    flyCam.setZoomSpeed(25f);

    inputManager.setCursorVisible(true);

    world.getSurface().bindCameraForLODControl(getCamera());
    terrainMaterial = new Material(assetManager,
            "Common/MatDefs/Terrain/TerrainLighting.j3md");
    Texture def = assetManager.loadTexture("Textures/lava2.jpg");
    def.setWrap(WrapMode.Repeat);

    terrainMaterial.setBoolean("useTriPlanarMapping", false);
    terrainMaterial.setBoolean("WardIso", true);
    terrainMaterial.setFloat("Shininess", 0);
    terrainMaterial.setTexture("DiffuseMap", def);
    terrainMaterial.setFloat("DiffuseMap_0_scale", 64f);

    // WIREFRAME material
    wireFrameMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
    wireFrameMat.getAdditionalRenderState().setWireframe(true);
    wireFrameMat.setColor("Color", ColorRGBA.Green);

    world.getSurface().bindMaterial(terrainMaterial);
    world.getSurface().bindTerrain(rootNode);

    DirectionalLight light = new DirectionalLight();
    light.setDirection((new Vector3f(-0.5f, -1f, -0.5f)).normalize());
    rootNode.addLight(light);

    inputManager.addMapping("wireframe", new KeyTrigger(KeyInput.KEY_T));
    inputManager.addListener(actionListener, "wireframe");
    world.getSurface().getCellAt(10, 10).addToMantleHeat(4000);
    world.play();
  }
  private final ActionListener actionListener = new ActionListener() {

    @Override
    public void onAction(String name, boolean pressed, float tpf) {
      if (name.equals("wireframe") && !pressed) {
        wireframe = !wireframe;
        if (wireframe) {
          world.getSurface().bindMaterial(wireFrameMat);
        } else {
          world.getSurface().bindMaterial(terrainMaterial);
        }
      }
    }
  };

  @Override
  public void simpleUpdate(float tpf) {
    if (delay.check())
    world.getSurface().updateHeightMap();
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
