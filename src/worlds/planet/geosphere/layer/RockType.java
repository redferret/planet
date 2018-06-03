package worlds.planet.geosphere.layer;

/**
 *
 * @author Richard
 */
public enum RockType {

  /**
   * Sand, gravel, soil.
   */
  SEDIMENT("Sediment"),
  /**
   * Shale, sandstone.
   */
  SEDIMENTARY("Sedimentary"),
  /**
   * No other sub-types yet.
   */
  METAMORPHIC("Metamorphic"),
  /**
   * Basalt, granite.
   */
  IGNEOUS("Igneous");

  static {
    RockType types[] = RockType.values();
    int typeValue = 0;
    for (RockType rockType : types) {
      rockType.typeValue = typeValue++;
    }
  }

  private int typeValue;
  private final String name;

  private RockType(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public int getTypeValue() {
    return typeValue;
  }

}
