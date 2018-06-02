
package worlds.planet.geosphere.tasks;

import engine.util.Delay;
import engine.util.Vec2;
import engine.util.task.Task;
import worlds.planet.PlanetCell;
import worlds.planet.Util;
import worlds.planet.geosphere.GeoCell;
import worlds.planet.geosphere.Geosphere;
import static worlds.planet.geosphere.tasks.MantleHeatUpdate.delayHeatUpdate;

/**
 *
 * @author Richard
 */
public class MantleConduction extends Task {

  private final Geosphere surface;
  private final Delay delay;
  
  /**
   * A simple task that allows heat to radiate out vertically. This cooling
   * is based off of inferred flux. The hotter the cell is the more it will
   * release that heat. The rate that this heat is released can trigger
   * volcanoes. 
   */
  public static float mantleSpecificHeat;
  
  static {
    mantleSpecificHeat = 1.9f;
  }
  
  public MantleConduction(Geosphere surface) {
    this.surface = surface;
    delay = new Delay(delayHeatUpdate);
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
    Vec2[] cells = Util.getCellIndexesFrom(cell.getGridPosition(), surface.getSize());
    float temp = cell.getMantleTemperature();
    
    for (Vec2 pos : cells) {
      GeoCell neighborCell = surface.getCellAt(pos);
      float cellTemp = neighborCell.getMantleTemperature();
      float Q = mantleSpecificHeat * (temp - cellTemp);
      float tempChange = Q / (mantleSpecificHeat * 1e6f);
      cell.applyTemperatureAcc(tempChange);
    }
  }

  @Override
  public void after() throws Exception {
  }
  
}
