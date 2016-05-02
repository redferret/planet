

package planet.mantel;

import java.awt.Color;
import java.util.List;
import planet.generics.Cell;
import planet.util.Tools;

/**
 * The mantel is below the crust, heat and magma that build up to critical
 * points below the crust cause volcanoes to erupt.
 * @author Richard DeSilvey
 */
public class Mantel extends Cell {

    public static float criticalTemperature;
    private float temperature;
    private static Integer[][] heatMap;
    
    static {
        Color[] colors = {new Color(95, 0, 15), new Color(255, 45, 45), new Color(250, 250, 0)};
        heatMap = Tools.constructSamples(colors, 100);
        criticalTemperature = 1200;
    }
    
    public Mantel(int x, int y) {
        super(x, y);
        
        temperature = 0;
    }

    public void addHeat(float amount){
        temperature += amount;
    }
    
    public void cool(){
        temperature--;
    }
    
    public float getMantelTemperature() {
        return temperature;
    }
    
    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        
        int index = (int) (temperature / 12);
        settings.add(heatMap[index]);
        
        return settings;
    }

}
