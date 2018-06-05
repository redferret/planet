
package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import engine.PlanetApp;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.TestWorld;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;
import static worlds.planet.Util.heatMap;
/**
 *
 * @author Richard
 */
public class SurfaceState extends AbstractAppState {
  
  private PlanetApp app;
  private Lithosphere surface;
  private boolean wireframe = false;
  private Material unshadedMat;
  
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
    surface = world.getGeosphere();
    surface.bindCameraForLODControl(app.getCamera());
    surface.bindTerrainToNode(((PlanetApp) app).getRootNode());
    surface.setMaterial(unshadedMat);
  }
  
  public boolean isWireFramed() {
    return wireframe;
  }
  
  public void negWireFramed() {
    wireframe = !wireframe;
    surface.getMaterial().getAdditionalRenderState().setWireframe(wireframe);
  }
  
  @Override
  public void update(float timeSlice) {
    if (!app.getStateManager().getState(WorldState.class).isPaused()) {
      surface.updateTerrainHeight(0.01f, (cell) -> {
        return 2000;
      });

      surface.updateVertexColors(heatMap, (heightVal) -> {
        return (int) (heightVal < -2.73f ? 0
                : (heightVal > heatMap.length - 1 ? heatMap.length - 1
                        : heightVal));
      });
    }
  }

  @Override
  public void cleanup() {
    super.cleanup();
  }
  
}
