
package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import com.jme3.terrain.geomipmap.TerrainQuad;
import engine.PlanetApp;

/**
 *
 * @author Richard
 */
public class TerrrainUnshadedMaterialState extends AbstractAppState {
  
  private PlanetApp app;
  private TerrainQuad terrain;
  private boolean wireframe = false;
  private Material wireFrameMat;
  
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    wireFrameMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
    wireFrameMat.setBoolean("VertexColor", true);
    wireFrameMat.getAdditionalRenderState().setWireframe(wireframe);
    this.app = (PlanetApp) app;
    terrain = this.app.getStateManager().getState(TerrainState.class).getWorld().getSurface();
    terrain.setMaterial(wireFrameMat);
  }
  
  public boolean isWireFramed() {
    return wireframe;
  }
  
  public void negWireFramed() {
    wireframe = !wireframe;
  }
  
  @Override
  public void setEnabled(boolean enabled) {
    super.setEnabled(enabled);
    if (isEnabled()) {
      terrain.setMaterial(wireFrameMat);
    }
  }
  
  @Override
  public void update(float timeSlice) {
    terrain.getMaterial().getAdditionalRenderState().setWireframe(wireframe);
  }
  
  @Override
  public void cleanup() {
    super.cleanup();
  }
  
}
