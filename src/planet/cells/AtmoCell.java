

package planet.cells;

import java.util.List;
import planet.util.TBuffer;


/**
 * The atmosphere is represented by the AtmoCell. Climate and weather are 
 * controlled by this class. 
 * @author Richard DeSilvey
 */
public class AtmoCell extends BioCell {

    public class AirBuffer extends TBuffer {

        private float moisture;
        
        public AirBuffer(){
            super();
        }
        
        public void addWater(float amount){
            if (amount < 0){
                throw new IllegalArgumentException("Amount must be positive");
            }
            moisture += amount;
        }
        
        @Override
        protected void init() {
            moisture = 0;
        }

        @Override
        public void applyBuffer() {
            getWaterPipeline().transferWater(moisture);
            resetBuffer();
        }
        
    }
    
    private AirBuffer airBuffer;
    
    public AtmoCell(int x, int y) {
        super(x, y);
        airBuffer = new AirBuffer();
    }

    public AirBuffer getAirBuffer(){
        return airBuffer;
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }
}
