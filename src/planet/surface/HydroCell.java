 
package planet.surface;
import planet.Planet;
import planet.util.TBuffer;
import static planet.surface.Layer.OCEAN;

/**
 * A HydroCell represents the hydrosphere of the planet. The class contains
 * information about the amount of water that exists on the surface of this
 * cell.
 * 
 * @author Richard DeSilvey
 */
public class HydroCell extends GeoCell {

    public final static int MAX_WATER_DEPTH_INDEX       = 100;
    public static int depthIndexRatio                   = 2000 / MAX_WATER_DEPTH_INDEX;
    
    public static int rainProb = 1000;
    public static float rainScale = 2.5f;
    
    /**
     * The amount of water that will continue to hold any sediments. All
     * sediments are dumped if the ocean mass reaches this capacity.
     */
    public static float oceanSedimentCapacity;

    public static float evapScale = 2.5f;

    public static float MIN_ANGLE = 0.0002f;
    
    /**
     * Buffer when moving water to other cells
     */
    public final class WaterBuffer extends TBuffer {

        private float mass;
        
        public WaterBuffer(){
            super();
        }
        
        @Override
        protected final void init() {
            mass = 0;
        }
        
        public void transferWater(float amount) {

            if (!waterBuffer.bufferSet()) {
                waterBuffer.bufferSet(true);
            }

            waterBuffer.mass += amount;

        }

        public void applyWaterBuffer(){
            if (waterBuffer.bufferSet()) {
                addOceanMass(waterBuffer.mass);

                if (mass < 0) {
                    mass = 0;
                }

                waterBuffer.resetBuffer();
            }
        }
        
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

        public void applyBuffer() {

            if (bufferSet()) {

                float cap = (getOceanMass() * .25f);
                SedimentBuffer eb = getSedimentBuffer();
                if (getOceanMass() < oceanSedimentCapacity) {

                    if (sediments > cap) {
                        float diff = sediments - cap;
                        eb.updateSurfaceSedimentMass(diff);
                        sediments = cap;
                    }
                } else {
                    eb.updateSurfaceSedimentMass(sediments);
                    sediments = 0;
                }

            }

        }
    }
    
    private WaterBuffer waterBuffer;
    private SuspendedSediments sedimentMap;
    private float mass;
    
    public HydroCell(int x, int y) {
        super(x, y);
        mass = 0;
        waterBuffer = new WaterBuffer();
        sedimentMap = new SuspendedSediments();
    }
    
    public WaterBuffer getWaterBuffer() {
        return waterBuffer;
    }

    public SuspendedSediments getSedimentMap() {
        return sedimentMap;
    }
    
    public void addOceanMass(float m){
        mass += m;
        if (mass < 0) mass = 0;
    }
    
    public void setOceanMass(float m){
        mass = m;
    }
    
    public float getOceanMass() {
        return mass;
    }
    
    public float getOceanVolume(){
        return mass / OCEAN.getDensity();
    }
    
    public float getOceanHeight() {
        return getOceanVolume() / Planet.self().getBase();
    }
    
    public int getRenderIndex(int settings) {
        
        
        int index = (int) (getOceanMass() / depthIndexRatio);
                
        return index < MAX_WATER_DEPTH_INDEX ? index : MAX_WATER_DEPTH_INDEX - 1;
        
    }

}
