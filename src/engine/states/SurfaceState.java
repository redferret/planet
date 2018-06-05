
package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import engine.PlanetApp;
import engine.surface.SurfaceMap;
import worlds.planet.TestWorld;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.Core;
import worlds.planet.geosphere.LowerMantle;
import worlds.planet.geosphere.UpperMantle;
import static worlds.planet.Util.heatMap;

/**
 *
 * @author Richard
 */
public class SurfaceState extends AbstractAppState {
  
  public static final String LITHOSPHERE = "lithosphere", UPPER_MANTLE = "upperMantle",
          LOWER_MANTLE = "lowerMantle", CORE = "core";
  
  private PlanetApp app;
  private Lithosphere lithosphere;
  private UpperMantle upperMantle;
  private LowerMantle lowerMantle;
  private Core core;
  private boolean wireframe = false;
  private Material unshadedMat;
  private String attachedSurface;
  
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    unshadedMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
    unshadedMat.setBoolean("VertexColor", true);
    unshadedMat.getAdditionalRenderState().setWireframe(wireframe);
    this.app = (PlanetApp) app;
    AppStateManager asm = this.app.getStateManager();
    WorldState worldState = asm.getState(WorldState.class);
    TestWorld world = worldState.getWorld();
    attachedSurface = "";
    lithosphere = world.getLithosphere();
    upperMantle = world.getUpperMantle();
    lowerMantle = world.getLowerMantle();
    core = world.getCore();
    
    renderSurface(CORE);
  }
  
  public void bindTerrain(SurfaceMap terrain) {
    terrain.bindCameraForLODControl(app.getCamera());
    terrain.bindTerrainToNode(((PlanetApp) app).getRootNode());
    terrain.setMaterial(unshadedMat);
  }
  
  public boolean isWireFramed() {
    return wireframe;
  }
  
  public void negWireFramed() {
    wireframe = !wireframe;
    switch(attachedSurface) {
      case LITHOSPHERE:
        lithosphere.getMaterial().getAdditionalRenderState().setWireframe(wireframe);
        return;
      case UPPER_MANTLE:
        upperMantle.getMaterial().getAdditionalRenderState().setWireframe(wireframe);
        return;
      case LOWER_MANTLE:
        lowerMantle.getMaterial().getAdditionalRenderState().setWireframe(wireframe);
        return;
      case CORE:
        core.getMaterial().getAdditionalRenderState().setWireframe(wireframe);
    }
  }
  
  public void renderSurface(String surface) {
    detachCurrentTerrain();
    attachedSurface = surface;
    switch(surface) {
      case LITHOSPHERE:
        bindTerrain(lithosphere);
        return;
      case UPPER_MANTLE:
        bindTerrain(upperMantle);
        return;
      case LOWER_MANTLE:
        bindTerrain(lowerMantle);
        return;
      case CORE:
        bindTerrain(core);
        return;
      default:
        throw new IllegalArgumentException("Surface (" + surface + ") does not exist");
    }
  }
  
  public void detachCurrentTerrain() {
    switch(attachedSurface) {
      case LITHOSPHERE:
        lithosphere.removeFromParent();
        lithosphere.clearCameraControl();
        return;
      case UPPER_MANTLE:
        upperMantle.removeFromParent();
        upperMantle.clearCameraControl();
        return;
      case LOWER_MANTLE:
        lowerMantle.removeFromParent();
        lowerMantle.clearCameraControl();
        return;
      case CORE:
        app.getRootNode().detachChild(core);
        core.clearCameraControl();
    }
  }
  
  @Override
  public void update(float timeSlice) {
    if (!app.getStateManager().getState(WorldState.class).isPaused()) {
      switch(attachedSurface) {
      case LITHOSPHERE:
        showTemperature(lithosphere);
        return;
      case UPPER_MANTLE:
        showTemperature(upperMantle);
        return;
      case LOWER_MANTLE:
        showTemperature(lowerMantle);
        return;
      case CORE:
        showTemperature(core);
      }
    }
  }
  
  public void showTemperature(SurfaceMap map) {
    map.updateTerrainHeight(0.01f, (cell) -> {
      return cell.getTemperature();
    });

    map.updateVertexColors(heatMap, (heightVal) -> {
      return (int) (heightVal < 0 ? 0
              : (heightVal > heatMap.length - 1 ? heatMap.length - 1
                      : heightVal));
    });
  }

  @Override
  public void cleanup() {
    super.cleanup();
  }
  
}
