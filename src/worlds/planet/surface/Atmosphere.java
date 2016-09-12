package worlds.planet.surface;

import worlds.planet.cells.atmosphere.Gas;
import worlds.planet.cells.PlanetCell;
import engine.util.Delay;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import worlds.planet.Planet;
import worlds.planet.cells.atmosphere.AtmoCell;
import worlds.planet.enums.GasType;

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

    private class SimpleClimateFactory implements TaskFactory {
        
        @Override
        public Task buildTask() {
            return new SimpleClimateTask();
        }

        private class SimpleClimateTask extends Task {

            private Delay delay;
            private boolean evaporate;
            private float totalEvaportatedMass;
            
            public SimpleClimateTask() {
                delay = new Delay(10);
                totalEvaportatedMass = 0;
                evaporate = true;
            }

            @Override
            public void before() {
            }
            
            @Override
            public void perform(int x, int y) {
            	PlanetCell cell = getCellAt(x, y);
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
