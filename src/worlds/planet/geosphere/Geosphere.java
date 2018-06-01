package worlds.planet.geosphere;

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

  public long getAgeStamp() {
    return ageStamp;
  }

  public void setAgeStamp(long ageStamp) {
    this.ageStamp = ageStamp;
  }
  
}
