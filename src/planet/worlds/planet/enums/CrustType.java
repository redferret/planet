
package planet.worlds.planet.enums;

/**
 * The crust type is given by determining if the crust will subduct and
 * if it is continental or oceanic crust.
 * 
 * @author Richard
 */
public enum CrustType {
    
    /**
     * Oceanic crust that does subduct
     */
    OCEANIC_SUBDUCT (0,     0.0f,   true),
    
    /**
     * Oceanic crust that does not subduct
     */
    OCEANIC         (1,     0.0f,   false),
    
    /**
     * Continental crust. This crust folds rather than
     * getting deleted.
     */
    CONTINENTAL     (2,     0.25f,  false);
    
    /**
     * The type of crust. Oceanic or Continental
     */
    private int type;
    
    /**
     * The amount of land folding that takes place based on the height of
     * the crust.
     */
    private float foldRatio;
    
    /**
     * Whether this crust will subduct.
     */
    private boolean subduct;
    
    private CrustType(int type, float foldRatio, boolean subduct){
        
        this.type = type;
        this.subduct = subduct;
        this.foldRatio = foldRatio;
        
    }

    public float getFoldRatio() {
        return foldRatio;
    }
    
    public int getType() {
        return type;
    }

    public boolean isSubduct() {
        return subduct;
    }
    
}
