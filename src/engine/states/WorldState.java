
package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import engine.PlanetApp;
import worlds.planet.TestWorld;

/**
 *
 * @author Richard
 */
public class WorldState extends AbstractAppState {
  
  private TestWorld world;
  
  public WorldState() {
    world = new TestWorld();
  }
  
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    world.play();
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
    world.shutdown();
  }
  
}
