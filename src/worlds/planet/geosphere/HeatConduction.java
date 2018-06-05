
package worlds.planet.geosphere;

import engine.util.concurrent.AtomicFloat;

/**
 *
 * @author Richard
 */
public abstract class HeatConduction {
  
  private final AtomicFloat temperature;
  private float newTemperature;

  public HeatConduction(float initialTemp) {
    this.temperature = new AtomicFloat(initialTemp);
    newTemperature = 0;
  }
  
  public abstract float getHeatConductivity();
  
  public void setNewTemperature(float newTemperature) {
    this.newTemperature = newTemperature;
  }
  
  public void updateTemperature() {
    this.addToTemperature(newTemperature);
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
