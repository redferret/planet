package worlds.planet.surface;

import worlds.planet.cells.atmosphere.Gas;
import worlds.planet.cells.PlanetCell;
import engine.util.Delay;
import static engine.util.Tools.changeMass;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import worlds.planet.cells.atmosphere.AtmoCell;
import worlds.planet.cells.geology.GeoCell;
import worlds.planet.enums.GasType;
import worlds.planet.enums.Layer;
import static worlds.planet.enums.SilicateContent.Mix;
import static worlds.planet.enums.SilicateContent.Rich;
import static worlds.planet.surface.Geosphere.windErosionConstant;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class Atmosphere extends Hydrosphere {

    private List<Gas> gases;

    public Atmosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
//        produceTasks(new SimpleClimateFactory());
        gases = new ArrayList<>();
        setupGases();
    }
    
    private void setupGases(){
        for (GasType gas : GasType.values()){
            gases.add(new Gas(gas));
        }
    }

    private class AeolianFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new AeolianTask();
        }

        private class AeolianTask extends Task {

            private Delay delay;

            public void construct() {
                delay = new Delay(150);
            }

            @Override
            public void before() {
            }

            @Override
            public void perform(int x, int y) {
                PlanetCell cell = waitForCellAt(x, y);
                aeolianErosion(cell);
                release(cell);
            }

            public void aeolianErosion(GeoCell spreadFrom) {
                convertTopLayer(spreadFrom);
            }

            public void convertTopLayer(GeoCell spreadFrom) {

                float rockMass, sandMass;

                if (spreadFrom.peekTopStratum() == null) {
                    return;
                }

                Layer rockType = spreadFrom.peekTopStratum().getLayer();

                GeoCell.SedimentBuffer eb = spreadFrom.getSedimentBuffer();
                // Wind erosion
                if (eb.getSediments() == 0 && !spreadFrom.hasOcean()
                        && spreadFrom.getMoltenRockLayer().getMoltenRockFromSurface() < 1000) {

                    Layer sedimentType;
                    if (rockType.getSilicates() == Rich) {
                        sedimentType = Layer.FELSIC;
                    } else if (rockType.getSilicates() == Mix) {
                        sedimentType = Layer.MIX;
                    } else {
                        sedimentType = Layer.MAFIC;
                    }

                    rockMass = spreadFrom.erode(windErosionConstant);
                    sandMass = changeMass(rockMass, rockType, sedimentType);

                    eb.transferSediment(sedimentType, sandMass);
                }
            }

            @Override
            public boolean check() {
                return true;
            }

            @Override
            public void after() {
            }

        }

    }
    
    private class SimpleClimateFactory implements TaskFactory {
        
        @Override
        public Task buildTask() {
            return new SimpleClimateTask();
        }

        private class SimpleClimateTask extends Task {

            private Delay delay;
            private boolean evaporate;
            private float totalEvaportatedMass;
            
            public void construct() {
                delay = new Delay(10);
                totalEvaportatedMass = 0;
                evaporate = true;
            }

            @Override
            public void before() {
            }
            
            @Override
            public void perform(int x, int y) {
            	PlanetCell cell = waitForCellAt(x, y);
                if (evaporate){
                    float amount = 32;
                    Stream<AtmoCell.AirLayer> layers = cell.getAirLayersStream();
                    layers.forEach(layer -> {
                        totalEvaportatedMass += layer.getAirBuffer().getWaterVapor();
                        layer.getAirBuffer().resetBuffer();
                    });
                    if (cell.hasOcean()){
                        float returnValue = -cell.addOceanMass(-amount);
                        totalEvaportatedMass += returnValue;
                    }
                }else{
                    float rate = totalEvaportatedMass / getTotalNumberOfCells();
                    cell.addOceanMass(rate);
                }
                release(cell);
            }

            /*
             * Calculates a rate based on latitude.
             */
            private float getRate(PlanetCell cell){
                int y = cell.getY();
                float w = getGridWidth();
                float h = w / 2;

                return (0 <= y && y < h) ? y / h : (w - y) / h;
            }

            @Override
            public boolean check() {
                return delay.check() && !PlanetSurface.suppressAtmosphere;
            }
            
            @Override
            public void after() {
                if (!evaporate){
                    totalEvaportatedMass = 0;
                }
                evaporate = !evaporate;
            }
        }

    }

}
