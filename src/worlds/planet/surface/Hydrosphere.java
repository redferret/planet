package worlds.planet.surface;

import worlds.planet.cells.HydroCell;
import worlds.planet.cells.HydroCell.ErosionBuffer;
import engine.util.task.Task;
import engine.util.task.TaskAdapter;
import engine.util.task.TaskFactory;
import worlds.planet.Planet;

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

    private class UpdateOceansFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new UpdateOceansTask();
        }
        
        private class UpdateOceansTask extends TaskAdapter {
            
            @Override
            public void before() {
            }
            
            @Override
            public void perform(int x, int y) {
                HydroCell toUpdate = getCellAt(x, y);
                ErosionBuffer erosionBuffer = toUpdate.getErosionBuffer();

                    erosionBuffer.update();
                }

            @Override
            public void after() {
            }
        }
    }

}
