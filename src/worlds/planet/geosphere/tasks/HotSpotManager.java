
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import engine.surface.Cell;
import engine.util.Delay;
import engine.util.task.BasicTask;
import engine.util.task.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.geosphere.Core;
import worlds.planet.geosphere.HotSpot;
import worlds.planet.geosphere.Mantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class HotSpotManager extends Task {

  
  private final Core surface;
  private static final ThreadLocalRandom LOCAL_RAND = ThreadLocalRandom.current();
  private final List<HotSpot> hotSpots;
  public static int maxHotSpots = 3000;
  private final Delay delay;
  private float prob;

  public HotSpotManager(Core surface) {
    hotSpots = new ArrayList<>();
    this.surface = surface;
    delay = new Delay(1);
    prob = 1f / (surface.getTotalNumberOfCells() * 0.5f);
  }
  
  private float getProb(float temp) {
    temp = temp > 4000 ? 4000 : (temp < 0 ? 0 : temp);
    float sqr = (temp - 4000);
    return 1e-9f * sqr * sqr;
  }
  
  @Override
  public void before() throws Exception {
  }

  @Override
  public void after() throws Exception {
    // Update each hotspot
    hotSpots.forEach(hotSpot -> {
    });
    
  }

  @Override
  public void construct() {}
  
  @Override
  public void perform(int x, int y) throws Exception {
    Cell cell = surface.getCellAt(x, y);
    cell.addToTemperature(2.5f);
    if (LOCAL_RAND.nextFloat() < prob) {
      Vector2f[] pos = new Vector2f[]{
        new Vector2f(x + 1, y),
        new Vector2f(x - 1, y),
        new Vector2f(x, y + 1),
        new Vector2f(x, y - 1)
      };
      cell.addToTemperature(250f * 5f);
      for(Vector2f p : pos) {
        surface.getCellAt(p).addToTemperature(125f * 5f);
      }
    }
  }

  @Override
  public boolean check() throws Exception {
    return delay.check();
  }
  
}
