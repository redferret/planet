package engine.states;

import com.jme3.app.Application;
import com.jme3.app.state.AbstractAppState;
import com.jme3.app.state.AppStateManager;
import com.jme3.material.Material;
import engine.PlanetApp;
import engine.surface.SurfaceMap;
import engine.surface.TerrainSurface;
import worlds.planet.PlanetCell;
import worlds.planet.TestWorld;
import worlds.planet.Util;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.geosphere.Core;
import worlds.planet.geosphere.UpperMantle;
import static worlds.planet.Util.heatMap;

/**
 *
 * @author Richard
 */
public class SurfaceState extends AbstractAppState {

  public static final String LITHOSPHERE = "lithosphere", 
          UPPER_MANTLE = "upperMantle",CORE = "core";

  private TerrainSurface temperatureTerrain;
  private TerrainSurface magmaTerrain;
  private PlanetApp app;
  private Lithosphere lithosphere;
  private UpperMantle upperMantle;
  private Core core;
  private boolean wireframe = false;
  private Material unshadedMat;
  private String attachedSurface;

  @Override
  public void initialize(AppStateManager stateManager, Application app) {
    super.initialize(stateManager, app);
    unshadedMat = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
    unshadedMat.setBoolean("VertexColor", true);
    unshadedMat.getAdditionalRenderState().setWireframe(wireframe);
    this.app = (PlanetApp) app;
    AppStateManager asm = this.app.getStateManager();
    WorldState worldState = asm.getState(WorldState.class);
    TestWorld world = worldState.getWorld();
    attachedSurface = "";
    lithosphere = world.getLithosphere();
    upperMantle = world.getUpperMantle();
    core = world.getCore();
    
    attachedSurface = CORE;
    temperatureTerrain = new TerrainSurface(world.getTerrainWidth());
    magmaTerrain = new TerrainSurface(world.getTerrainWidth());
    
//    temperatureTerrain.bindTerrainToNode(((PlanetApp) app).getRootNode());
//    temperatureTerrain.setMaterial(unshadedMat);
    
    magmaTerrain.bindTerrainToNode(((PlanetApp) app).getRootNode());
    magmaTerrain.setMaterial(unshadedMat);
    
  }

  public boolean isWireFramed() {
    return wireframe;
  }

  public void negWireFramed() {
    wireframe = !wireframe;
    magmaTerrain.getMaterial().getAdditionalRenderState().setWireframe(wireframe);
  }

  @Override
  public void update(float timeSlice) {
    switch (attachedSurface) {
      case LITHOSPHERE:
        showTemperature(lithosphere);
        return;
      case UPPER_MANTLE:
        showTemperature(upperMantle);
        return;
      case CORE:
        showTemperature(core);
    }
  }

  public void showTemperature(SurfaceMap map) {
//    temperatureTerrain.updateTerrainHeight(0.01f, -2.73f, 50, map, (cell) -> {
//      return cell.getTemperature();
//    });
    
    magmaTerrain.updateTerrainHeight(0.01f, 0, 50, map, (cell) ->{
      return cell.getMagma();
    });

    
//    temperatureTerrain.updateVertexColors(heatMap, (heightVal) -> {
//      return (int) (heightVal < 0 ? 0
//              : (heightVal > heatMap.length - 1 ? heatMap.length - 1
//                      : heightVal));
//    });
    
    magmaTerrain.updateVertexColors(heatMap, (heightVal) -> {
      return (int) (heightVal < 0 ? 0
              : (heightVal > heatMap.length - 1 ? heatMap.length - 1
                      : heightVal));
    });
  }
 
  @Override
  public void cleanup() {
    super.cleanup();
  }

  public void renderSurface(String surfaceName) {
    attachedSurface = surfaceName;
  }

}
