package worlds.planet.enums;

/**
 *
 * @author Richard
 */
public enum RockType {
    
    /**
     * Sand, gravel, soil.
     */
    SEDIMENT        ("Sediment", 0),
    /**
     * Shale, sandstone.
     */
    SEDIMENTARY     ("Sedimentary", 1),
    /**
     * No other sub-types yet.
     */
    METAMORPHIC     ("Metamorphic", 2),
    /**
     * Basalt, granite.
     */
    IGNEOUS         ("Igneous", 3),
    /**
     * Lava, water, rain, ice.
     */
    NULL            ("Null", -1);
    
    private int typeValue;
    
    private String name;
    
    private RockType(String name, int typeValue){
        
        this.name = name;
        this.typeValue = typeValue;
        
    }

    public String getName() {
        return name;
    }

    public int getTypeValue() {
        return typeValue;
    }
    
}
