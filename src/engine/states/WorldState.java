
package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import worlds.planet.TestWorld;

/**
 *
 * @author Richard
 */
public class WorldState extends AbstractAppState {
  
  private final TestWorld world;
  private boolean isWorldPaused;
  
  public WorldState() {
    world = new TestWorld();
    isWorldPaused = true;
  }
  
  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    world.setIsPaused(isWorldPaused);
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

  public void setIsPaused() {
    world.setIsPaused(isWorldPaused = !isWorldPaused);
  }

  boolean isPaused() {
    return isWorldPaused;
  }
  
}
