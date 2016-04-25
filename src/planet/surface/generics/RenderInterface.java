
package planet.surface.generics;


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
     * @return The index that maps onto the render lookup array
     */
    public int getCellRenderIndex(int x, int y);
    
    /**
     * The render look up is an array of Java Objects and can be used to 
     * pass information about how to render the map's data. The data
     * look up value is used as a way to determine which object is
     * used for rendering.
     * 
     * @return The render data for this lookup.
     */
    public Object[] renderLookup();
    
}
