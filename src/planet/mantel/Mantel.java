

package planet.mantel;

import java.util.List;
import planet.generics.Cell;

/**
 *
 * @author Richard DeSilvey
 */
public class Mantel extends Cell {

    public Mantel(int x, int y) {
        super(x, y);
    }

    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        return settings;
    }

}
