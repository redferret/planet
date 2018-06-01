
package worlds.planet.geosphere;

import java.awt.Color;

/**
 * Iron Oxide
 * Olivine biotite
 * plagioclase feldspar and pyroxene
 * Biotite
 * orthoclase feldspar, quartz, plagioclase feldspar, mica, and amphibole
 * 
 * @author Richard
 */
public class Material {
  
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
  private final Color color;
  
  public Material(String name, float mass, float specificHeat, 
          float density, float erosionFactor, Color color) {
    this.name = name;
    this.density = density;
    this.erosionFactor = erosionFactor;
    this.mass = mass;
    this.color = color;
    this.specificHeat = specificHeat;
  }
  
  public Material copy() {
    return new Material(this.name, this.mass, this.specificHeat, 
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
    if (obj instanceof Material) {
      Material mat = (Material) obj;
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

  public Color getColor() {
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
  
}
