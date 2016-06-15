package worlds.planet.surface;

import worlds.planet.cells.PlanetCell;
import planet.util.Delay;
import planet.util.Task;
import planet.util.TaskFactory;
import planet.util.Tools;

/**
 *
 * @author Richard DeSilvey
 */
public class Atmosphere extends Hydrosphere {

    public Atmosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        produceTasks(new EvaporateFactory());
    }

    private class EvaporateFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new EvaporateTask();
        }

        private class EvaporateTask implements Task {

            private Delay delay;

            public EvaporateTask() {
                delay = new Delay(1);
            }

            @Override
            public void perform(int x, int y) {
                if (y != 0) {
                    PlanetCell cell = getCellAt(x, y);
                    float w = getGridWidth();
                    float h = w / 2;
                    float rate = 0;

                    rate = calcLatitudeRate(y, h, w);

                    float amount = 15 * rate;
                    amount = cell.addOceanMass(-amount);

                    int rx = random.nextInt(getGridWidth());
                    int ry = random.nextInt(getGridWidth());

                    amount /= 4f;
                    getCellAt(Tools.checkBounds(rx + 1, getGridWidth()), ry).addOceanMass(amount);
                    getCellAt(Tools.checkBounds(rx - 1, getGridWidth()), ry).addOceanMass(amount);

                    getCellAt(rx, Tools.checkBounds(ry + 1, getGridWidth())).addOceanMass(amount);
                    getCellAt(rx, Tools.checkBounds(ry - 1, getGridWidth())).addOceanMass(amount);
                }
            }

            private float calcLatitudeRate(int y, float h, float w) {
                if (0 <= y && y < h) {
                    return y / h;
                } else {
                    return (w - y) / h;
                }
            }

            @Override
            public boolean check() {
                return delay.check() && !PlanetSurface.suppressAtmosphere;
            }

        }

    }

}
