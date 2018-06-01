package worlds.planet.geosphere;

import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import engine.util.Vec2;
import java.util.Set;
import worlds.planet.Util;
import worlds.planet.Planet;
import worlds.planet.PlanetCell;
import worlds.planet.PlanetSurface;

import java.util.concurrent.ConcurrentLinkedDeque;

import static worlds.planet.Planet.instance;
import static worlds.planet.Surface.*;

/**
 * A GeoCell is a Cell representing land Geologically. The cell contains strata
 * and specialized methods for adding and/or removing from the strata.
 *
 * @author Richard DeSilvey
 */
public class GeoCell extends Mantle {

  /**
   * The list of strata for this cell
   */
  private Deque<Layer> strata;

  /**
   * Tracks the total thickness of the strata.
   */
  private float totalStrataThickness;

  /**
   * The total height makes adding up each layer faster. Each time a layer
   * is removed or it's thickness is altered the totalMass is updated. The units
   * are in kilograms.
   */
  private float totalMass;
  
  /**
   * The total volume is calculated each time layer is added or removed or
   * updated and is used to determine the average density of this cell in cubic
   * meters.
   */
  private float totalVolume;

  /**
   * The amount of this cell that is currently submerged in the mantel.
   */
  private float curAmountSubmerged;

  private float crustTemperature;
  
  /**
   * A Point that is represented as the velocity for Plate Tectonics. When a
   * plate collides with a sibling (Cell owned by the same plate) the collision
   * is inelastic and will reduce it's velocity as well as moving a little
   * bit of it's energy through the system.
   */
  private Vec2 velocity;

  private static Integer[][] heightMap;

  public final static int MAX_HEIGHT_INDEX = 17;
  /**
   * The ratio for indexing onto the height map array, by taking a cell height
   * and dividing it by this value will give the proper index to the height map.
   */
  public static int heightIndexRatio = 17 / MAX_HEIGHT_INDEX;

  static {
    Color[] heightColors = {new Color(255, 255, 204), new Color(51, 153, 51),
      new Color(157, 166, 175), new Color(255, 255, 255)};
    
    heightMap = Util.constructSamples(heightColors, MAX_HEIGHT_INDEX);
  }

  /**
   * Constructs a new GeoCell at the location (x, y) with the parent surface map
   * provided.
   *
   * @param x The x coordinate
   * @param y The y coordinate
   */
  public GeoCell(int x, int y) {
    super(x, y);
    setup();
  }

  private void setup() {

    strata = new ConcurrentLinkedDeque<>();
    velocity = new Vec2(0, 0);

    totalStrataThickness = 0f;
    totalMass = 0f;
    totalVolume = 0f;
    curAmountSubmerged = 0f;
    crustTemperature = 0;
  }

  public void setVelocity(Vec2 vel) {
    velocity = new Vec2(vel);
  }

  public Vec2 getVelocity() {
    return velocity;
  }

  public void addCrustTemperature(float rate) {
    crustTemperature += rate;
    if (crustTemperature < -273) {
      crustTemperature = -273;
    }
  }

  public float getCrustTemperature() {
    return crustTemperature;
  }
  
  /**
   * Creates a deep copy of this GeoCell and it's strata.
   *
   * @return The copy of this GeoCell.
   */
  public GeoCell copy() {

    GeoCell copy = new GeoCell(getX(), getY());
    Deque<Layer> copyStrata = new ConcurrentLinkedDeque<>();

    strata.forEach(layer -> {
      copyStrata.add(layer.copy());
    });
    copy.strata = copyStrata;

    return copy;
  }

  /**
   * Adds the given strata to the top of the existing strata for this cell. The
   * strata being added will have it's elements popped off and then pushed onto
   * the top of the strata for this cell.
   *
   * @param strata The strata being added
   */
  public void addStrata(Deque<Layer> strata) {
    while (strata != null && !strata.isEmpty()) {
      pushRockLayer(strata.pop());
    }
  }

  public Deque<Layer> removeAllStrata() {
    Deque<Layer> removed = removeStrata(getStrataThickness());
    totalStrataThickness = 0f;
    totalMass = 0f;
    totalVolume = 0f;
    return removed;
  }

  /**
   * Removes the strata based on the depth given in meters. The removed strata
   * will be in reversed order in respects to the strata of this cell.
   *
   * @param atDepth
   * @return
   */
  public Deque<Layer> removeStrata(float atDepth) {

    Layer splitter = new Layer();
    addAtDepth(splitter, atDepth);

    Deque<Layer> removedStrata = new LinkedList<>();
    Layer layer = removeTopRockLayer();
    while (layer != null && layer != splitter) {
      if (layer != splitter) {
        removedStrata.push(layer);
      }
      layer = removeTopRockLayer();
    }
    removeTopRockLayer();

    return removedStrata;
  }

