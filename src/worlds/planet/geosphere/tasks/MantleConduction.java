
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import engine.util.Delay;
import engine.util.task.Task;
import worlds.planet.PlanetCell;
import worlds.planet.Util;
import worlds.planet.geosphere.GeoCell;
import worlds.planet.geosphere.Geosphere;

/**
 *
 * @author Richard
 */
public class MantleConduction extends Task {

  private final Geosphere surface;
  private final Delay delay;
  
  public MantleConduction(Geosphere surface) {
    this.surface = surface;
    delay = new Delay(100);
  }
  
  @Override
  public void construct() {}

  @Override
  public boolean check() throws Exception {
    return delay.check();
  }

  @Override
  public void before() throws Exception {
  }

  @Override
  public void perform(int x, int y) throws Exception {
    GeoCell cell = surface.getCellAt(x, y);
    Vector2f[] cells = Util.getCellIndexesFrom(cell.getGridPosition(), surface.getTerrainSize());
    float temp = cell.getMantleTemperature(); 
    for (Vector2f pos : cells) {
      GeoCell neighborCell = surface.getCellAt(pos);
      float cellTemp = neighborCell.getMantleTemperature();
      float tempChange = (PlanetCell.length * (temp - cellTemp)) / (1e7f);
      cell.addToMantleHeat(-tempChange);
    }
  }

  @Override
  public void after() throws Exception {
  }
  
}
