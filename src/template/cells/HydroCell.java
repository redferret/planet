 
package template.cells;

import java.awt.Color;
import java.util.List;
import worlds.planet.Planet;
import worlds.planet.surface.Hydrosphere;
import static planet.util.Tools.constructGradient;
import static worlds.planet.enums.Layer.OCEAN;

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
    
    static {
        Color colors[] = {new Color(0, 0, 0, 0), new Color(153, 204, 255, 128), new Color(0, 102, 255, 192),
                        new Color(0, 0, 153, 255)};
        
        float[] dist = {0.04f, 0.36f, 0.68f, 1f};
        oceanMap = constructGradient(colors, dist, MAX_WATER_DEPTH_INDEX);
        
        evapScale = 2.5f;
        sedimentCapacity = 0.50f;
        minAngle = 1.5f;
    }
    private float oceanMass;
    
    public HydroCell(int x, int y) {
        super(x, y);
        
        oceanMass = 0;
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
            return temp;
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
        return oceanMass / OCEAN.getDensity();
    }
    
    public float getOceanHeight() {
        return getOceanVolume() / Planet.self().getCellArea();
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
