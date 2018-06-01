package worlds.planet;

import java.util.List;
import worlds.planet.geosphere.GeoCell;

/**
 *
 * @author Richard DeSilvey
 */
public class PlanetCell extends GeoCell {

  public static int area, length;

  static {
    length = 1;
    area = 1;
  }

  public PlanetCell() {
    this(0, 0);
  }

  public PlanetCell(int x, int y) {
    super(x, y);
  }

  public List<Integer[]> render(List<Integer[]> settings) {
    return super.render(settings);
  }

}
