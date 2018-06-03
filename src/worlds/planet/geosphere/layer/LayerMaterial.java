
package worlds.planet.geosphere.layer;

import com.jme3.math.Vector4f;

/**
 *
 * @author Richard
 */

public class LayerMaterial {
  
  private final String name;
  
  /**
   * The density of this material
   */
  private final float density;
  private final float erosionFactor;
  
  /**
   * The thermal conductivity of this material. High values means high conductivity
   * See more at https://www.khanacademy.org/science/physics/thermodynamics/specific-heat-and-heat-transfer/a/what-is-thermal-conductivity
   */
  private float specificHeat;
  /**
   * The mass of this material
   */
  private float mass;
  /**
   * The displayed color this layer should be on the screen.
   */
  private final Vector4f color;
  
  private LayerMaterial(String name, float mass, float specificHeat, 
          float density, float erosionFactor, Vector4f color) {
    this.name = name;
    this.density = density;
    this.erosionFactor = erosionFactor;
    this.mass = mass;
    this.color = color;
    this.specificHeat = specificHeat;
  }
  
  public LayerMaterial copy() {
    return new LayerMaterial(this.name, this.mass, this.specificHeat, 
            this.density, this.erosionFactor, this.color);
  }

  public float getSpecificHeat() {
    return specificHeat;
  }
  
  public float getErosionFactor() {
    return erosionFactor;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj instanceof LayerMaterial) {
      LayerMaterial mat = (LayerMaterial) obj;
      return mat.name.equals(this.name);
    } else {
      return false;
    }
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
  
  public float getDensity() {
    return density;
  }

  public float getMass() {
    return mass;
  }

  public Vector4f getColor() {
    return color;
  }
  
  public void addMass(float mass) {
    this.mass += mass;
    if (this.mass < 0) {
      this.mass = 0;
    }
  }
  
  public void setMass(float mass) {
    this.mass = mass;
  }

  public String getName() {
    return name;
  }
  
  @Override
  public String toString() {
    return "(" + getName() + " " + getMass() + ")";
  }
  
  public static final String BASALT = "Basalt";
  
  public static LayerMaterial getLayer(String layerName) {
    return getLayer(layerName, 0);
  }
  
  public static LayerMaterial getLayer(String layerName, float initMass) {
    switch(layerName) {
      case BASALT:
        return new LayerMaterial(layerName, initMass, 1.5f, 2.8f, 2.0f, 
                new Vector4f(0.44f, 0.36f, 0.4f, 1.0f));

      default:
        return null;
    }
  }
  
}