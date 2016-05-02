

package planet.mantel;

import java.awt.Color;
import java.util.List;
import planet.generics.Cell;
import planet.util.Tools;

/**
 *
 * @author Richard DeSilvey
 */
public class Mantel extends Cell {

    private float temperature;
    private static Integer[][] heatMap;
    
    static {
        Color[] colors = {new Color(95, 0, 15), new Color(255, 45, 45), new Color(250, 250, 0)};
        heatMap = Tools.constructSamples(colors, 100);
    }
    
    public Mantel(int x, int y) {
        super(x, y);
        
        temperature = 0;
    }

    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        
        int index = (int) (temperature / 10);
        settings.add(heatMap[index]);
        
        return settings;
    }

}
