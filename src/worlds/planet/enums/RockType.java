package worlds.planet.enums;

/**
 *
 * @author Richard
 */
public enum RockType {
    
    /**
     * Sand, gravel, soil.
     */
    SEDIMENT        ("Sediment"),
    /**
     * Shale, sandstone.
     */
    SEDIMENTARY     ("Sedimentary"),
    /**
     * No other sub-types yet.
     */
    METAMORPHIC     ("Metamorphic"),
    /**
     * Basalt, granite.
     */
    IGNEOUS         ("Igneous"),
    /**
     * Lava, water, rain, ice.
     */
    FLUID            ("Fluid"),
    
    MOLTENROCK      ("Molten Rock");
    
    static {
        RockType types[] = RockType.values();
        int typeValue = 0;
        for (RockType rockType : types){
            rockType.typeValue = typeValue++;
        }
    }
    
    private int typeValue;
    private String name;
    
    private RockType(String name){
        
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
