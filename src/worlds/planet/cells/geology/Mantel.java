

package worlds.planet.cells.geology;

import java.awt.Color;
import java.util.List;
import java.util.Random;
import engine.surface.Cell;
import engine.util.Tools;

/**
 * The mantel is below the crust, heat and magma that build up to critical
 * points below the crust cause volcanoes to erupt.
 * @author Richard DeSilvey
 */
public class Mantel extends Cell {

    public static float criticalTemperature;
    private static Integer[][] heatMap;
    private static Random rand;
    
    private float temperature;
    
    static {
        Color[] colors = {new Color(95, 0, 15), new Color(255, 45, 45), new Color(250, 250, 0)};
        heatMap = Tools.constructSamples(colors, 100);
        criticalTemperature = 1200;
        rand = new Random();
    }
    
    public Mantel(int x, int y) {
        super(x, y);
        
        temperature = rand.nextInt(1200);
    }

    public void addHeat(float amount){
        temperature += amount;
    }
    
    public void cool(float amount){
        temperature -= amount;
        if (temperature < 0) temperature = 0;
    }
    
    public boolean checkExtrusive(){
        if (temperature >= (criticalTemperature * .75f)){
            return rand.nextInt(100) < 75;
        }
        return false;
    }
    
    public boolean checkIntrusive(){
        if (temperature >= (criticalTemperature * 7/8)){
            return rand.nextInt(100) < 25;
        }
        return false;
    }
    
    public float getMantelTemperature() {
        return temperature;
    }
    
    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        
        if (temperature > criticalTemperature) temperature = criticalTemperature;
        
        int index = (int) (temperature / 12);
        index = index >= heatMap.length? index - 1 : index < 0? 0 : index;
        settings.add(heatMap[index]);
        
        return settings;
    }

}
