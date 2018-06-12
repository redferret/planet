
package worlds.planet.geosphere;

import engine.concurrent.AtomicFloat;
import static java.lang.Float.NaN;
import static java.lang.Float.max;
import static java.lang.Float.min;
import worlds.planet.PlanetCell;
import worlds.planet.Util;

/**
 * Additional properties of a cell that every cell would share
 * @author Richard
 */
public abstract class CellProperties {
  
  private final AtomicFloat magma;
//  private final float[] magmaVelocityField;
  private final float[] magmaFlow;
  private final float[] magmaFlowBuffer;
  
  private final AtomicFloat temperature;
  private float newTemperature;

  public CellProperties(float initialTemp) {
    this.temperature = new AtomicFloat(initialTemp);
    newTemperature = NaN;
    magma = new AtomicFloat(0);
    magmaFlow = new float[4];
//    magmaVelocityField = new float[4];
    magmaFlowBuffer = new float[4];
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
  
  public float getMagma() {
    return magma.get();
  }
  
  public void setMagmaFlowBuffer(float[] flux) {
    System.arraycopy(flux, 0, magmaFlowBuffer, 0, 4);
  }
  
  public void setFlowAt(int index, float vel) {
    magmaFlow[index] = vel;
  }
  
  public void addToMagma(float amount) {
    magma.set(max(0, magma.get() + amount));
  }
  
  public void applyFlowBuffer() {
    System.arraycopy(magmaFlowBuffer, 0, magmaFlow, 0, 4);
  }
  
  public void applyDrag() {
    for (int v = 0; v < 4; v++) {
      float vel = magmaVelocityField[v];
      float mass = magma.get();
      if (mass != 0){
        float drag = -0.0015f * vel;
        magmaVelocityField[v] += drag;
      }
    }
  }
  
  public void updateMagma() {

    float sumOfFlows = 0;
    for (int a = 0; a < 4; a++) {
      sumOfFlows += magmaFlow[a];
    }

    float totalMagma = magma.get() + sumOfFlows;
    magma.set(max(0, totalMagma));
    
  }
  
}
