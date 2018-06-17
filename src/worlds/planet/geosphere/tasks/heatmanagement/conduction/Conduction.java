 
package worlds.planet.geosphere.tasks.heatmanagement.conduction;

import com.jme3.math.Vector2f;
import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.util.Delay;
import engine.task.TaskAdapter;
import worlds.planet.PlanetCell;
import worlds.planet.geosphere.CellProperties;

/**
 *
 * @author Richard
 * @param <C>
 */
public abstract class Conduction<C extends Cell> extends TaskAdapter {
  
  protected final SurfaceMap<C> surface;
  protected final Delay delay;
  
  public Conduction(SurfaceMap surface) {
    this.surface = surface;
    delay = new Delay(1);
  }
  
  @Override
  public boolean check() throws Exception {
    return delay.check();
  }
  
  public float[] getTemperatures(int x, int y, CellProperties top, CellProperties bottom) {
    Vector2f left = new Vector2f(x + 1, y);
    Vector2f right = new Vector2f(x - 1, y);
    Vector2f forward = new Vector2f(x, y + 1);
    Vector2f back = new Vector2f(x, y - 1);
    Vector2f[] neighbors = new Vector2f[]{left, right, forward, back};
    Cell center = surface.getCellAt(x, y);
    float T[] = new float[6];
    for (int t = 0; t < 4; t++) {
      CellProperties cell = surface.getCellAt(neighbors[t]);
      T[t] = cell.getTemperature();
    }
    T[4] = (top != null) ? top.getTemperature() : center.getTopNullTemperature();
    T[5] = (bottom != null) ? bottom.getTemperature() : center.getBNT();
    
    return T;
  } 
  
  public float[] calculateHeatConductance(int x, int y,CellProperties top, 
          CellProperties bottom) {
    CellProperties centerCell = surface.getCellAt(x, y);
    float h2Capacity = centerCell.getHeatCapacity();
    
    Vector2f left = new Vector2f(x + 1, y);
    Vector2f right = new Vector2f(x - 1, y);
    Vector2f forward = new Vector2f(x, y + 1);
    Vector2f back = new Vector2f(x, y - 1);
    Vector2f[] neighbors = new Vector2f[]{left, right, forward, back};
    
    // Calculate the conductance of each neighboring Cell
    float K_neighbors[] = new float[6];
    float zLength1 = centerCell.getZLength();
    float neighboringArea = zLength1 * PlanetCell.length;
    for (int k = 0; k < 4; k++) {
      CellProperties neighbor = surface.getCellAt(neighbors[k]);
      float h1Capacity = neighbor.getHeatCapacity();
      float zLength2 = neighbor.getZLength();
      K_neighbors[k] = calculateConductance(neighboringArea, zLength1, zLength2, h2Capacity, 
              h1Capacity, centerCell.getHCR());
    }
    
    // Calculate the conductance of each top and bottom cell
    if (top != null) {
      float totalResistenceToTop = top.getBCR() + centerCell.getTCR();
      K_neighbors[4] = calculateConductance(PlanetCell.area, zLength1, top.getZLength(), h2Capacity,
              top.getHeatCapacity(), totalResistenceToTop);
      centerCell.getTNC();
    } else {
      K_neighbors[4] = centerCell.getTNC();
    }
    if (bottom != null) {
      float totalResistenceToBottom = bottom.getTCR() + centerCell.getBCR();
      K_neighbors[5] = calculateConductance(PlanetCell.area, zLength1, bottom.getZLength(), h2Capacity,
              bottom.getHeatCapacity(), totalResistenceToBottom);
    } else {
      K_neighbors[5] = centerCell.getBNC();
    }
    return K_neighbors;
  }

  public void setNewTemperature(int x, int y, Cell top, Cell bottom) {
    float[] K = calculateHeatConductance(x, y, top, bottom);
    Cell center = surface.getCellAt(x, y);
    float curTemp = center.getTemperature();
    float[] T = getTemperatures(x, y, top, bottom);
    float heatFlow = calculateHeatFlow(K, T, curTemp);
    float newTemp = calculateNewTemperature(heatFlow, curTemp, K);
    center.setNewTemperature(newTemp);
  }
  
  public static float calculateConductance(float area, float zLength1, float zLength2, 
          float h1Capacity, float h2Capacity, float additionalResistence) {
    return area / ((zLength1 / (2*h1Capacity)) + (zLength2 / (2*h2Capacity)) + additionalResistence);
  }

  public float calculateHeatFlow(float[] K, float[] T, float temperatureCenterCell) {
    float heatFlow = 0;
    for (int c = 0; c < 6; c++) {
      heatFlow += (K[c] * (T[c] - temperatureCenterCell));
      }
    return heatFlow;
  }
  
  public float calculateNewTemperature(float heatFlow, float currentTemp, 
          float[] K_conds) {
    float sumOfK = 0;
    for (float K : K_conds) {
      sumOfK += K;
    }
    return currentTemp + ((heatFlow / sumOfK) * 0.005f );
  }
  
}
