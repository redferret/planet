import java.util.HashSet;
import java.util.Set;
import org.junit.*;
import worlds.planet.geosphere.Layer;
import static org.junit.Assert.*;
import worlds.planet.geosphere.LayerMaterial;

/**
 *
 * @author Richard
 */
public class LayerTest {
  
  @Test
  public void addDuplicateMaterialTest() {
    Layer testLayer = new Layer();
    assertTrue(testLayer.getMass() == 0);
    
    LayerMaterial m1 = new LayerMaterial("Test Material 1", 1000, 1, 1.2f, 1, null);
    
    testLayer.addMaterial(m1);
    testLayer.addMaterial(m1);
    
    assertTrue(testLayer.getMass() == 2000);
    assertTrue(testLayer.getDensity() == 1.2f);
    
    Set<LayerMaterial> materials = testLayer.copyMaterials();
    
    assertTrue(materials.size() == 1);
    materials.forEach(mat -> {
      assertTrue(mat.equals(m1));// The copy is equal in value
      assertTrue(mat != m1); // The copy is not the same object
    });
  }
  
  @Test
  public void addMaterialsTest() {
    Layer testLayer = new Layer();
    assertTrue(testLayer.getMass() == 0);
    
    LayerMaterial m1 = new LayerMaterial("Test Material 1", 1000, 1, 1.2f, 1, null);
    LayerMaterial m2 = new LayerMaterial("Test Material 2", 50, 1, 2.2f, 1, null);
    LayerMaterial m3 = new LayerMaterial("Test Material 3", 100, 1, 1.3f, 1, null);
    
    testLayer.addMaterial(m1);
    assertTrue(testLayer.getMass() == 1000);
    
    testLayer.addMaterial(m2);
    testLayer.addMaterial(m3);
    assertTrue(testLayer.getMass() == 1150);
    
    float expectedDensity = (1.2f + 2.2f + 1.3f) / 3;
    assertTrue(testLayer.getDensity() == expectedDensity);
  }
  
  @Test
  public void addSetOfMaterialsTest() {
    Layer testLayer = new Layer();
    assertTrue(testLayer.getMass() == 0);
    
    LayerMaterial m1 = new LayerMaterial("Test Material 1", 1000, 1, 1.2f, 1, null);
    LayerMaterial m2 = new LayerMaterial("Test Material 2", 50, 1, 2.2f, 1, null);
    LayerMaterial m3 = new LayerMaterial("Test Material 3", 100, 1, 1.3f, 1, null);
    
    Set<LayerMaterial> materials = new HashSet<>();
    
    materials.add(m1);
    materials.add(m2);
    materials.add(m3);
    testLayer.addMaterials(materials);
    
    assertEquals(1150, testLayer.getMass(), 0f);
    float expectedDensity = (1.2f + 2.2f + 1.3f) / 3;
    assertTrue(testLayer.getDensity() == expectedDensity);
  }
  
  @Test
  public void addAnotherLayerTest() {
    Layer testLayer1 = new Layer();
    Layer testLayer2 = new Layer();
    assertTrue(testLayer1.getMass() == 0);
    
    LayerMaterial m1 = new LayerMaterial("Test Material 1", 1000, 1, 1.2f, 1, null);
    LayerMaterial m2 = new LayerMaterial("Test Material 2", 50, 1, 2.2f, 1, null);
    LayerMaterial m3 = new LayerMaterial("Test Material 3", 100, 1, 1.3f, 1, null);
    
    Set<LayerMaterial> materials = new HashSet<>();
    
    materials.add(m1);
    materials.add(m2);
    materials.add(m3);
    testLayer1.addMaterials(materials);
    
    testLayer2.addMaterial(m1);
    
    testLayer2.addMaterials(testLayer1);
    
    assertEquals(2150, testLayer2.getMass(), 0f);
  }
  
  @Test
  public void removeMaterialTest() {
    Layer testLayer = new Layer();
    
    LayerMaterial m1 = new LayerMaterial("Test Material 1", 1000, 1, 1.2f, 1, null);
    LayerMaterial m2 = new LayerMaterial("Test Material 2", 50, 1, 2.2f, 1, null);
    LayerMaterial m3 = new LayerMaterial("Test Material 3", 100, 1, 1.3f, 1, null);
    
    Set<LayerMaterial> materials = new HashSet<>();
    
    materials.add(m1);
    materials.add(m2);
    materials.add(m3);
    testLayer.addMaterials(materials);
    
    Set<LayerMaterial> removed = testLayer.removeMaterial(500);
    
    float expectedM1Mass = (1000f/1150f) * 500;
    float expectedM2Mass = (50f/1150f) * 500;
    float expectedM3Mass = (100f/1150f) * 500;
    
    removed.forEach(material -> {
      float testMass = material.getMass();
      switch(material.getName()) {
        case "Test Material 1":
          assertEquals(expectedM1Mass, testMass, 0f);
          break;
        case "Test Material 2":
          assertEquals(expectedM2Mass, testMass, 0f);
          break;
        case "Test Material 3":
          assertEquals(expectedM3Mass, testMass, 0f);
          break;
        default:
          fail();
      }
    });
    float expectedTotalMass = 1150 - expectedM1Mass - expectedM2Mass - expectedM3Mass;
    assertTrue(testLayer.getMass() == expectedTotalMass);
    
    Layer removedLayer = new Layer(removed);
    assertTrue(removedLayer.getMass() == 500);
  }
}
