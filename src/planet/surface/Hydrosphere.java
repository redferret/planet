

package planet.surface;

import planet.Planet;
import planet.cells.GeoCell;
import planet.cells.HydroCell;

import static planet.cells.HydroCell.minAngle;
import static planet.cells.HydroCell.evapScale;
import static planet.cells.HydroCell.oceanSedimentCapacity;
import static planet.enums.Layer.OCEAN;
import static planet.util.Tools.calcMass;
import static planet.util.Tools.clamp;
import static planet.util.Tools.getLowestCellFrom;

/**
 * The hydrosphere is everything that deal with water, rivers, lakes, seas,
 * and oceans.
 * @author Richard DeSilvey
 */
public abstract class Hydrosphere extends Geosphere {

    public static boolean drawOcean;

    static {
        drawOcean = true;
    }
    
    public Hydrosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
    }

    /**
     * Add a uniformed layer on the whole surface.
     * @param amount The amount being added
     */
    public void addWaterToAllCells(float amount){
        for (int x = 0; x < worldSize; x++){
            for (int y = 0; y < worldSize; y++){
                getCellAt(x, y).addOceanMass(amount);
            }
        }
    }
    
    public void updateOceans(int x, int y) {

    }
    
}
