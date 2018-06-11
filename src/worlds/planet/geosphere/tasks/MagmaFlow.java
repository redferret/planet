
package worlds.planet.geosphere.tasks;

import engine.task.CompoundTask;
import engine.task.TaskAdapter;
import worlds.planet.geosphere.Mantle;
import worlds.planet.PlanetCell;
import worlds.planet.Util;
import worlds.planet.geosphere.UpperMantle;
import static engine.surface.SurfaceMap.HDIR_X_INDEX;
import static engine.surface.SurfaceMap.HDIR_Y_INDEX;
import static java.lang.Float.min;
import static java.lang.Math.acos;
import static java.lang.Math.atan;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;


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
      
      for(int flowIndex = 0; flowIndex < 4; flowIndex++) {
        int nx = x + HDIR_X_INDEX[flowIndex];
        int ny = y + HDIR_Y_INDEX[flowIndex];
        Mantle neighbor = mantle.getCellAt(nx, ny);
        
        float neighborTemp = neighbor.getTemperature();
        float neighborMagma = neighbor.getMagma();
        
        if (curMagma != 0 || neighborMagma != 0) {
          
          float curMagmaHeight = Util.calcHeight(curMagma, PlanetCell.area, 3.0f);
          float neighborMagmaHeight = Util.calcHeight(neighborMagma, PlanetCell.area, 3.0f);
          float h = mantleTemp + curMagmaHeight - neighborTemp - neighborMagmaHeight;
          
          float theta = (float) atan(h / PlanetCell.length);
          float totalMassDisplacement = Util.calcMass(h, PlanetCell.area, 3.0f) / 0.1f;
          float flow = (float) (totalMassDisplacement * sin(theta));
          
          if (h > 0) {
            accelerationField[flowIndex] = -flow;
          } else if (h < 0) {
            accelerationField[flowIndex] = flow;
          } else {
            accelerationField[flowIndex] = 0;
          }
        } else if (curMagma == 0 && neighborMagma == 0){
          accelerationField[flowIndex] = 0;
          mantleCell.setFlowAt(flowIndex, 0);
        }
      }
      mantleCell.setMagmaFlowBuffer(accelerationField);
    }
  }

  /**
   * Applies the acceleration to the velocity of the magma
   */
  private class ApplyAcceleration extends TaskAdapter {
    
    @Override
    public void perform(int x, int y) throws Exception {
      Mantle mantleCell = mantle.getCellAt(x, y);
      mantleCell.applyFlowBuffer();
      mantleCell.updateMagma();
    }
  }

}
