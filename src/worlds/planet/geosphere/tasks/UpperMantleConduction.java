
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import worlds.planet.PlanetCell;
import worlds.planet.Surface;
import worlds.planet.Util;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.LowerMantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class UpperMantleConduction extends Conduction {

  public UpperMantleConduction(Surface surface) {
    super(surface);
  }
  
  @Override
  public void before() throws Exception {
  }

  @Override
  public void perform(int x, int y) throws Exception {
    UpperMantle upperMantle = surface.getCellAt(x, y);
    
    Vector2f[] cells = Util.getCellIndexesFrom(upperMantle.getGridPosition(), surface.getTerrainSize());
    float totalTemperatureChange = 0;
    float curCellTemp = upperMantle.getUpperMantleTemperature();
    
    for (Vector2f pos : cells) {
      UpperMantle neighborCell = surface.getCellAt(pos);
      float neighborCellTemp = neighborCell.getUpperMantleTemperature();
      totalTemperatureChange += (curCellTemp - neighborCellTemp);
    }
    totalTemperatureChange /= 8f;
    
    for (Vector2f pos : cells) {
      UpperMantle neighborCell = surface.getCellAt(pos);
      float neighborCellTemp = neighborCell.getUpperMantleTemperature();
      float joules = getJoules(neighborCellTemp, curCellTemp, UpperMantle.UPPER_MANTLE_MASS, UpperMantle.UPPER_MANTLE_SPECIFIC_HEAT);
      float tempChange = getTemperatureChange(joules, PlanetCell.area, UpperMantle.UPPER_MANTLE_SPECIFIC_HEAT, PlanetCell.length);
      neighborCell.addToUpperMantleHeat(tempChange);
      totalTemperatureChange += tempChange;
    }
    upperMantle.addToUpperMantleHeat(-totalTemperatureChange);
//    Crust crust = surface.getCellAt(x, y);
//    crust.addCrustHeat(tempToCrust);
//    upperMantle.addToUpperMantleHeat(-tempToCrust);
  }

  @Override
  public void after() throws Exception {
  }
  
}
