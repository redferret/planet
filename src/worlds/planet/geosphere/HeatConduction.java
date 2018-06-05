
package worlds.planet.geosphere;

import engine.util.concurrent.AtomicFloat;
import static java.lang.Float.NaN;

/**
 *
 * @author Richard
 */
public abstract class HeatConduction {
  
  private final AtomicFloat temperature;
  private float newTemperature;

  public HeatConduction(float initialTemp) {
    this.temperature = new AtomicFloat(initialTemp);
    newTemperature = NaN;
  }
  
  /**
   * The heat capacity of the cell
   * @return 
   */
  public abstract float getHeatCapacity();
  
  /**
   * How much resistence is applied horizontally
   * @return 
   */
  public abstract float getHorizontalResistence();
  
  /**
   * How much resistence is applied vertically
   * @return 
   */
  public abstract float getVerticalResistence();
  
  /**
   * If there is no top cell for heat to conduct, then use this value.
   * @return 
   */
  public abstract float topNullConductance();
  
  /**
   * If there is no bottom cell for heat to conduct, then use this value.
   * @return 
   */
  public abstract float bottomNullConductance();
  
  public void setNewTemperature(float newTemperature) {
    this.newTemperature = newTemperature;
  }
  
  public void updateTemperature() {
    if (!Float.isNaN(newTemperature)) {
      temperature.set(newTemperature);
      newTemperature = NaN;
    }
  }

  public void addToTemperature(float amount) {
    float temp = temperature.get() + amount;
    if (temp < -273) {
      temperature.getAndSet(-273);
    } else {
      temperature.getAndSet(temp);
    }
  }
  
  public float getTemperature() {
    return temperature.get();
  }
}
