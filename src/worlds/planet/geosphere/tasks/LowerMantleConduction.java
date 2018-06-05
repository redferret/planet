
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import engine.surface.Cell;
import worlds.planet.PlanetCell;
import worlds.planet.Util;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.LowerMantle;
import worlds.planet.geosphere.UpperMantle;

/**
 *
 * @author Richard
 */
public class LowerMantleConduction extends Conduction {

  public LowerMantleConduction(Lithosphere surface) {
    super(surface);
  }

  @Override
  public void before() throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public void perform(int x, int y) throws Exception {
    LowerMantle lowerMantle = surface.getCellAt(x, y);
    
    Vector2f[] cells = Util.getCellIndexesFrom(lowerMantle.getGridPosition(), surface.getTerrainSize());
    for (Vector2f pos : cells) {
      LowerMantle neighborCell = surface.getCellAt(pos);
      float neighborCellTemp = neighborCell.getLowerMantleTemperature();
      float curCellTemp = lowerMantle.getLowerMantleTemperature();
      float joules = getJoules(neighborCellTemp, curCellTemp, LowerMantle.LOWER_MANTLE_MASS, LowerMantle.LOWER_MANTLE_SPECIFIC_HEAT);
      float tempChange = getTemperatureChange(joules, PlanetCell.area, LowerMantle.LOWER_MANTLE_SPECIFIC_HEAT, PlanetCell.length);
      neighborCell.addToLowerMantleHeat(tempChange);
      lowerMantle.addToLowerMantleHeat(-tempChange);
    }
    
//    UpperMantle upperMantle = surface.getCellAt(x, y);
//    upperMantle.addToUpperMantleHeat(tempToUpper);
//    lowerMantle.addToLowerMantleHeat(-tempToUpper);
  }

  @Override
  public void after() throws Exception {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }
  
}
