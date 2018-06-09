
package engine.surface;

import com.jme3.math.Vector2f;
import com.jme3.renderer.Camera;
import com.jme3.scene.Node;
import com.jme3.scene.VertexBuffer;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainPatch;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import worlds.planet.Util;

/**
 *
 * @author Richard
 */
public class TerrainSurface extends TerrainQuad {

  private TerrainLodControl control;
  
  public TerrainSurface(int totalSize) {
    super("surface", 33, totalSize + 1, null);
  }
  public void clearCameraControl() {
    removeControl(control);
  }
  
  public void bindCameraForLODControl(Camera camera) {
//    control = new TerrainLodControl(this, camera);
//    control.setLodCalculator(new DistanceLodCalculator(getPatchSize(), 5f));
//    addControl(control);
  }
  
  public void bindTerrainToNode(Node rootNode) {
    setLocalTranslation(0, 200, 0);
    setLocalScale(2f, 1f, 2f);
    rootNode.attachChild(this);
  }
  
  /**
   * Update the terrain's height based on the temperature of the mantle.
   * @param scale Scale the height with this value
   * @param min
   * @param max
   * @param map The data being mapped to this terrain
   * @param cellData
   */
  public void updateTerrainHeight(float scale, float min, float max, 
          SurfaceMap<Cell> map, TerrainHeightValue cellData) {
    List<Vector2f> locs = new ArrayList<>();
    List<Float> heights = new ArrayList<>();
    map.getMapData().values().forEach(cell -> {
      float height = cellData.getHeightValue(cell) * scale;
      Vector2f pos = cell.getGridPosition();
      locs.add(Util.scalePositionForTerrain(pos.getX(), pos.getY(), map.getSize()));
      heights.add(height < min ? min : (height > max ? max : height));
    });
    setHeight(locs, heights);
  }

  public void updateVertexColors(float colorMap[][], MapBounds bounds) {
    List<TerrainPatch> patches = new ArrayList<>();
    getAllTerrainPatches(patches);
    patches.forEach(patch -> {
      float[] heightMap = patch.getHeightMap();
      float[] colorArray = new float[heightMap.length * 4];
      // Iterate over the heightMap, plug the height values into a function
      // to get a color and set that into the colorArray.
      int colorIndex = 0;
      for (int h = 0; h < heightMap.length; h++) {
        float height = heightMap[h];
        int colorMapIndex = bounds.getIndex(height);
        float[] color = colorMap[colorMapIndex];
        colorArray[colorIndex++] = color[0];// red
        colorArray[colorIndex++] = color[1];// green
        colorArray[colorIndex++] = color[2];// blue
        colorArray[colorIndex++] = color[3];// alpha
      }
      
      patch.getMesh().setBuffer(VertexBuffer.Type.Color, 4, colorArray);
      
    });
  }

}
