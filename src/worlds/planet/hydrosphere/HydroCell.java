 
package worlds.planet.hydrosphere;

import worlds.planet.PlanetCell;
import worlds.planet.geosphere.GeoCell;

import java.awt.Color;
import java.util.List;

import worlds.planet.enums.Layer;
import engine.util.TBuffer;
import static engine.util.Tools.calcMass;
import static engine.util.Tools.clamp;
import static engine.util.Tools.constructGradient;
import static engine.util.Tools.getLowestCellFrom;
import static worlds.planet.enums.Layer.OCEAN;
import static worlds.planet.Planet.instance;
import static worlds.planet.enums.SilicateContent.*;

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
     * The percentage of water that can dissolve sediments.
     */
    public static float sedimentCapacity;
    public static float minAngle;
    private static Integer[][] oceanMap;
    
    /**
     * The buffer performs movement of water as well as erosion.
     */
    public final class ErosionBuffer extends TBuffer {

        private float bufferedMass;
        
        public ErosionBuffer(){
            super();
        }
        
        public void updateFlow(){
            
        }
        
        public void updateVelocityField(){
            
        }
        
        
        private void update(){

            HydroCell lowestCell;

            float lowestHeight, curCellHeight, displacedMass, differenceHeight;
            int area;

            lowestCell = (HydroCell) getLowestCellFrom((PlanetCell) HydroCell.this);

            if (lowestCell == null) {
                return;
            }
            SedimentBuffer sb = getSedimentBuffer();
            
            ErosionBuffer toUpdateWaterBuffer = getErosionBuffer();
            ErosionBuffer lowestHydroBuffer = lowestCell.getErosionBuffer();

            toUpdateWaterBuffer.applyBuffer();
            lowestHydroBuffer.applyBuffer();

            SuspendedSediments lowestSSediments = lowestCell.getSuspendedSedimentBuffer();
            SuspendedSediments toUpdateSSediments = getSuspendedSedimentBuffer();

            lowestSSediments.applyBuffer();
            toUpdateSSediments.applyBuffer();

            if (lowestCell != HydroCell.this && hasOcean()) {

                lowestHeight = lowestCell.getHeight();
                curCellHeight = getHeight();

                // Move the water
                differenceHeight = (curCellHeight - lowestHeight) / 2f;
                curCellHeight /= 2f;
                lowestHeight /= 2f;

                differenceHeight = clamp(differenceHeight, -lowestHeight, curCellHeight);
                area = PlanetCell.cellArea;
                displacedMass = calcMass(differenceHeight, area, OCEAN);

                toUpdateWaterBuffer.transferWater(-displacedMass);
                lowestHydroBuffer.transferWater(displacedMass);
                
                double theta = Math.atan(differenceHeight / PlanetCell.cellLength);
                float angle = (float) Math.sin(theta);
                float pressure = (float) Math.min(minAngle, angle);

                // Move suspended sediments based on angle to lowest cell.
                float movedSeds = toUpdateSSediments.getSediments() * angle;
                Layer sedimentType = toUpdateSSediments.sedimentType;
                lowestSSediments.transferSediment(sedimentType, movedSeds);
                toUpdateSSediments.transferSediment(sedimentType, -movedSeds);

                float erosion = (displacedMass * pressure);
                sedimentType = sb.getSedimentType();

                if (sedimentType != null) {

                    Layer rockType = peekTopStratum().getLayer();

                    if (rockType.getSilicates() == Rich) {
                        sedimentType = Layer.FELSIC;
                    } else if (rockType.getSilicates() == Mix) {
                        sedimentType = Layer.MIX;
                    } else {
                        sedimentType = Layer.MAFIC;
                    }

                    float erodedSeds = erode(erosion);
                    toUpdateSSediments.transferSediment(sedimentType, erodedSeds);
                    
                }
                
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
    
    
    /**
     * Buffer is used for moving sediments to other cells. The buffer is
     * used to hold sediments in the water as they move down stream.
     */
    public final class SuspendedSediments extends TBuffer {
        
        private float sediments;
        private Layer sedimentType;
        
        public SuspendedSediments(){
            super();
        }
        
        @Override
        protected final void init(){
            sediments = 0;
        }
       
        public void transferSediment(Layer type, float amount) {
            if (!bufferSet()) {
                bufferSet(true);
            }
            if (sedimentType == null){
                sedimentType = type;
            }else if (sedimentType != type){
                sedimentType = Layer.MIX;
            }
            sediments += amount;
        }

        public float getSediments() {
            return sediments;
        }

        private float getCap(){
            return (getOceanMass() * sedimentCapacity);
        }
        
        public boolean atCapacity(){
            float cap = getCap();
            return sediments > cap;
        }
        
        public void applyBuffer() {
            SedimentBuffer eb = getSedimentBuffer();
            if (atCapacity()){
                float diff = sediments - getCap();
                eb.transferSediment(sedimentType, diff);
                sediments = getCap();
            }
        }
    }
    
    static {
        Color colors[] = {new Color(0, 0, 0, 0), new Color(153, 204, 255, 128), new Color(0, 102, 255, 192),
                        new Color(0, 0, 153, 255)};
        
        float[] dist = {0.04f, 0.36f, 0.68f, 1f};
        oceanMap = constructGradient(colors, dist, MAX_WATER_DEPTH_INDEX);
        
        evapScale = 2.5f;
        sedimentCapacity = 0.50f;
        minAngle = 0.02f;
    }
    
    private ErosionBuffer erosionBuffer;
    private SuspendedSediments suspendedSediments;
    private float oceanMass;
    
    public HydroCell(int x, int y) {
        super(x, y);
        
        oceanMass = 0f;
        erosionBuffer = new ErosionBuffer();
        suspendedSediments = new SuspendedSediments();
    }
    
    public ErosionBuffer getErosionBuffer() {
        return erosionBuffer;
    }

    public SuspendedSediments getSuspendedSedimentBuffer() {
        return suspendedSediments;
    }
    
    /**
     * Adds or subtracts water from this cell. The returned value is the 
     * actual amount removed or added.
     * @param amount Positive amounts add, negative amounts subtract.
     * @return The amount that was removed or added.
     */
    public float addOceanMass(float amount){
        if (oceanMass + amount < 0) {
            float temp = oceanMass;
            oceanMass = 0;
            return -temp;
        }else {
            oceanMass += amount;
            return amount;
        }
    }
    
    public void setOceanMass(float m){
        oceanMass = m;
    }
    
    public float getOceanMass() {
        return oceanMass;
    }
    
    public float getOceanVolume(){
        return getOceanMass() / OCEAN.getDensity();
    }
    
    public float getOceanHeight() {
        return getOceanVolume() / PlanetCell.cellArea;
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        
        if (Hydrosphere.drawOcean && hasOcean()){
            int index = (int) (getOceanMass() / depthIndexRatio);

            int setting = index < MAX_WATER_DEPTH_INDEX ? index : MAX_WATER_DEPTH_INDEX - 1;

            settings.add(oceanMap[setting]);
        }
        return super.render(settings);
        
    }

}
