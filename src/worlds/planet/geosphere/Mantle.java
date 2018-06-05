
package worlds.planet.geosphere;

import engine.surface.Cell;
import engine.util.concurrent.AtomicFloat;

/**
 *
 * @author Richard
 */
public abstract class Mantle extends Cell {
  
  private final AtomicFloat temperature;
  
  public Mantle(int x, int y, float initialTemperature) {
    super(x, y);
    temperature = new AtomicFloat(initialTemperature);
  }
  
  public void addToTemperature(float amount) {
    float temp = temperature.get() + amount;
    if (temp < -273) {
      temperature.getAndSet(-273);
    } else {
      temperature.getAndSet(temp);
    }
  }
  @Override
  public void addToTemperatureFlux(float flux) {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  @Override
  public float getTemperature() {
    return temperature.get();
  }
  
}
