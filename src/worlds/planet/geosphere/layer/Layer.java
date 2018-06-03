
package worlds.planet.geosphere.layer;

import java.util.HashSet;
import java.util.Set;
import worlds.planet.PlanetCell;
import worlds.planet.Surface;

/**
 *
 * @author Richard
 */
public class Layer {
  
  private RockType type;
  
  /**
   * The average averageDensity of the layer, this is based on an average for that type.
   * The units are in grams per liter.
   */
  private float averageDensity;
  
  private float averageErosionFactor;
  
  private float totalMass;
  
  private float specificHeat;
  
  /**
   * The temperature of this layer
   */
  private float temperature;
  
  private float granularity;
  
  private long depositTimeStamp;
  /**
   * An object reference of the layer below this Stratum.
   */
  private Layer bottom, top;
  
  /**
   * The list of materials that make up this Rock Layer. This forms layers 
   * such as granite, basalt, sandstones, or sediments eroded from these
   * rocks. 
   */
  private final Set<LayerMaterial> materials;
  
  public Layer() {
    this.materials = new HashSet<>();
    update();
  }
  
  /**
   * Copy constructor
   * @param layer A Deep copy of this Layer and the Materials inside it.
   */
  public Layer(Layer layer) {
    this.bottom = layer.bottom;
    this.top = layer.top;
    this.materials = layer.copyMaterials();
    this.averageDensity = layer.averageDensity;
    this.averageErosionFactor = layer.averageErosionFactor;
    this.granularity = layer.granularity;
    this.temperature = layer.temperature;
    this.totalMass = layer.totalMass;
    this.type = layer.type;
  }

  public Layer(Set<LayerMaterial> materials) {
    this.materials = new HashSet<>();
    this.materials.addAll(materials);
    update();
  }
  
  /**
   * Sets the age of this cell to the time stamp given.
   */
  public void recordTime() {
    this.depositTimeStamp = Surface.planetAge.get();
  }

  public long getAge() {
    return Surface.planetAge.get() - depositTimeStamp;
  }
  
  private void update() {
    calculateErosionFactor();
    calculateDensity();
    calculateTotalMass();
    calculateSpecificHeat();
  }
  
  private void calculateSpecificHeat() {
    specificHeat = 0;
    materials.forEach(material -> {
      specificHeat += material.getSpecificHeat();
    });
    specificHeat /= materials.size();
  }
  
  private void calculateTotalMass() {
    totalMass = 0;
    materials.forEach(material -> {
      totalMass += material.getMass();
    });
  }
  
  private void calculateDensity() {
    averageDensity = 0;
    materials.forEach(material -> {
      averageDensity += material.getDensity();
    });
    averageDensity /= materials.size();
  }
  
  private void calculateErosionFactor() {
    averageErosionFactor = 0;
    materials.forEach(material -> {
      averageErosionFactor += material.getErosionFactor();
    });
    averageErosionFactor /= materials.size();
  }
  
  public void addMaterials(Layer layer) {
    addMaterials(layer.materials);
  }
  
  public void addMaterials(Set<LayerMaterial> materials) {
    materials.forEach(material -> {
      addMaterial(material);
    });
  }
  
  /**
   * Searches for the material that matches the type being added. No matches
   * adds the material, matches update the current material.
   * @param material 
   */
  public void addMaterial(LayerMaterial material) {
    // Search for the material if it exists and update that instead of
    // adding a new one, otherwise add a new one.
    boolean found = false;
    for (LayerMaterial m : materials) {
      if (m.getName().equals(material.getName())) {
        m.addMass(material.getMass());
        found = true;
        break;
      }
    }
    if (!found) {
      materials.add(material);
    }
    update();
  }

  public float getMass() {
    return totalMass;
  }
  
  /**
   * Returns the volume of this stratum in cubic meters
   *
   * @return The volume of this stratum
   */
  public float getVolume() {
    return getMass() / averageDensity;
  }

  /**
   * The thickness is calculated based on the volume of the stratum and the area
   * of a cell. This is also the same as height.
   *
   * @return The thickness of this stratum.
   */
  public float getThickness() {
    return getVolume() / PlanetCell.area;
  }
  
  public float getSpecificHeat() {
    return specificHeat;
  }
  
  /**
   * Performs a deep copy of the materials in the layer.
   * @return 
   */
  public Set<LayerMaterial> copyMaterials() {
    Set<LayerMaterial> copies = new HashSet<>();
    totalMass = 0;
    materials.forEach(material -> {
      copies.add(material.copy());
    });
    return copies;
  }
  
  /**
   * Removes an equal amount of mass from all the materials in this layer.
   * Also returns a list of each material removed based on their proportions.
   * @param mass The amount of mass being removed from this Rock Layer.
   * @return 
   */
  public Set<LayerMaterial> removeMaterial(float mass) {
    Set<LayerMaterial> removedMaterials = new HashSet<>();
    // If the mass being removed is greater than the totalMass
    if (mass > totalMass) {
      removedMaterials = copyMaterials();
      materials.clear();
      update();
      return removedMaterials;
    }
    
    final float massToRemove = mass;
    
    // Search for the material if it exists and update that instead of
    // adding a new one, otherwise add a new one.
    for (LayerMaterial material : materials) {
      float massOfMaterial = material.getMass();
      float ratioOfMaterial = massOfMaterial / totalMass;
      float removedMass = massToRemove * ratioOfMaterial;
      
      // Copy the material and set it's mass to the correct ratio
      LayerMaterial removedMaterial = material.copy();
      
      material.addMass(-removedMass);
      if (material.getMass() == 0) {
        removedMaterial.setMass(0);
      } else {
        removedMaterial.setMass(removedMass);
      }
      
      removedMaterials.add(removedMaterial);
    }
    
    // Remove materials with a zero mass.
    materials.removeIf(m -> m.getMass() == 0);
    
    update();
    return removedMaterials;
  }
  
  public float getDensity() {
    return averageDensity;
  }

  /**
   * The average erosion factor based on this material in this layer
   * @return The erosion factor for this layer.
   */
  public float getErosionFactor() {
    return averageErosionFactor;
  }

  public RockType getType() {
    return type;
  }
  
  /**
   * Create a reference to the layer below this stratum. This helps create
   * another data structure for easier iteration through the strata.
   *
   * @param stratum The stratum being referenced below this stratum.
   */
  public void setBottom(Layer stratum) {
    bottom = stratum;
  }

  public void setTop(Layer stratum) {
    top = stratum;
  }

  /**
   * Fetches the stratum layer above this one if it exists, null otherwise.
   *
   * @return The layer above this layer.
   */
  public Layer previous() {
    return top;
  }

  /**
   * The stratum below this stratum.
   *
   * @return The next stratum below this.
   */
  public Layer next() {
    return bottom;
  }

  /**
   * Nullifies the bottom reference of this stratum.
   */
  public void removeBottom() {
    bottom = null;
  }

  public void removeTop() {
    top = null;
  }

  public Layer copy() {
    return new Layer(this);
  }

  
}
