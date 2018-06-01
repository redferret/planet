package worlds.planet.geosphere;

import com.jme3.math.Vector2f;
import engine.util.Vec2;
import java.util.ArrayList;
import java.util.List;
import worlds.planet.Surface;
import worlds.planet.geosphere.tasks.MantleHeatLoss;
/**
 * Contains all logic that works on the geology of the planet.
 *
 * @author Richard DeSilvey
 */
public abstract class Geosphere extends Surface {

  private long ageStamp;
  
  public Geosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
    super(worldSize, surfaceDelay, threadsDelay, threadCount);
    ageStamp = 0;
    produceTasks(() -> {
      return new MantleHeatLoss(this);
    });
  }
  
  /**
   * Update the height data of the height map for rendering.
   */
  public void updateHeightMap() {
    List<Vector2f> locs = new ArrayList<>();
    List<Float> heights = new ArrayList<>();
    map.values().forEach(cell -> {
      float height = cell.getHeightWithoutOceans();
      Vec2 pos = cell.getGridPosition();
      locs.add(new Vector2f((pos.getX() * 2) - size, (pos.getY() * 2) - size));
      heights.add(height);
    });
    terrain.setHeight(locs, heights);
  }

  public long getAgeStamp() {
    return ageStamp;
  }

  public void setAgeStamp(long ageStamp) {
    this.ageStamp = ageStamp;
  }
  
}
