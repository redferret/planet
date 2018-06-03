package worlds.planet.geosphere;

import com.jme3.math.Vector2f;
import com.jme3.scene.VertexBuffer.Type;
import com.jme3.terrain.geomipmap.TerrainPatch;
import engine.util.Vec2;
import java.util.ArrayList;
import java.util.List;
import worlds.planet.Surface;
import worlds.planet.Util;
import static worlds.planet.geosphere.Mantle.heatMap;
import worlds.planet.geosphere.tasks.MantleConduction;
import worlds.planet.geosphere.tasks.MantleRadiation;
/**
 * Contains all logic that works on the geology of the planet.
 *
 * @author Richard DeSilvey
 */
public abstract class Geosphere extends Surface {

  private long ageStamp;
  
  public Geosphere(int totalSize, int threadsDelay, int threadCount) {
    super(totalSize, threadsDelay, threadCount);
    ageStamp = 0;
    produceTasks(() -> {
      return new MantleRadiation(this);
    });
    produceTasks(() -> {
      return new MantleConduction(this);
    });
  }
  
  /**
   * Update the terrain's height based on the temperature of the mantle.
   * @param scale Scale the height with this value
   */
  public void updateTerrainForMantleTemp(float scale) {
    List<Vector2f> locs = new ArrayList<>();
    List<Float> heights = new ArrayList<>();
    map.values().forEach(cell -> {
      float height = cell.getMantleTemperature() * scale;
      Vec2 pos = cell.getGridPosition();
      locs.add(Util.scalePositionForTerrain(pos.getX(), pos.getY(), getTerrainSize() + 1));
      heights.add(height);
    });
    setHeight(locs, heights);
  }
  
  public void updateVertexColors() {
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
        int heatColorIndex = (int) (height < -2.73f ? 0 : (height > 50 ? 50 : height));
        float[] heatColor = heatMap[heatColorIndex];
        colorArray[colorIndex++] = heatColor[0];// red
        colorArray[colorIndex++] = heatColor[1];// green
        colorArray[colorIndex++] = heatColor[2];// blue
        colorArray[colorIndex++] = heatColor[3];// alpha
      }
      
      patch.getMesh().setBuffer(Type.Color, 4, colorArray);
      
    });
  }
  
  public long getAgeStamp() {
    return ageStamp;
  }

  public void setAgeStamp(long ageStamp) {
    this.ageStamp = ageStamp;
  }
  
}
