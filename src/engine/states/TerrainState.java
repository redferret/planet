
package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import worlds.planet.TestWorld;

/**
 *
 * @author Richard
 */
public class TerrainState extends AbstractAppState {
  
  private TestWorld world;
  
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    world = new TestWorld();
  }
  
  public TestWorld getWorld() {
    return world;
  }
  
  @Override
  public void update(float tpf) {
    //TODO: implement behavior during runtime
  }
  
  @Override
  public void cleanup() {
    super.cleanup();
    //TODO: clean up what you initialized in the initialize method,
    //e.g. remove all spatials from rootNode
    //this is called on the OpenGL thread after the AppState has been detached
  }
  
}
