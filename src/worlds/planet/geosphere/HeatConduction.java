
package worlds.planet.geosphere;

import engine.util.concurrent.AtomicFloat;
import static java.lang.Float.NaN;

/**
 * Each cell will have the ability to conduct heat
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
  public abstract float getBottomResistence();
  
  public abstract float getTopResistence();
  
  public abstract float getZLength();
  
  public float getTopNullTemperature() {
    return -273f;
  }
  
  public float getBottomNullTemperature() {
    return -273f;
  }
  
  /**
   * If there is no top cell for heat to conduct, then use this value.
   * @return 
   */
  public float topNullConductance() {
    return 0;
  }
  
  /**
   * If there is no bottom cell for heat to conduct, then use this value.
   * @return 
   */
  public float bottomNullConductance() {
    return 0;
  }
  
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
