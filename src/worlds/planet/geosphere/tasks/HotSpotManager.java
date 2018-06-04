
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import engine.util.Delay;
import engine.util.task.BasicTask;
import engine.util.task.Task;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.geosphere.GeoCell;
import worlds.planet.geosphere.Geosphere;
import worlds.planet.geosphere.HotSpot;

/**
 *
 * @author Richard
 */
public class HotSpotManager extends BasicTask {

  
  private final Geosphere surface;
  private static final ThreadLocalRandom LOCAL_RAND = ThreadLocalRandom.current();
  private final List<HotSpot> hotSpots;
  public static int maxHotSpots = 3000;
  private final Delay delay;

  public HotSpotManager(Geosphere surface) {
    hotSpots = new ArrayList<>();
    this.surface = surface;
    delay = new Delay(1500);
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
        GeoCell cell = surface.getCellAt(x, y);
        Vector2f[] pos = new Vector2f[]{
            new Vector2f(x + 1, y),
            new Vector2f(x - 1, y),
            new Vector2f(x, y + 1),
            new Vector2f(x, y - 1)
        };
        float prob = getProb(cell.getMantleTemperature());
        if (LOCAL_RAND.nextFloat() < prob) {
          cell.addToMantleHeat(15f * 0.8f);
          for(Vector2f p : pos) {
            surface.getCellAt(p).addToMantleHeat(12.5f * 0.8f);
          }
        }
      });
    }
  }
  
}
