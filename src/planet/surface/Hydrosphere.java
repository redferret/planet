package planet.surface;

/**
 * The hydrosphere is everything that deals with rivers, lakes, seas, and
 * oceans.
 *
 * @author Richard DeSilvey
 */
public abstract class Hydrosphere extends Geosphere {

    public static boolean drawOcean;

    static {
        drawOcean = true;
    }

    public Hydrosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
    }

    /**
     * Add a uniformed layer on the whole surface.
     *
     * @param amount The amount being added
     */
    public void addWaterToAllCells(float amount) {
        for (int x = 0; x < worldSize; x++) {
            for (int y = 0; y < worldSize; y++) {
                getCellAt(x, y).addOceanMass(amount);
            }
        }
    }

    public void updateOceans(int x, int y) {
        moveWater(x, y);
        erode(x, y);
    }

    /**
     * Updates the water surface and velocity field.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    private void moveWater(int x, int y) {

    }

    /**
     * Erodes sediments and rock and transfers it.
     *
     * @param x The x coordinate
     * @param y The y coordinate
     */
    private void erode(int x, int y) {

    }

}
