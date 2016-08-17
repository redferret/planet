package worlds.planet.enums;

import java.awt.Color;
import static worlds.planet.enums.SilicateContent.*;
import static worlds.planet.enums.RockType.*;

/**
 * Describes what type of layer a stratum is. The layer describes density,
 * color, the name (String), an erosion factor, an ID, and a rock type.
 *
 * @author Richard DeSilvey
 */
public enum Layer {

    MAFIC("Sediments that are derived from silicate poor igneous rock", 
            new Color(200, 200, 180), SEDIMENT, Poor, 0.00f, 1990f),
    
    MIX("Sediments that contain an intermidate amount of silicate", 
            new Color(236, 220, 165), SEDIMENT, Mix, 0, 1850f),
    
    FELSIC("Sediments that are derived from silicate rich rock", 
            new Color(245, 240, 207), SEDIMENT, Rich, 0, 1750f),
    
    /**
     * Soil is formed when life interacts with sediments and contains biomass.
     */
    SOIL("Soil", new Color(90, 56, 37), SEDIMENT, Mix, 0.05f, 1920f),
    
    /**
     * Sandstones that are derived from silicate poor sediments,
     */
    MAFIC_SANDSTONE("Sandstone composed mostly of mafic sediments", 
            new Color(255, 201, 14).darker(), SEDIMENTARY, Poor, 0.10f, 2350f),
    
    /**
     * Sandstones that are a mix between poor and rich silicate sediments.
     */
    MIX_SANDSTONE("Sandstone intermediate silicates", 
            new Color(255, 201, 14), SEDIMENTARY, Mix, 0.10f, 2350f),
    
    /**
     * Sandstones that are silicate rich, when metamorphisized Felsic sandstone
     * will form Quartzite.
     */
    FELSIC_SANDSTONE("Sandstone composed mostly of felsic sediments", 
            new Color(255, 201, 14).brighter(), SEDIMENTARY, Rich, 0.10f, 2350f),
    
    /**
     * Shale is formed when clay and silt sized particles are compacted
     * together.
     */
    SHALE("Shale", new Color(200, 190, 230), SEDIMENTARY, Mix, 0.13f, 2400f),
    
    /**
     * Limestone is composed of skeletal fragments of marine organisms such as
     * coral biological life deposits this rock.
     */
    LIMESTONE("Limestone", new Color(226, 245, 170), SEDIMENTARY, Poor, 0.15f, 2550f),
    
    /**
     * Silicate rich sandstones that are metamorphisized.
     */
    QUARTZITE("Sandstones that are metamorphisized", 
            new Color(255, 208, 175), METAMORPHIC, Rich, 0.33f, 2645f),
    
    /**
     * Shale that is metamorphisized
     */
    SLATE("", new Color(147,147,202), METAMORPHIC, Mix, 0.25f, 2750f),
    
    /**
     * Limestone that is metamorphisized
     */
    MARBLE("Limestone that is metamorphisized is turned into Marble", 
            new Color(220, 168, 163), METAMORPHIC, Poor, 0.19f, 2711f),
    
    PHYLITE("First stage of metamorphism, very small crystals", 
            new Color(64, 128, 128), METAMORPHIC, Mix, 0.28f, 2900f),
    
    /**
     * First stage of foliated metamorphisized rock.
     */
    SCHIST("Second stage of metamorphism, small crystals", 
            new Color(125, 130, 145), METAMORPHIC, Mix, 0.30f, 3000f),
    
    /**
     * Second stage of foliated metamorphisized rock of schist or volcanic rocks.
     */
    GNEISS("Last stage of metamorphism, large crystals", 
            new Color(136, 0, 20), METAMORPHIC, Mix, 0.33f, 3100f),
    
    
    /**
     * Basalt is usually deposited at fault lines in the ocean or some hot
     * spots like Hawaii, usually extrusive.
     */
    BASALT("Basalt, Silicate Poor Extrusive Igneous", 
            new Color(70, 70, 70), IGNEOUS, Poor, 0.17f, 2990f),
    /**
     * Rhyolite is from explosive extrusive volcanoes that are silicate rich, 
     * usually extrusive
     */
    RHYOLITE("Rhyolite, Silicate Rich Extrusive Igneous", 
            new Color(190, 126, 126), IGNEOUS, Rich, 0.25f, 2690f),
    
    /**
     * Silicate intermediate igneous rock, usually extrusive
     */
    ANDESITE("Andesite, Silicate Intermediate Extrusive Igneous", 
            new Color(205, 180, 165), IGNEOUS, Mix, 0.2f, 2790f),
    