  /**
   * The density is an average based on the types of layer that exist in the
   * strata. The amount of sea water also effects the average density.
   *
   * @return The average density of this cell as grams per liter.
   */
  public float getDensity() {

    float oceanMass = 0;//((HydroCell) this).getOceanMass();
    float oceanVolume = 0;//oceanMass / OCEAN.getDensity();

    float totalMassWithOceans = (getTotalMass() + oceanMass);
    float totalVolumeWithOceans = (getTotalVolume() + oceanVolume);

    return (totalVolumeWithOceans == 0)? 0 : totalMassWithOceans / totalVolumeWithOceans;
  }

  /**
   * Gets the density of this cell without the ocean, if one exists.
   *
   * @return The density of this cell's rock without ocean density considered
   */
  public float getGeoDensity() {
    float tv = getTotalVolume();
    return (tv == 0) ? 0 : getTotalMass() / tv;
  }

  public float getTotalMass() {
    return totalMass;
  }

  public float getTotalVolume() {
    return totalVolume;
  }

  /**
   * Adds the given amount and type at the given depth starting from the top and
   * working down the strata.
   *
   * @param rockToAdd
   * @param depth
   */
  public void addAtDepth(Layer rockToAdd, float depth) {
    float currentDepth = 0;
    Layer selectedRockLayer;
    Deque<Layer> workingStrata = new LinkedList<>();

    while (peekTopRockLayer() != null) {

      selectedRockLayer = removeTopRockLayer();
      float rockLayerThickness = selectedRockLayer.getThickness();
      Layer selectedType = selectedRockLayer;

      currentDepth += rockLayerThickness;

      if (currentDepth < depth) {
        float diff = depth - currentDepth;
        float diffInMass = Util.calcMass(diff, PlanetCell.area, selectedType.getDensity());
        Set<Material> removedMaterials = selectedRockLayer.removeMaterial(-diffInMass);
        Layer splitRockLayer = new Layer(removedMaterials);
        workingStrata.push(selectedRockLayer);
        workingStrata.push(rockToAdd.copy());
        workingStrata.push(splitRockLayer);
        break;
      } else {
        workingStrata.push(selectedRockLayer);
      }

      if (peekTopRockLayer() == null && currentDepth >= depth) {
        workingStrata.push(rockToAdd);
        break;
      }
    }

    while (!workingStrata.isEmpty()) {
      pushRockLayer(workingStrata.removeFirst());
    }
  }


  /**
   * Adds a new layer if a type is specified.
   *
   * <p>
   * Consider the cases below when calling this method</p>
   * <ul>
   * <li>If the type is null then the top or bottom layer receives the amount
   * being added. Checking for existing strata is important before calling this
   * method since negative amounts are allowed but not recommended unless the
   * top or bottom layer mass is greater or equal to the amount being removed,
   * <code>placeAmount()</code> helps to accomplish this.
   * </li>
   * <li>If the type is null then the top or bottom is added or subtracted</li>
   * </ul>
   *
   * @param rockBeingAdded
   * @param toTop Adding to the top or bottom of the strata.
   */
  public void addToStrata(Layer rockBeingAdded, boolean toTop) {
    if (toTop) {
      pushRockLayer(rockBeingAdded);
    } else {
      appendRockLayer(rockBeingAdded);
    }
  }

  /**
   * Updates the total mass and volume for this cell with the given mass and
   * layer type (density).
   *
   */
  private void updateMV(Layer rock) {

    if (rock == null) {
      throw new IllegalArgumentException("The layer can't be null");
    }
    float mass = rock.getMass();
    totalStrataThickness = Math.max(0, totalStrataThickness + Util.calcHeight(mass, PlanetCell.area, rock.getDensity()));
    totalMass = Math.max(0, totalMass + mass);
    totalVolume = Math.max(0, totalVolume + (mass / rock.getDensity()));
  }

  public float getSpecificHeat() {
    float sp = 0;
    sp = strata.stream()
            .map((layer) -> layer.getSpecificHeat())
            .reduce(sp, (accumulator, _item) -> accumulator + _item);
    return sp / strata.size();
  }
  
  /**
   * Adds a new layer layer to the strata, if the layer is null nothing will
   * happen.
   *
   * @param layer The layer being added
   */
  public void pushRockLayer(Layer layer) {

    if (layer != null) {
      layer.setBottom(strata.peek());
      if (!strata.isEmpty() && strata.peek() != null) {
        strata.peek().setTop(layer);
      }
      strata.push(layer);
      updateMV(layer);
    }
  }

  /**
   * Adds to the bottom of the strata.
   *
   * @param layer The layer being appended to this cell.
   */
  public void appendRockLayer(Layer layer) {

    if (layer != null) {
      Layer bottom = peekBottomRockLayer();
      if (bottom != null) {
        bottom.setBottom(layer);
        layer.setTop(bottom);
      }
      strata.addLast(layer);
      updateMV(layer);
    }
  }

