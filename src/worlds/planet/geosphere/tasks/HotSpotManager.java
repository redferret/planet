
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import engine.util.Delay;
import engine.util.task.BasicTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.geosphere.HotSpot;
import worlds.planet.geosphere.LowerMantle;
import worlds.planet.geosphere.Mantle;

/**
 *
 * @author Richard
 */
public class HotSpotManager extends BasicTask {

  
  private final LowerMantle surface;
  private static final ThreadLocalRandom LOCAL_RAND = ThreadLocalRandom.current();
  private final List<HotSpot> hotSpots;
  public static int maxHotSpots = 3000;
  private final Delay delay;

  public HotSpotManager(LowerMantle surface) {
    hotSpots = new ArrayList<>();
    this.surface = surface;
    delay = new Delay(500);
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
  public void perform() throws Exception {
    if (delay.check()) {
      int cellCount = (int) (surface.getTotalNumberOfCells());
      List<Integer> indexes = new ArrayList<>();
      for (int c = 0; c < cellCount; c++) {
        indexes.add(LOCAL_RAND.nextInt(surface.getTotalNumberOfCells()));
      }
      indexes.forEach(index -> {
        int x = surface.calcX(index);
        int y = surface.calcY(index);
        Mantle cell = surface.getCellAt(x, y);
        Vector2f[] pos = new Vector2f[]{
            new Vector2f(x + 1, y),
            new Vector2f(x - 1, y),
            new Vector2f(x, y + 1),
            new Vector2f(x, y - 1)
        };
        float prob = getProb(cell.getTemperature());
        if (LOCAL_RAND.nextFloat() < prob) {
          cell.addToTemperature(15f * 0.8f);
          for(Vector2f p : pos) {
            surface.getCellAt(p).addToTemperature(12.5f * 0.8f);
          }
        }
      });
    }
  }
  
}
