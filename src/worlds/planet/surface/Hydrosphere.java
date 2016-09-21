package worlds.planet.surface;


import worlds.planet.cells.hydrology.HydroCell.ErosionBuffer;
import engine.util.task.Task;
import engine.util.task.TaskAdapter;
import engine.util.task.TaskFactory;
import worlds.planet.cells.PlanetCell;

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
            PlanetCell cell = waitForCellAt(i);
            cell.addOceanMass(amount);
            release(cell);
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
                PlanetCell toUpdate = waitForCellAt(x, y);
                ErosionBuffer erosionBuffer = toUpdate.getErosionBuffer();
                erosionBuffer.updateFlow();
                erosionBuffer.updateVelocityField();
                release(toUpdate);
            }

            @Override
            public void after() {
            }
        }
    }

}
