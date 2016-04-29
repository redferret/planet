

package planet.surface;

import java.util.List;


/**
 * The atmosphere is represented by the AtmoCell. Climate and weather are 
 * controlled by this class. 
 * @author Richard DeSilvey
 */
public class AtmoCell extends BioCell {

    public AtmoCell(int x, int y) {
        super(x, y);
    }

    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }
}
