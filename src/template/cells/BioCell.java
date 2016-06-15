

package template.cells;

import java.util.List;

/**
 * The Biosphere is represented by this cell. The biosphere includes all life
 * forms and their impact on the environment.
 * @author Richard DeSilvey
 */
public class BioCell extends HydroCell {

    public BioCell(int x, int y) {
        super(x, y);
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }
    
}
