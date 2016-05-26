 
package planet.cells;

import java.awt.Color;
import java.util.List;
import planet.Planet;
import planet.util.TBuffer;
import planet.surface.Hydrosphere;
import static planet.util.Tools.calcMass;
import static planet.util.Tools.clamp;
import static planet.util.Tools.constructGradient;
import static planet.util.Tools.getLowestCellFrom;
import static planet.enums.Layer.OCEAN;

/**
 * A HydroCell represents the hydrosphere of the planet. The class contains
 * information about the amount of water that exists on the surface of this
 * cell.
 * 
 * @author Richard DeSilvey
 */
public class HydroCell extends GeoCell {

    public final static int MAX_WATER_DEPTH_INDEX = 50;
    public static int depthIndexRatio = 21000 / MAX_WATER_DEPTH_INDEX;

    public static float evapScale;

    /**
     * The cross-sectional area of each virtual pipe in the water pipeline
     * model.
     */
    public static float crossSectionalArea;
    /**
     * The percentage of water that can dissolve sediments.
     */
    public static float sedimentCapacity;
    public static float minAngle;
    private static Integer[][] oceanMap;
    
    /**
     * The water pipeline model for the movement of water.
     */
    public final class WaterPipeline extends TBuffer {

        private float bufferedMass;
        
        public WaterPipeline(){
            super();
        }
        
        public void update(){

            HydroCell cellToUpdate, lowestCell;

            float lowestHeight, curCellHeight, displacedMass,
                    diffGeoHeight, differenceHeight, totalMass;

            cellToUpdate = (HydroCell) thisCell();
            lowestCell = (HydroCell) getLowestCellFrom(cellToUpdate);

            if (lowestCell == null || cellToUpdate == null) {
                return;
            }

            WaterPipeline toUpdateWaterBuffer = cellToUpdate.getWaterPipeline();
            WaterPipeline lowestHydroBuffer = lowestCell.getWaterPipeline();

            toUpdateWaterBuffer.applyBuffer();
            lowestHydroBuffer.applyBuffer();

            HydroCell.SuspendedSediments lowestSSediments = cellToUpdate.getSedimentMap();
            HydroCell.SuspendedSediments toUpdateSSediments = lowestCell.getSedimentMap();

            lowestSSediments.applyBuffer();
            toUpdateSSediments.applyBuffer();

            if (lowestCell != cellToUpdate && cellToUpdate.hasOcean()) {

                lowestHeight = lowestCell.getHeight();
                curCellHeight = cellToUpdate.getHeight();

                // Move the water
                differenceHeight = (curCellHeight - lowestHeight) / 2.5f;
                curCellHeight = cellToUpdate.getHeight() / 2.5f;
                lowestHeight = lowestCell.getHeight() / 2.5f;

                differenceHeight = clamp(differenceHeight, -lowestHeight, curCellHeight);

                displacedMass = calcMass(differenceHeight, Planet.self().getCellArea(), OCEAN);

                toUpdateWaterBuffer.transferWater(-displacedMass);
                lowestHydroBuffer.transferWater(displacedMass);
                
                double theta = Math.atan(differenceHeight / Planet.self().getCellLength());
                float pressure = (float) Math.sin(theta);
                
            }

        }

        public void transferWater(float amount){
            bufferedMass += amount;
        }
        
        @Override
        protected void init() {
            bufferedMass = 0;
        }

        @Override
        public void applyBuffer() {
            addOceanMass(bufferedMass);
            resetBuffer();
        }
        
    }
    
    private Cell thisCell(){
        return this;
    }
    
    /**
     * Buffer when moving sediments to other cells
     */
    public final class SuspendedSediments extends TBuffer {
        
        private float sediments;
        
        public SuspendedSediments(){
            super();
        }
        
        @Override
        protected final void init(){
            sediments = 0;
        }
       
        public void transferSediment(float amount) {
            if (!bufferSet()) {
                bufferSet(true);
            }

            if (amount > 0) {
                sediments += amount;
            }
        }

        public float getSediments() {
            return sediments;
        }

        private float getCap(){
            return (getOceanMass() * sedimentCapacity);
        }
        
        public boolean atCapacity(){
            float cap = getCap();
            return sediments >= cap;
        }
        
        public void applyBuffer() {
            if (bufferSet()) {
                SedimentBuffer eb = getSedimentBuffer();
                eb.updateSurfaceSedimentMass(sediments);
                resetBuffer();
            }
        }
    }
    
    static {
        Color colors[] = {new Color(0, 0, 0, 0), new Color(153, 204, 255, 128), new Color(0, 102, 255, 192),
                        new Color(0, 0, 153, 255)};
        
        float[] dist = {0.04f, 0.36f, 0.68f, 1f};
        oceanMap = constructGradient(colors, dist, MAX_WATER_DEPTH_INDEX);
        
        evapScale = 2.5f;
        sedimentCapacity = 0.25f;
        minAngle = 0.0002f;
        crossSectionalArea = Planet.self().getCellArea();
    }
    
    private WaterPipeline waterPipeline;
    private SuspendedSediments sedimentMap;
    private float oceanMass;
    
    public HydroCell(int x, int y) {
        super(x, y);
        
        oceanMass = 0;
        waterPipeline = new WaterPipeline();
        sedimentMap = new SuspendedSediments();
    }
    
    public WaterPipeline getWaterPipeline() {
        return waterPipeline;
    }

    public SuspendedSediments getSedimentMap() {
        return sedimentMap;
    }
    
    public void addOceanMass(float m){
        oceanMass += m;
        if (oceanMass < 0) oceanMass = 0;
    }
    
    public void setOceanMass(float m){
        oceanMass = m;
    }
    
    public float getOceanMass() {
        return oceanMass;
    }
    
    public float getOceanVolume(){
        return oceanMass / OCEAN.getDensity();
    }
    
    public float getOceanHeight() {
        return getOceanVolume() / Planet.self().getCellArea();
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        
        if (Hydrosphere.drawOcean){
            int index = (int) (getOceanMass() / depthIndexRatio);

            int setting = index < MAX_WATER_DEPTH_INDEX ? index : MAX_WATER_DEPTH_INDEX - 1;

            settings.add(oceanMap[setting]);
        }
        return super.render(settings);
        
    }

}
