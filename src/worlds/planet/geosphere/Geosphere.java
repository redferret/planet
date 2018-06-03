package worlds.planet.geosphere;

import worlds.planet.Surface;
import worlds.planet.geosphere.tasks.MantleConduction;
import worlds.planet.geosphere.tasks.MantleRadiation;
/**
 * Contains all logic that works on the geology of the planet.
 *
 * @author Richard DeSilvey
 */
public abstract class Geosphere extends Surface {

  private long ageStamp;
  
  public Geosphere(int totalSize, int threadsDelay, int threadCount) {
    super(totalSize, threadsDelay, threadCount);
    ageStamp = 0;
    produceTasks(() -> {
      return new MantleRadiation(this);
    });
    produceTasks(() -> {
      return new MantleConduction(this);
    });
  }
  
  public long getAgeStamp() {
    return ageStamp;
  }

  public void setAgeStamp(long ageStamp) {
    this.ageStamp = ageStamp;
  }
  
}
