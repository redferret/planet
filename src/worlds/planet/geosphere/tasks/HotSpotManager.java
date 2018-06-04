
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import engine.util.Delay;
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
public class HotSpotManager extends Task {

  
  private final Geosphere surface;
//  private final int totalSize;
  private static final ThreadLocalRandom LOCAL_RAND = ThreadLocalRandom.current();
  private final List<HotSpot> hotSpots;
  public static int maxHotSpots = 3000;
  private Delay delay;

  public HotSpotManager(Geosphere surface) {
//    totalSize = getThread().getManager().getBounds().getUpperXBound();
    hotSpots = new ArrayList<>();
    this.surface = surface;
    delay = new Delay(200);
  }
  
  private float getProb(float temp) {
    temp = temp > 4000 ? 4000 : (temp < 0 ? 0 : temp);
    float sqr = (temp - 4000);
    return 1e-10f * sqr * sqr;
  }
  
  @Override
  public void before() throws Exception {
  }

  @Override
  public void after() throws Exception {
    // Update each hotspot
    hotSpots.forEach(hotSpot -> {
      GeoCell cell = surface.getCellAt(hotSpot.getPosition());
      cell.addToMantleHeat(12.0f);
    });
    
  }

  @Override
  public void construct() {}

  @Override
  public boolean check() throws Exception {
    return delay.check();
  }

  @Override
  public void perform(int x, int y) throws Exception {
    GeoCell cell = surface.getCellAt(x, y);
    Vector2f[] pos = new Vector2f[]{
        new Vector2f(x + 1, y),
        new Vector2f(x - 1, y),
        new Vector2f(x, y + 1),
        new Vector2f(x, y - 1)
    };
    
    float mantleTemp = cell.getMantleTemperature();
    float prob = getProb(mantleTemp);
    if ((LOCAL_RAND.nextFloat() < prob) && (hotSpots.size() < maxHotSpots)) {
      cell.addToMantleHeat(200.0f);
      for(Vector2f p : pos) {
        surface.getCellAt(p).addToMantleHeat(150f);
      }
//      hotSpots.add(new HotSpot(new Vector2f(x, y), 0));
    }
  }
  
}
