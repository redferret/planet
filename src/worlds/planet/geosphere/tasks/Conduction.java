
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
    delay = new Delay(1);
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
    T[4] = (top != null) ? top.getTemperature() : -273f;
    T[5] = (bottom != null) ? bottom.getTemperature() : -273f;
    
    return T;
  } 
  
  public float[] calculateHeatConductance(int x, int y, float zLength, 
          HeatConduction top, HeatConduction bottom) {
    HeatConduction centerCell = surface.getCellAt(x, y);
    float centerCapacity = centerCell.getHeatCapacity();
    
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
      float heatCapacity = cell.getHeatCapacity();
      K_neighbors[k] = calculateConductance(area, zLength, centerCapacity, 
              heatCapacity, cell.getHorizontalResistence());
    }
    // Calculate the conductance of each top and bottom cell
    K_neighbors[4] = (top != null) ?
            calculateConductance(PlanetCell.area, zLength, centerCapacity, 
                    top.getHeatCapacity(), top.getVerticalResistence()) 
            : 
            centerCell.topNullConductance();
    K_neighbors[5] = (bottom != null) ?
            calculateConductance(PlanetCell.area, zLength, centerCapacity, 
                    bottom.getHeatCapacity(), bottom.getVerticalResistence()) 
            : 
            centerCell.bottomNullConductance();
    
    return K_neighbors;
  }

  public void setNewTemperature(int x, int y, float zLength, Cell top, Cell bottom) {
    float[] K = calculateHeatConductance(x, y, zLength, top, bottom);
    Cell center = surface.getCellAt(x, y);
    float curTemp = center.getTemperature();
    float[] T = getTemperatures(x, y, top, bottom);
    float heatFlow = calculateHeatFlow(K, T, curTemp);
    float newTemp = this.calculateNewTemperature(heatFlow, curTemp, K, zLength);
    center.setNewTemperature(newTemp);
  }
  
  private static float calculateConductance(float area, float zLength, 
          float h1Capacity, float h2Capacity, float additionalResistence) {
    return area / ((zLength / (2*h1Capacity)) + (zLength / (2*h2Capacity)) + additionalResistence);
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
    return currentTemp + ((heatFlow / sumOfK) * 0.01f);
  }
  
}
