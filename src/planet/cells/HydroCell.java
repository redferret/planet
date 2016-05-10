 
package planet.cells;
import java.awt.Color;
import java.util.List;
import planet.Planet;
import planet.util.TBuffer;
import static planet.enums.Layer.OCEAN;
import planet.surface.Hydrosphere;
import planet.util.Tools;

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

    /**
     * The amount of water that will continue to hold any sediments. All
     * sediments are dumped if the ocean mass reaches this capacity.
     */
    public static float oceanSedimentCapacity;

    public static float evapScale;

    /**
     * The percentage of water that can dissolve sediments.
     */
    public static float sedimentCapacity;
    public static float minAngle;
    private static Integer[][] oceanMap;
    
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

            if (!bufferSet()) {
                bufferSet(true);
            }

            mass += amount;

        }

        public void applyWaterBuffer(){
            if (bufferSet()) {
                addOceanMass(mass);
                resetBuffer();
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

        private float getCap(){
            return (getOceanMass() * sedimentCapacity);
        }
        
        public boolean atCapacity(){
            float cap = getCap();
            return sediments >= cap;
        }
        
        private boolean oceanIsDeep(){
            return getOceanMass() >= oceanSedimentCapacity;
        }
        
        public void applyBuffer() {

            if (bufferSet()) {

                float cap = getCap();
                SedimentBuffer eb = getSedimentBuffer();
                if (!oceanIsDeep()) {
                    if (atCapacity()){
                        float overflow = sediments - cap;
                        eb.updateSurfaceSedimentMass(overflow);
                        sediments = cap;
                    }
                } else {
                    eb.updateSurfaceSedimentMass(sediments);
                    sediments = 0;
                }

            }

        }
    }
    
    static {
        Color colors[] = {new Color(0, 0, 0, 0), new Color(153, 204, 255, 128), new Color(0, 102, 255, 192),
                        new Color(0, 0, 153, 255)};
        
        float[] dist = {0.04f, 0.36f, 0.68f, 1f};
        oceanMap = Tools.constructGradient(colors, dist, MAX_WATER_DEPTH_INDEX);
        
        oceanSedimentCapacity = 50;
        evapScale = 2.5f;
        sedimentCapacity = 0.25f;
        minAngle = 0.0002f;
    }
    
    private WaterBuffer waterBuffer;
    private SuspendedSediments sedimentMap;
    private float oceanMass;
    
    public HydroCell(int x, int y) {
        super(x, y);
        
        oceanMass = 0;
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
        return getOceanVolume() / Planet.self().getBase();
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
