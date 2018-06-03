
package worlds.planet.geosphere.tasks;

import engine.util.task.BasicTask;
import java.util.concurrent.ThreadLocalRandom;

/**
 *
 * @author Richard
 */
public class HotSpotManager extends BasicTask {

  private final int totalSize;

  public HotSpotManager() {
    totalSize = getThread().getManager().getBounds().getUpperXBound();
  }
  
  @Override
  public void before() throws Exception {
  }

  @Override
  public void perform() throws Exception {
    int randIndex = ThreadLocalRandom.current().nextInt(totalSize);
    
  }

  @Override
  public void after() throws Exception {
  }
  
}
