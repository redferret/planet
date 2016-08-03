package worlds.planet.surface;

import worlds.planet.cells.HydroCell;
import worlds.planet.cells.HydroCell.ErosionBuffer;
import engine.util.Task;
import engine.util.TaskAdapter;
import engine.util.TaskFactory;

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
        produceTasks(new UpdateOceansFactory());
    }

    /**
     * Add a uniformed layer on the whole surface.
     *
     * @param amount The amount being added
     */
    public void addWaterToAllCells(float amount) {
        int count = getTotalNumberOfCells();
        for (int i = 0; i < count; i++) {
            getCellAt(i).addOceanMass(amount);
        }
    }

    public void updateOceans(int x, int y) {
        HydroCell toUpdate = getCellAt(x, y);
        ErosionBuffer erosionBuffer = toUpdate.getErosionBuffer();

        erosionBuffer.update();
    }

    private class UpdateOceansFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new UpdateOceansTask();
        }
        
        private class UpdateOceansTask extends TaskAdapter {
            @Override
            public void perform(int x, int y) {
                updateOceans(x, y);
            }

            @Override
            public void before() {
            }

            @Override
            public void after() {
            }
        }
    }

}
