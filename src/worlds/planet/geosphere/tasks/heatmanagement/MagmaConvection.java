package worlds.planet.geosphere.tasks.heatmanagement;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.task.CompoundTask;
import engine.task.TaskAdapter;
import worlds.planet.geosphere.Core;
import worlds.planet.PlanetCell;
import static engine.surface.SurfaceMap.HDIR_X_INDEX;
import static engine.surface.SurfaceMap.HDIR_Y_INDEX;

/**
 *
 * @author Richard
 */
public class MagmaConvection extends CompoundTask {

  private final SurfaceMap surface;

  public MagmaConvection(SurfaceMap surface) {
    this.surface = surface;
  }

  @Override
  public void setup() {
    addSubTask(new UpdateVelocities());
    addSubTask(new ApplyAcceleration());
  }

  /**
   * Updates the acceleration for the magma under the surface
   */
  private class UpdateVelocities extends TaskAdapter {

    @Override
    public void perform(int x, int y) throws Exception {
      Cell cell = surface.getCellAt(x, y);

      float curTemp = cell.getTemperature();
      float curMagma = cell.getMagma();
      float[] accelerationField = new float[4];

      for (int a = 0; a < 4; a++) {
        Cell neighbor = surface.getCellAt(x + HDIR_X_INDEX[a], y + HDIR_Y_INDEX[a]);

        float outflowFlux = accelerationField[a];
        float neighborTemp = neighbor.getTemperature(); 
        float neighborMagma = neighbor.getMagma(); 
         
        if (curMagma != 0 || neighborMagma != 0) { 
          float h;
          if (surface instanceof Core) {
            h = curMagma - curTemp - neighborMagma + neighborTemp; 
          } else {
            h = curTemp + curMagma - neighborTemp - neighborMagma; 
          }
          float gravity = 9.8f; 
          outflowFlux += (h * gravity * 20f) / PlanetCell.length; 
          accelerationField[a] = -outflowFlux; 
        } else if (curMagma == 0 && neighborMagma == 0){ 
          accelerationField[a] = 0; 
          cell.setVelocityAt(a, 0); 
        } 
      }
      cell.setMagmaAcceleration(accelerationField);
    }
  }

  /**
   * Applies the acceleration to the velocity of the magma
   */
  private class ApplyAcceleration extends TaskAdapter {

    @Override
    public void perform(int x, int y) throws Exception {
      Cell cell = surface.getCellAt(x, y);
      cell.updateVelocity();
      cell.applyDrag();
      cell.updateMagma();
    }
  }

}
