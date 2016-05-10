

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

        HydroCell cellToUpdate, lowestCell;
        
        float lowestHeight, curCellHeight, displacedMass,
                diffGeoHeight, differenceHeight, totalMass;

        cellToUpdate = (HydroCell)getCellAt(x, y);
        lowestCell = (HydroCell)getLowestCellFrom(cellToUpdate);

        if (lowestCell == null || cellToUpdate == null) {
            return;
        }

        HydroCell.WaterBuffer toUpdateWaterBuffer = cellToUpdate.getWaterBuffer();
        HydroCell.WaterBuffer lowestHydroBuffer = lowestCell.getWaterBuffer();
        
        toUpdateWaterBuffer.applyWaterBuffer();
        lowestHydroBuffer.applyWaterBuffer();
        
        HydroCell.SuspendedSediments lowestSSediments = cellToUpdate.getSedimentMap();
        HydroCell.SuspendedSediments toUpdateSSediments = lowestCell.getSedimentMap();
        
        lowestSSediments.applyBuffer();
        toUpdateSSediments.applyBuffer();
        
        // ADD WATER AT THIS STEP... maybe
        
        if (lowestCell != cellToUpdate && cellToUpdate.hasOcean()) {

            lowestHeight = lowestCell.getHeight();
            curCellHeight = cellToUpdate.getHeight();

            // Move the water
            differenceHeight = (curCellHeight - lowestHeight) / 2.5f;
            curCellHeight = cellToUpdate.getHeight() / 2.5f;
            lowestHeight = lowestCell.getHeight() / 2.5f;

            differenceHeight = clamp(differenceHeight, -lowestHeight, curCellHeight);

            displacedMass = calcMass(differenceHeight, Planet.self().getBase(), OCEAN);

            toUpdateWaterBuffer.transferWater(-displacedMass);
            lowestHydroBuffer.transferWater(displacedMass);

            // Erosion/Deposition
            lowestHeight = lowestCell.getHeightWithoutOceans();
            curCellHeight = cellToUpdate.getHeightWithoutOceans();
            diffGeoHeight = curCellHeight - lowestHeight;

            if (cellToUpdate.getOceanMass() <= oceanSedimentCapacity) {

                float angle, velocity, slope;
                GeoCell.SedimentBuffer sedimentBuffer = cellToUpdate.getSedimentBuffer();
                // Erosion
                angle = (float) Math.atan(diffGeoHeight / Planet.self().getSqrtBase());
                slope = Math.max((float) Math.sin(angle), minAngle);

                totalMass = cellToUpdate.getOceanMass() * slope;

                if (sedimentBuffer.getSediments() <= 10) {
                    
                    velocity = cellToUpdate.erode(totalMass);
                    
                    velocity += sedimentBuffer.getSediments();
                    sedimentBuffer.removeAllSediments();
                } else {
                    velocity = -sedimentBuffer.updateSurfaceSedimentMass(-totalMass);
                }
                lowestSSediments.transferSediment(velocity);
                toUpdateSSediments.transferSediment(-velocity);
            }

            // Only evaporate if in oceans. Will probably be removed later.
            else if (cellToUpdate.getOceanMass() > oceanSedimentCapacity) {
                // Evaporate Water
//                toUpdateWaterBuffer.transferWater(-evapScale);
            }
        }
    }
    
}
