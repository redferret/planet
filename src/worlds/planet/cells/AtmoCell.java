

package worlds.planet.cells;

import java.util.List;
import engine.util.TBuffer;
import java.util.ArrayList;
import worlds.planet.enums.Gases;
import worlds.planet.surface.Gas;


/**
 * The atmosphere is represented by the AtmoCell. Climate and weather are 
 * controlled by this class. 
 * @author Richard DeSilvey
 */
public class AtmoCell extends BioCell {

    public class AirBuffer extends TBuffer {

        private float waterVapor;
        
        public AirBuffer(){
            super();
        }
        
        public void addWater(float amount){
            if (amount < 0){
                throw new IllegalArgumentException("Amount must be positive");
            }
            waterVapor += amount;
        }
        
        public float getWaterVapor(){
            return waterVapor;
        }
        
        @Override
        protected void init() {
            waterVapor = 0;
        }

        @Override
        public void applyBuffer() {
            getErosionBuffer().transferWater(waterVapor);
            resetBuffer();
        }
        
    }
    
    private AirBuffer airBuffer;
    
    private float temperature;
    
    private Gas waterVapor;
    
    public AtmoCell(int x, int y) {
        super(x, y);
        airBuffer = new AirBuffer();
        waterVapor = new Gas(Gases.WaterVapor);
    }

    public AirBuffer getAirBuffer(){
        return airBuffer;
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }
}
