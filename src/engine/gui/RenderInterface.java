
package engine.gui;

import java.util.List;


/**
 * The interface allows each map to implement these special methods
 * for rendering itself. The cell data index is the data given by a cell,
 * this method is already created and used by the SurfaceMap, the 
 * renderLookup returns an array of Objects that should contain
 * information on how to render a specific cell based on the cell
 * data index which maps onto the Object array. This is typically
 * used for height maps or maps that use data to map onto an array 
 * of colors.
 * 
 * @author Richard DeSilvey
 */
public interface RenderInterface {
    
    /**
     * The data lookup value is used as an indexer to a data array that 
     * should hold information about rendering a cell for this map
     * 
     * @param x The x coordinate
     * @param y The y coordinate
     * @return The indexes that map onto the render lookup array
     */
    public List<Integer[]> getCellData(int x, int y);
    
}
