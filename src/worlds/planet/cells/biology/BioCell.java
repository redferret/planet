

package worlds.planet.cells.biology;

import java.util.List;
import worlds.planet.cells.HydroCell;

/**
 * The Biosphere is represented by this cell. The biosphere includes all life
 * forms and their impact on the environment.
 * @author Richard DeSilvey
 */
public class BioCell extends HydroCell {

    private BioSurface bioSurface;
    
    
    public BioCell(int x, int y) {
        super(x, y);
        bioSurface = new BioSurface(this);
    }
    
    public void updateBiology(){
        bioSurface.update();
    }

    public BioSurface getBioSurface() {
        return bioSurface;
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        
        return super.render(settings);
    }
    
}