  /**
   * Removes the top layer from this cell. The bottom reference of the cell
   * being removed is also nullified after removal.
   *
   * @return the layer being removed from the top, null if there are no strata
   * to be removed.
   */
  public Layer removeTopRockLayer() {

    if (strata.peek() == null) {
      return null;
    }
    Layer removed = strata.removeFirst();
    removed.removeBottom();
    if (strata.peek() != null) {
      strata.peek().removeTop();
    }

    return updateRemoved(removed);
  }

  /**
   * Removes the bottom layer from this cell. The bottom reference shouldn't
   * matter since it is the lowest layer in the strata.
   *
   * @return the layer being removed from the bottom, null if there are no
   * strata to be removed.
   */
  public Layer removeBottomRockLayer() {

    Layer removed = strata.removeLast();
    Layer bottom = peekBottomRockLayer();

    if (bottom != null) {
      bottom.removeTop();
    }

    Layer nextBottom = peekBottomRockLayer();
    if (nextBottom != null) {
      nextBottom.removeBottom();
    }

    return updateRemoved(removed);
  }

  /**
   * Updates the layer being removed and performs additional clean up
   *
   * @param removed The layer that is being removed.
   * @return The removed layer
   */
  private Layer updateRemoved(Layer removed) {
    if (removed == null) {
      throw new IllegalArgumentException("removed cannot be null");
    }
    updateMV(removed);
    return removed;
  }

  /**
   * Peeks at the top of the strata of this cell but does not remove the top
   * layer.
   *
   * @return The layer at the top of the strata.
   */
  public Layer peekTopRockLayer() {
    return strata.peekFirst();
  }

  /**
   * Peeks at the bottom of the strata of this cell but does not remove the
   * bottom layer.
   *
   * @return The layer at the bottom of the strata.
   */
  public Layer peekBottomRockLayer() {
    return strata.peekLast();
  }

  /**
   * Get the strata list. The index 0 represents the top of the strata
   *
   * @return The list of strata for this cell
   */
  public Deque<Layer> getStrata() {
    return strata;
  }

  /**
   * The height of this cell can be represented as a height with or without
   * seawater. This method subtracts the ocean depth but leaves the average
   * density the same as if the ocean were there but doesn't include the depth
   * of the ocean.
   *
   * @return The height of this cell without ocean depth.
   */
  public float getHeightWithoutOceans() {
    float oceanHeight = 0;//((HydroCell) this).getOceanHeight();
    return hasOcean() ? (getHeight() - oceanHeight) : getHeight();
  }

  /**
   * Fetches the current thickness of all the strata added together. This isn't
   * like the height of the cell where it is calculated from equilibrium and up
   * from the mantel.
   *
   * @return
   */
  public float getStrataThickness() {
    return totalStrataThickness;
  }

  /**
   * The height of this cell is based on the average density of the strata with
   * the ocean depth included. If the timescale is in Geological the height of
   * this cell will be updated to it's equilibrium height.
   *
   * @return The height of this cell with ocean depth included.
   */
  public float getHeight() {

    float cellHeight;
    float oceanVolume = 0;//((HydroCell) this).getOceanVolume();

    cellHeight = (oceanVolume + getTotalVolume()) / PlanetCell.area;

    if (instance().getTimeScale() == Planet.TimeScale.Geological) {
      recalculateHeight();
    }

    return cellHeight - curAmountSubmerged;

  }

  /**
   * Shifts the height of this cell to it's equilibrium height. This method is
   * called while the simulation is in the Geological timescale and the
   * <code>getThickness()</code> method is called.
   */
  public void recalculateHeight() {
    float cellHeight, amountSubmerged, density = getDensity();
    float oceanVolume = 0;//((HydroCell) this).getOceanVolume();

    cellHeight = (oceanVolume + getTotalVolume()) / PlanetCell.area;
    float factor = mantleDensityFactor();
    amountSubmerged = cellHeight * density / (Mantle.mantle_density * factor);

    curAmountSubmerged = amountSubmerged;
  }

  public boolean hasOcean() {
    return false;//((HydroCell) this).getOceanMass() > 0;
  }

  @Override
  public synchronized List<Integer[]> render(List<Integer[]> settings) {
    int setting;
    PlanetSurface surface = (PlanetSurface) instance().getSurface();
    switch (surface.displaySetting) {
      case HEIGHTMAP:
        float thisHeight = getHeightWithoutOceans() * 1000;
        float height = Math.max(0, thisHeight - surface.getLowestHeight());
        setting = (int) Math.min((height / heightIndexRatio), MAX_HEIGHT_INDEX - 1);
        settings.add(heightMap[setting]);

        return super.render(settings);

      
      default: // The display setting is not listed
        return super.render(settings);
    }
  }
}
