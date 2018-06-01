package worlds.planet.geosphere;

import com.jme3.math.Vector2f;
import engine.util.Vec2;
import engine.util.task.Task;
import java.util.ArrayList;
import java.util.List;
import worlds.planet.PlanetCell;
import worlds.planet.Surface;
import worlds.planet.Util;
import worlds.planet.geosphere.tasks.MantleHeatUpdate;
import worlds.planet.geosphere.tasks.MantleConduction;
import worlds.planet.geosphere.tasks.MantleRadiation;
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
      return new MantleRadiation(this);
    });
    produceTasks(() -> {
      return new MantleConduction(this);
    });
    produceTasks(() -> {
      return new MantleHeatUpdate(this);
    });
  }
  
  /**
   * Update the height data of the height map for rendering.
   */
  public void updateHeightMap() {
    List<Vector2f> locs = new ArrayList<>();
    List<Float> heights = new ArrayList<>();
    map.values().forEach(cell -> {
      float height = cell.getMantleTemperature() * 0.01f;
      Vec2 pos = cell.getGridPosition();
      locs.add(Util.scalePositionForTerrain(pos.getX(), pos.getY(), size + 1));
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
