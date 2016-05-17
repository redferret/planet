package planet.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import planet.Planet;
import static planet.Planet.TimeScale.Geological;
import planet.surface.PlanetSurface;
import planet.surface.Surface;

/**
 * A surface can be broken up into sections where a SurfaceThread can modify and
 * control that section.
 *
 * @author Richard DeSilvey
 */
public class SurfaceThread extends MThread {

    private float absLowestHeight;
    private AtomicInteger lowestHeightIntPart;
    private AtomicInteger lowestHeightDecPart;

    /**
     * Lower bounds are inclusive, upper bounds are exclusive
     */
    protected Boundaries bounds;
    private int curFrame;
    
    private static final boolean CONTINUOUS = true;
    
    /**
     * Constructs a new SurfaceThread.
     *
     * @param delay The amount of time to delay each frame in milliseconds
     * @param bounds The surface boundaries
     * @param name The name of this thread
     */
    public SurfaceThread(int delay, Boundaries bounds, String name) {
        super(delay, name, CONTINUOUS);

        this.bounds = bounds;
        curFrame = 0;
        lowestHeightIntPart = new AtomicInteger(Integer.MAX_VALUE);
        lowestHeightDecPart = new AtomicInteger(Integer.MAX_VALUE);
        absLowestHeight = Integer.MAX_VALUE;
    }
    
    /**
     * Each time the thread posts an update this method is called following a
     * post-update call to postUpdate()
     */
    public final void update() {

        Surface surface = Planet.self().getSurface();

        boolean sw = (curFrame % 2) == 0;
        int m;
        int lowerYBound = bounds.getLowerYBound();
        int upperYBound = bounds.getUpperYBound();
        int lowerXBound = bounds.getLowerXBound();
        int upperXBound = bounds.getUpperXBound();

        int ystart = sw ? lowerYBound : (upperYBound - 1);
        int yinc = sw ? 1 : -1;

        try {
            for (int b = 0; b < 2; b++) {
                for (int y = ystart; (sw ? (y < upperYBound) : (y >= 0)); y += yinc) {

                    m = ((b > 0) && (y % 2 == 0)) ? 1
                            : ((b > 0) && (y % 2 != 0) ? -1 : 0);

                    for (int x = ((y % 2) + m) + lowerXBound; x < upperXBound; x += 2) {
                        surface.partialUpdate(x, y);
                        updateMinimumHeight(x, y, (PlanetSurface) surface);
                    }
                }
            }

            surface.fullUpdate();
            
        } catch (Exception e) {
            Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE,
                    "An exception occured when updating the surface: {0}", getName());
            e.printStackTrace();
        }
        curFrame++;

        absLowestHeight = absLowestHeight < 0 ? 0 : absLowestHeight;
        int intPart = (int) absLowestHeight;
        int decPart = (int) ((absLowestHeight - intPart) * 10);

        lowestHeightIntPart.set(intPart);
        lowestHeightDecPart.set(decPart);

        absLowestHeight = Integer.MAX_VALUE;
    }

    public float getPreviousLowestHeight() {

        float decPart = lowestHeightDecPart.get() / 10f;
        decPart = lowestHeightIntPart.get() + decPart;

        return decPart;
    }

    private void updateMinimumHeight(int x, int y, PlanetSurface surface) {
        float cellHeight = surface.getCellAt(x, y).getHeightWithoutOceans();

        if (cellHeight < absLowestHeight) {
            absLowestHeight = cellHeight;
        }
    }

}
