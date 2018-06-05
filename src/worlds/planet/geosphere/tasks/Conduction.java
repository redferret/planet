
package worlds.planet.geosphere.tasks;

import com.jme3.math.Vector2f;
import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.util.Delay;
import engine.util.task.Task;
import worlds.planet.PlanetCell;
import worlds.planet.geosphere.HeatConduction;

/**
 *
 * @author Richard
 * @param <C>
 */
public abstract class Conduction<C extends Cell> extends Task {
  
  protected final SurfaceMap<C> surface;
  protected final Delay delay;
  public static int timeStep = 1;
  
  public Conduction(SurfaceMap surface) {
    this.surface = surface;
    delay = new Delay(100);
  }
  @Override
  public void construct() {}
  
  @Override
  public boolean check() throws Exception {
    return delay.check();
  }
  
  public float[] getTemperatures(int x, int y, HeatConduction top, HeatConduction bottom) {
    Vector2f left = new Vector2f(x + 1, y);
    Vector2f right = new Vector2f(x - 1, y);
    Vector2f forward = new Vector2f(x, y + 1);
    Vector2f back = new Vector2f(x, y - 1);
    Vector2f[] neighbors = new Vector2f[]{left, right, forward, back};
    
    float T[] = new float[6];
    for (int t = 0; t < 4; t++) {
      HeatConduction cell = surface.getCellAt(neighbors[t]);
      T[t] = cell.getTemperature();
    }
    T[4] = top.getTemperature();
    T[5] = bottom.getTemperature();
    
    return T;
  } 
  
  public float[] calculateHeatConductance(int x, int y, float zLength, 
          HeatConduction top, HeatConduction bottom) {
    HeatConduction centerCell = surface.getCellAt(x, y);
    float centerConductivity = centerCell.getHeatConductivity();
    
    Vector2f left = new Vector2f(x + 1, y);
    Vector2f right = new Vector2f(x - 1, y);
    Vector2f forward = new Vector2f(x, y + 1);
    Vector2f back = new Vector2f(x, y - 1);
    Vector2f[] neighbors = new Vector2f[]{left, right, forward, back};
    
    // Calculate the conductance of each neighboring Cell
    float K_neighbors[] = new float[6];
    float area = zLength * PlanetCell.length;
    for (int k = 0; k < 4; k++) {
      HeatConduction cell = surface.getCellAt(neighbors[k]);
      float heatConductivity = cell.getHeatConductivity();
      K_neighbors[k] = calculateConductance(area, zLength, centerConductivity, heatConductivity);
    }
    // Calculate the conductance of each top and bottom cell
    K_neighbors[4] = calculateConductance(PlanetCell.area, zLength, centerConductivity, top.getHeatConductivity());
    K_neighbors[5] = calculateConductance(PlanetCell.area, zLength, centerConductivity, bottom.getHeatConductivity());
    return K_neighbors;
  }

  private static float calculateConductance(float area, float zLength, 
          float h1Conductivity, float h2Conductivity) {
    return area / ((zLength / (2*h1Conductivity)) * (zLength / (2*h2Conductivity)));
  }

  public float calculateHeatFlow(float[] K, float[] T, float temperatureCenterCell) {
    float heatFlow = 0;
    for (int c = 0; c < 6; c++) {
      heatFlow += (K[c] * (T[c] - temperatureCenterCell));
    }
    return heatFlow;
  }
  
  public float calculateNewTemperature(float heatFlow, float currentTemp, 
          float[] K_conds, float zLength) {
    
    float sumOfK = 0;
    for (float K : K_conds) {
      sumOfK += K;
    }
    float volume = PlanetCell.area * zLength;
    float capacity = timeStep * (sumOfK / volume);
    
    return currentTemp + ((timeStep * heatFlow) / (capacity * volume));
  }
  
  protected float getTemperatureChange(float joules, float area, float k, float length) {
    return (joules * length) / (k * area);
  }
  
  protected float getJoules(float neighborCellTemp, float curCellTemp, 
          float mass, float specificHeat) {
    float tempDiff = (curCellTemp - neighborCellTemp);
    float joules = (specificHeat * PlanetCell.area * tempDiff) / PlanetCell.length;
    return joules;
  }
}
