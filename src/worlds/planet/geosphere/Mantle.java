package worlds.planet.geosphere;

import java.awt.Color;
import java.util.List;
import engine.surface.Cell;
import engine.util.concurrent.AtomicFloat;
import java.util.concurrent.ThreadLocalRandom;
import worlds.planet.Util;

/**
 * The mantle is below the crust, heat and magma that build up to critical
 * points below the crust cause volcanoes to erupt and aid in the movement 
 * of plate tectonics.
 *
 * @author Richard DeSilvey
 */
public class Mantle extends Cell {

  public static float[][] heatMap;
  public static float[] heatMapFl;

  private final AtomicFloat temperature;
  
  /**
   * The average density of the mantel. The units are in kilograms per cubic
   * meter.
   */
  public static float mantle_density = 3500f;

  static {
    Color[] colors = {new Color(0, 0, 0), new Color(255, 0, 0), new Color(250, 250, 0)};
    heatMap = Util.constructSamples(colors, 50);
  }

  public Mantle(int x, int y) {
    super(x, y);
    temperature = new AtomicFloat(ThreadLocalRandom.current().nextInt(2500, 4000));
  }

  
  /**
   * A cell is less dense if it's hotter. This will be a percentage of the 
   * actual density of the material for this cell. 
   * @return A value between 1 and 0. Value of 1 is a temperature of zero.
   */
  public float mantleDensityFactor() {
    float temp = temperature.get();
    return -0.00000006f * (temp * temp) + 1f;
  }
  
  public void addToMantleHeat(float amount) {
    float temp = temperature.get() + amount;
    if (temp > 4000) {
      temperature.getAndSet(4000);
    } else if (temp < -273) {
      temperature.getAndSet(-273);
    } else {
      temperature.getAndSet(temp);
    }
  }

  public float getMantleTemperature() {
    return temperature.get();
  }

//  @Override
//  public List<Integer[]> render(List<Integer[]> settings) {
//
//    int index = (int) (temperature.get() / 12);
//    index = index >= heatMap.length - 1 ? heatMap.length - 1 : index < 0 ? 0 : index;
//    settings.add(heatMap[index]);
//
//    return settings;
//  }

}
