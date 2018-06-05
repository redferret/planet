
package worlds.planet.geosphere;

/**
 *
 * @author Richard
 */
public interface HeatConduction {
  public float getHeatConductivity();
  public void addToTemperatureFlux(float flux);
  public float getTemperature();
}
