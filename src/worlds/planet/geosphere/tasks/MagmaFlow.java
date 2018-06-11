
package worlds.planet.geosphere.tasks;

import engine.task.CompoundTask;
import engine.task.TaskAdapter;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;
import worlds.planet.PlanetCell;
import static engine.surface.SurfaceMap.HDIR_X_INDEX;
import static engine.surface.SurfaceMap.HDIR_Y_INDEX;
import engine.util.Delay;
import static java.lang.Float.max;

/**
 *
 * @author Richard
 */
public class MagmaFlow extends CompoundTask {

  private final Lithosphere crust;
  
  public MagmaFlow(Lithosphere crust) {
    this.crust = crust;
  }

  @Override
  public void setup() {
    addSubTask(new UpdateVelocities());
    addSubTask(new ApplyAcceleration());
  }

  /**
   * Updates the acceleration for the magma under the crust
   */
  private class UpdateVelocities extends TaskAdapter {
    
    @Override
    public void perform(int x, int y) throws Exception {
      Crust crustCell = crust.getCellAt(x, y);

      float crustTemp = crustCell.getTemperature();
      float curMagma = crustCell.getMagma();
      float[] accelerationField = crustCell.getMagmaOutflowFlux();
      
      for(int a = 0; a < 4; a++) {
        float outflowFlux = accelerationField[a];
        Crust neighbor = crust.getCellAt(x + HDIR_X_INDEX[a], y + HDIR_Y_INDEX[a]);
        
        float neighborTemp = neighbor.getTemperature();
        float neighborMagma = neighbor.getMagma();
        
        if (curMagma != 0 || neighborMagma != 0) {
          float h = crustTemp + curMagma - neighborTemp - neighborMagma;
          float gravity = 9.8f;
          outflowFlux += (h * gravity) / PlanetCell.length;
          accelerationField[a] = -outflowFlux * 50f;
        } else if (curMagma == 0 && neighborMagma == 0){
          accelerationField[a] = 0;
          crustCell.setVelocityAt(a, 0);
        }
        
      }
      
      crustCell.setOutflowFluxBuffer(accelerationField);
      
    }
  }

  /**
   * Applies the acceleration to the velocity of the magma
   */
  private class ApplyAcceleration extends TaskAdapter {
    
    @Override
    public void perform(int x, int y) throws Exception {
      Crust crustCell = crust.getCellAt(x, y);
      crustCell.updateVelocity();
      crustCell.updateMagma();
    }
  }

}
