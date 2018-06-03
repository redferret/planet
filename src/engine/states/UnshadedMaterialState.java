
package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import engine.PlanetApp;

/**
 *
 * @author Richard
 */
public class UnshadedMaterialState extends AbstractAppState {
  
  private PlanetApp app;
  private boolean wireframe = false;
  private Material wireFrameMat;
  
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    wireFrameMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
    wireFrameMat.setBoolean("VertexColor", true);
    wireFrameMat.getAdditionalRenderState().setWireframe(wireframe);
    this.app = (PlanetApp) app;
    this.app.getWorld().getSurface().setMaterial(wireFrameMat);
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
      this.app.getWorld().getSurface().setMaterial(wireFrameMat);
    }
  }
  
  @Override
  public void update(float timeSlice) {
    app.getWorld().getSurface().getMaterial().getAdditionalRenderState().setWireframe(wireframe);
    
  }
  
  @Override
  public void cleanup() {
    super.cleanup();
  }
  
}
