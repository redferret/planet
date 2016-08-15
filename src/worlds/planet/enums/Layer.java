package worlds.planet.enums;

import java.awt.Color;

/**
 * Describes what type of layer a stratum is. The layer describes density,
 * color, the name (String), an erosion factor, an ID, and a rock type.
 *
 * @author Richard DeSilvey
 */
public enum Layer {

    /**
     * Sand is formed when gravel breaks down, also from other rock types like
     * sedimentary rocks.
     */
    SEDIMENT("Sand", new Color(240, 228, 176), 0, 0.00f, 1850f),
    /**
     * Soil is formed when life interacts with Sand.
     */
    SOIL("Soil", new Color(90, 56, 37), 1, 0.05f, 1920f),
    /**
     * Sedimentary rock forms by compressing sediments past a certain depth.
     * They are the second easiest to erode next to sediments.
     */
    SANDSTONE("Sandstone", new Color(255, 201, 14), 3, 0.12f, 2350f),
    /**
     * Shale is formed when clay and silt sized particles are compacted
     * together.
     */
    SHALE("Shale", new Color(200, 190, 230), 4, 0.14f, 2400f),
    /**
     * Limestone is composed of skeletal fragments of marine organisms such as
     * coral biological life deposits this rock.
     */
    LIMESTONE("Limestone", new Color(226, 245, 170), 5, 0.15f, 2550f),
    /**
     * Metamorphic rock forms at fault boundaries when rock subducts and get
     * twisted and compressed or if enough sedimentary rock is at a particular
     * depth to form metamorphic rock. Metamorphic rock also takes the longest
     * to erode.
     */
    METAMORPHIC("Metamorphic", new Color(136, 0, 20), 6, 0.22f, 3100f),
    /**
     * Basalt is usually deposited at fault lines or certain volcanoes.
     */
    BASALT("Basalt", new Color(127, 127, 127), 7, 0.18f, 2990f),
    /**
     * Granite can be from certain volcanoes but mainly intrusive.
     */
    GRANITE("Granite", new Color(193, 120, 102), 8, 0.20f, 2790f),
    /**
     * Sea water is represented as a top layer. The average density of sea water
     * is around 1.05 cc. Sea water deposits shale rock.
     */
    OCEAN("Liquid Water", Color.BLUE, 9, 0.0f, 1050f),
    /**
     * Lava, not magma, is represented by this layer. It's average density is
     * 2.75 cc. lava can erode or deposit. If the lava is hot enough it will
     * carve out rock. If the lava is cool enough it deposits basalt.
     */
    LAVA("Lava", Color.RED, 10, 0.0f, 2750f),
    /**
     * Ice is represented by this layer and has a density of 0.9 cc. Ice erodes
     * rock and deposits gravel instead of sand. The gravel is one layer below
     * ice and is only formed when ice moves.
     */
    ICE("Ice", new Color(230, 248, 248), 11, 0.0f, 900f);

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

    /* Hidden Constructor */
    private Layer(String name, Color color, int typeid,
            float erosionFactor, float density) {
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
