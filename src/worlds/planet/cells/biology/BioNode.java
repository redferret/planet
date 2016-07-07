

package worlds.planet.cells.biology;

import engine.cells.Cell;
import java.util.List;

/**
 * Contains life forms
 * @author Richard DeSilvey
 */
public class BioNode extends Cell {

    //private FoodChain decLife; /*The food chain for animal life*/
    //private FoodChain plantLife; /*The plant and bacterial food chain*/
    
    public BioNode(int x, int y) {
        super(x, y);
    }

    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        return settings;
    }

}
