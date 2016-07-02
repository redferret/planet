

package worlds.planet.cells;

import java.util.List;
import engine.util.TBuffer;
import java.util.ArrayList;
import java.util.stream.Stream;
import worlds.planet.enums.Gases;
import worlds.planet.surface.Gas;


/**
 * The atmosphere is represented by the AtmoCell. Climate and weather are 
 * controlled by this class. 
 * @author Richard DeSilvey
 */
public class AtmoCell extends BioCell {

    /**
     * The height of each AirLayer on the surface. This calculation is based
     * on the maximum altitude for the planet up to the mesosphere.
     */
    private static float airLayerThickness;
    
    public static final int LAYER_COUNT = 2;
    
    static {
        airLayerThickness = Gases.MAX_ALTITUDE / LAYER_COUNT;
    }
    
    /**
     * This class is used to represent the layers in an Atmosphere.
     */
    public class AirLayer {

        private float temperature;
        private Gas waterVapor;

        public class AirBuffer extends TBuffer {

            private float waterVapor;

            public AirBuffer() {
                super();
            }

            public void addWater(float amount) {
                if (amount < 0) {
                    throw new IllegalArgumentException("Amount must be positive");
                }
                waterVapor += amount;
            }

            public float getWaterVapor() {
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

        public AirLayer() {
            airBuffer = new AirBuffer();
            this.temperature = 0;
            this.waterVapor = new Gas(Gases.WaterVapor);
        }

        public float getWaterVapor() {
            return waterVapor.getMolarMass();
        }
        
        public void addWater(float amount){
            waterVapor.setMass(waterVapor.getMass() + amount);
            if (waterVapor.getMass() < 0){
                waterVapor.setMass(0);
            }
        }
        
        public AirBuffer getAirBuffer() {
            return airBuffer;
        }

    }

    private List<AirLayer> airLayers;
    
    public AtmoCell(int x, int y) {
        super(x, y);
        airLayers = new ArrayList<>();
        setupLayers();
    }

    private void setupLayers() {
        for (int l = 0; l < LAYER_COUNT; l++){
            airLayers.add(new AirLayer());
        }
    }

    /**
     * Gets the layers as a Stream
     * @return The layers as a Stream
     */
    public Stream<AirLayer> getLayers(){
        return airLayers.stream();
    }
    
    /**
     * Calculates the average temperature of all the layers at this cell.
     * @return The average temperature for this cell.
     */
    public float getAverageTemp(){
        
        float avgTemp = 0;
        
        for(AirLayer layer : airLayers){
            avgTemp += layer.temperature;
        }
        
        return avgTemp / airLayers.size();
    }
    
    public static float getAirLayerThickness() {
        return airLayerThickness;
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }
}
