

package worlds.planet.cells.biology;

import engine.surface.SurfaceMap;
import engine.util.Boundaries;
import engine.util.Task;
import engine.util.TaskManager;
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
        bioSurface = new BioSurface();
    }
    
    public void updateBiology(){
        bioSurface.update();
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        
        return super.render(settings);
    }
    
}

