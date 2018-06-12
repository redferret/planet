package worlds.planet.geosphere.tasks.heatmanagement;

import engine.task.CompoundTask;
import engine.task.TaskAdapter;
import worlds.planet.geosphere.Mantle;
import worlds.planet.PlanetCell;
import worlds.planet.geosphere.UpperMantle;
import static engine.surface.SurfaceMap.HDIR_X_INDEX;
import static engine.surface.SurfaceMap.HDIR_Y_INDEX;

/**
 *
 * @author Richard
 */
public class MagmaFlow extends CompoundTask {

  private final UpperMantle mantle;

  public MagmaFlow(UpperMantle mantle) {
    this.mantle = mantle;
  }

  @Override
  public void setup() {
    addSubTask(new UpdateVelocities());
    addSubTask(new ApplyAcceleration());
  }

  /**
   * Updates the acceleration for the magma under the mantle
   */
  private class UpdateVelocities extends TaskAdapter {

    @Override
    public void perform(int x, int y) throws Exception {
      Mantle mantleCell = mantle.getCellAt(x, y);

      if (x == 2 && y == 2) {
        int i = 0;
      }

      float mantleTemp = mantleCell.getTemperature();
      float curMagma = mantleCell.getMagma();
      float[] accelerationField = new float[]{0, 0, 0, 0};

      for (int a = 0; a < 4; a++) {
        Mantle neighbor = mantle.getCellAt(x + HDIR_X_INDEX[a], y + HDIR_Y_INDEX[a]);

        float outflowFlux = accelerationField[a];
        float neighborTemp = neighbor.getTemperature(); 
        float neighborMagma = neighbor.getMagma(); 
         
        if (curMagma != 0 || neighborMagma != 0) { 
          float h = mantleTemp + curMagma - neighborTemp - neighborMagma; 
          float gravity = 9.8f; 
          outflowFlux += (h * gravity * 50f) / PlanetCell.length; 
          accelerationField[a] = -outflowFlux; 
        } else if (curMagma == 0 && neighborMagma == 0){ 
          accelerationField[a] = 0; 
          mantleCell.setVelocityAt(a, 0); 
        } 
      }
      mantleCell.setMagmaAcceleration(accelerationField);
    }
  }

  /**
   * Applies the acceleration to the velocity of the magma
   */
  private class ApplyAcceleration extends TaskAdapter {

    @Override
    public void perform(int x, int y) throws Exception {
      Mantle mantleCell = mantle.getCellAt(x, y);
      mantleCell.updateVelocity();
      mantleCell.applyDrag();
      mantleCell.updateMagma();
    }
  }

}
