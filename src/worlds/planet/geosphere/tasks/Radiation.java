
package worlds.planet.geosphere.tasks;

import engine.surface.Cell;
import engine.util.Delay;
import engine.util.task.Task;
import worlds.planet.Util;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;

/**
 *
 * @author Richard
 */
public abstract class Radiation extends Task {
  
  protected final Lithosphere geosphere;
  protected final Delay delay;
  
  
  public Radiation(Lithosphere geosphere) {
    this.geosphere = geosphere;
    delay = new Delay(100);
  }
  
  @Override
  public void construct() {
  }

  @Override
  public boolean check() {
    return delay.check();
  }
  
  public void updateRadiation(Cell cell, float mass, float specificHeat, 
          TemperatureData data, TempSettingCallBack setting) {
    float heatFromMantle = Util.calcHeatRadiation(data.getTemperature(cell));
    float denom = (mass * specificHeat);
    denom = denom == 0 ? 1 : denom;
    float tempChangeToMantle = heatFromMantle / denom;
    setting.temperatureFlux(-tempChangeToMantle * 0.1f);
  }
  
}