    /**
     * A silicate poor intrusive igneous rock that cooled slowly.
     */
    GABBRO("Gabbro, Silicate Poor Intrusive Igneous", 
            new Color(140, 140, 140), IGNEOUS, Poor, 0.19f, 3200f),
    
    /**
     * A Silicate intermediate intrusive igneous rock that cooled slowly.
     */
    DIORITE("Diorite, Silicate Intermediate Intrusive Igneous", 
            new Color(220, 220, 220), IGNEOUS, Mix, 0.23f, 2990f),
    
    /**
     * A silicate rich intrusive igneous rock that cooled slowly.
     */
    GRANITE("Granite, Silicate Rich Intrusive Igneous", 
            new Color(190, 126, 126), IGNEOUS, Rich, 0.28f, 2750f),
    
    /**
     * Volcanic glass formed when molten rock cools quickly, considered as 
     * a silicate intermediate.
     */
    OBSIDIAN("Obsidian, Igneous rock that cooled fast", 
            new Color(70, 0, 70), IGNEOUS, Mix, 0.23f, 2600f),
    /**
     * Sea water is represented as a top layer. The average density of sea water
     * is around 1.05 cc. Sea water deposits shale rock.
     */
    OCEAN("Liquid Water", Color.BLUE, FLUID, None, 0.0f, 1050f),
    /**
     * Lava, or magma, is represented by this layer. Depending on how it is
     * deposited, intrusive or extrusive will form different kinds of igneous
     * rock.
     */
    MAFICMOLTENROCK("Molten rock that forms igneous deposits", 
            Color.RED.darker(), FLUID, Poor, 0.0f, 2750f),
    
    MOLTENROCK("Molten rock that forms igneous deposits", 
            Color.RED, FLUID, Mix, 0.0f, 2750f),
    
    FELSICMOLTENROCK("Molten rock that forms igneous deposits", 
            Color.RED.brighter(), FLUID, Rich, 0.0f, 2750f),
    /**
     * Ice is represented by this layer and has a density of 0.9 cc. Ice erodes
     * rock and deposits gravel instead of sand. The gravel is one layer below
     * ice and is only formed when ice moves.
     */
    ICE("Ice", new Color(230, 248, 248), FLUID, None, 0.0f, 900f);

    /**
     * The name of the material this stratum is. This includes Sediment,
     * Sedimentary, Igneous, and Metamorphic.
     */
    private String name;

    /**
     * The type id for this stratum.
     */
    private int typeid;

    /**
     * A multiplier for erosion.
     */
    private float erosionFactor;

    /**
     * The density of the layer type, this is based on an average for that type.
     * The units are in grams per liter.
     */
    private float density;

    /**
     * The displayed color this layer should be on the screen.
     */
    private Color color;

    private SilicateContent silicates;
    
    private RockType rockType;
    
    static {
        Layer layers[] = Layer.values();
        int index = 0;
        for (Layer layer : layers){
            layer.typeid = index++;
        }
    }
    
    /* Hidden Constructor */
    private Layer(String name, Color color, RockType rockType, SilicateContent silicates,
            float erosionFactor, float density) {
        this.silicates = silicates;
        this.rockType = rockType;
        this.color = color;
        this.typeid = typeid;
        this.name = name;
        this.density = density;
        this.erosionFactor = erosionFactor;
    }

    public void setErosionFactor(float f) {
        erosionFactor = f;
    }

    /**
     * The density is a set value for the type of rock this layer is. The units
     * are measured in kilograms per cubic meter.
     *
     * @return The density of this layer
     */
    public float getDensity() {
        return density;
    }

    /**
     * The name is used to display on the GUI the type of rock/layer this is.
     *
     * @return The name of this layer
     */
    public String getName() {
        return name;
    }

    public RockType getRockType() {
        return rockType;
    }
    
    public SilicateContent getSilicates() {
        return silicates;
    }
    
    /**
     * The erosion factor is based on an average and the value is between 1.0f
     * and 0.0f Sand is 0.0f since it is already eroded into sediment. The
     * higher the value the longer it takes to erode.
     *
     * @return The erosion factor for this layer
     */
    public float getErosionFactor() {
        return erosionFactor;
    }

    /**
     * The color the stratum should appear on the UI.
     *
     * @return The color representing this layer
     */
    public Color getColor() {
        return color;
    }

    /**
     * This value is a unique ID for this layer type.
     *
     * @return The layer ID
     */
    public int getID() {
        return typeid;
    }

}
