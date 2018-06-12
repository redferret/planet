
package worlds.planet.geosphere;

import engine.concurrent.AtomicFloat;
import static java.lang.Float.NaN;
import static java.lang.Float.max;

/**
 * Additional properties of a cell that every cell would share
 * @author Richard
 */
public abstract class CellProperties {
  
  private final AtomicFloat magma;
  private final float[] magmaVelocityField;
  private final float[] magmaAcceleration;
  
  private final AtomicFloat temperature;
  private float newTemperature;

  public CellProperties(float initialTemp) {
    this.temperature = new AtomicFloat(initialTemp);
    newTemperature = NaN;
    magma = new AtomicFloat(0);
    magmaVelocityField = new float[4];
    magmaAcceleration = new float[4];
  }
  
  /**
   * The heat capacity of the cell. Higher values hold more heat.
   * @return 
   */
  public abstract float getHeatCapacity();
  
  /**
   * Get the Horizontal Conductive Resistence of this cell. <br>
   * How much conductive resistence is applied horizontally
   * @return 
   */
  public abstract float getHCR();
  
  /**
   * Get the Bottom Conductive Resistence of this cell. <br>
   * How much conductive resistence is applied to the bottom of the cell
   * @return 
   */
  public abstract float getBCR();
  
  /**
   * Get the Top Conductive Resistence of this cell. <br>
   * How much conductive resistence is applied to the top of the cell
   * @return 
   */
  public abstract float getTCR();
  
  /**
   * The true depth of this layer
   * @return 
   */
  public abstract float getZLength();
  
  /**
   * Get the Top Null Conduction. <br>
   * If there is no top cell, use this default temperature. By default without
   * overriding this method it will return -273
   * @return By default without overriding this method it will return -273
   */
  public float getTopNullTemperature() {
    return -273f;
  }
  
  /**
   * Get the Bottom Null Temperature. <br>
   * If there is no bottom cell, use this default temperature. By default without
   * overriding this method it will return -273
   * @return By default without overriding this method it will return -273
   */
  public float getBNT() {
    return -273f;
  }
  
  /**
   * Get the Top Null Temperature. <br>
   * If there is no top cell for heat to conduct, then use this conduction
   * value. By default this method returns 0
   * @return By default this method returns 0
   */
  public float getTNC() {
    return 0;
  }
  
  /**
   * Get the Bottom Null Conduction. <br>
   * If there is no bottom cell for heat to conduct, then use this conduction
   * value. By default this method returns 0
   * @return By default this method returns 0
   */
  public float getBNC() {
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
  
  public void setMagmaAcceleration(float[] flux) {
    System.arraycopy(flux, 0, magmaAcceleration, 0, 4);
  }
  
  public void setVelocityAt(int index, float vel) {
    magmaVelocityField[index] = vel;
  }
  
  public void addToMagma(float amount) {
    magma.set(max(0, magma.get() + amount));
  }
  
  public void updateVelocity() {
    for (int a = 0; a < 4; a++) {
      magmaVelocityField[a] += magmaAcceleration[a];
      magmaAcceleration[a] = 0;
    }
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
      sumOfFlows += magmaVelocityField[a];
    }

    float totalMagma = magma.get() + sumOfFlows;
    magma.set(max(0, totalMagma));
    
  }
  
}
