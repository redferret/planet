package worlds.planet.geosphere;

import java.awt.Color;
import java.util.List;
import engine.surface.Cell;
import worlds.planet.Util;

/**
 * The mantle is below the crust, heat and magma that build up to critical
 * points below the crust cause volcanoes to erupt and aid in the movement 
 * of plate tectonics.
 *
 * @author Richard DeSilvey
 */
public class Mantle extends Cell {

  private static Integer[][] heatMap;

  private float temperature;
  
  /**
   * The average density of the mantel. The units are in kilograms per cubic
   * meter.
   */
  public static float mantle_density = 3500f;

  static {
    Color[] colors = {new Color(95, 0, 15), new Color(255, 45, 45), new Color(250, 250, 0)};
    heatMap = Util.constructSamples(colors, 100);
  }

  public Mantle(int x, int y) {
    super(x, y);
    temperature = 1200;
  }

  /**
   * A cell is less dense if it's hotter. This will be a percentage of the 
   * actual density of the material for this cell. 
   * @return A value between 1 and 0. Value of 1 is a temperature of zero.
   */
  public float mantleDensityFactor() {
    return -0.00000006f * (temperature * temperature) + 1f;
  }
  
  public void addToMantleHeat(float amount) {
    temperature += amount;
    if (temperature > 4000) {
      temperature = 4000;
    } else if (temperature < -273) {
      temperature = -273;
    }
  }

  public void cool(float amount) {
    temperature -= amount;
    if (temperature < 0) {
      temperature = 0;
    }
  }

  public float getMantleTemperature() {
    return temperature;
  }

  @Override
  public List<Integer[]> render(List<Integer[]> settings) {

    int index = (int) (temperature / 12);
    index = index >= heatMap.length - 1 ? heatMap.length - 1 : index < 0 ? 0 : index;
    settings.add(heatMap[index]);

    return settings;
  }

}
