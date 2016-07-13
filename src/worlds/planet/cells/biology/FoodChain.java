

package worlds.planet.cells.biology;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Richard DeSilvey
 */
public class FoodChain {

    private List<LifeForm> chain;
    
    public FoodChain() {
        chain = new ArrayList<>();
    }
    
    public boolean containsLife(){
        return !chain.isEmpty();
    }
}
