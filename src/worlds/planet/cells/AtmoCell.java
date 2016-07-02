

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
        airLayers.add(new AirLayer());
        airLayers.add(new AirLayer());
        airLayerThickness = Gases.MAX_ALTITUDE / airLayers.size();
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
        return 0;
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        return super.render(settings);
    }
}
