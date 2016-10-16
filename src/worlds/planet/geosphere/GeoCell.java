package worlds.planet.geosphere;

import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import engine.util.Point;
import engine.util.TBuffer;
import engine.util.Tools;
import worlds.planet.Planet;
import worlds.planet.hydrosphere.HydroCell;
import worlds.planet.enums.CrustType;
import worlds.planet.enums.Layer;
import worlds.planet.Surface;
import worlds.planet.geosphere.Geosphere;
import worlds.planet.PlanetSurface;
import worlds.planet.enums.RockType;
import static engine.util.Tools.*;
import engine.util.concurrent.SurfaceThread;

import java.util.concurrent.ConcurrentLinkedDeque;

import static worlds.planet.Planet.instance;
import static worlds.planet.Surface.*;
import static worlds.planet.enums.Layer.*;

/**
 * A GeoCell is a Cell representing land Geologically. The cell contains
 * strata and specialized methods for adding and/or removing from the strata.
 *
 * @author Richard DeSilvey
 */
public class GeoCell extends Mantel {

    /**
     * A single class buffer part of a transfer map
     */
    public final class SedimentBuffer extends TBuffer {

        /**
         * Pending pendingSediments to be added
         */
        private float pendingSediments;

        /**
         * Represents the amount of Sediments on the surface of this cell.
         */
        private float totalSediments;

        private Layer sedimentType;
        
        public SedimentBuffer() {
            super();
            sedimentType = null;
        }

        protected final void init() {
            pendingSediments = 0;
        }

        /**
         * During erosion, pendingSediments need to be transfered to other
         * cells, this acts as a sediment map where changes will be applied
         * later after erosion occurs.
         *
         * @param type The sediment type being added, this changes the 
         * composition of the sediment from Mafic to Felsic
         * @param amount The amount to transfer in kilograms
         */
        public void transferSediment(Layer type, float amount) {
            if (!bufferSet()) {
                bufferSet(true);
            }
            
            if (sedimentType == null){
                sedimentType = type;
            }else if (sedimentType != type){
                sedimentType = Layer.MIX;
            }
            pendingSediments += amount;
        }

        /**
         * Apply the changes that need to be made to this cell, if the buffer is
         * not set nothing will happen.
         */
        public void applyBuffer() {
            if (bufferSet()) {
                updateSurfaceSedimentMass(pendingSediments);
                resetBuffer();
            }
        }

        /**
         * Removes all sediments and returns the total amount what was here.
         * @return The total amount of sediments.
         */
        public float removeAllSediments() {
            if (sedimentType != null) {
                float temp = totalSediments;
                updateMV(-totalSediments, sedimentType);
                totalSediments = 0;
                sedimentType = null;
                return temp;
            } else {
                return 0;
            }
        }

        /**
         * Adds or removes pendingSediments from this cell.
         *
         * @param mass The amount of pendingSediments in kilograms.
         * @return The amount that was removed or added
         */
        public float updateSurfaceSedimentMass(float mass) {
            if (mass < 0) {
                if ((totalSediments + mass) < 0) {
                    float t = totalSediments;
                    removeAllSediments();
                    return -t;
                }
            }
            totalSediments += mass;
            updateMV(mass, sedimentType);
            return mass;
        }

        /**
         *
         * @return The amount of pendingSediments from this cell in kilograms.
         */
        public float getSediments() {
            return totalSediments;
        }

        public Layer getSedimentType() {
            return sedimentType;
        }

    }

    public final class MoltenRockLayer extends TBuffer {

        /**
         * Represents the amount of molten rock on the surface of this cell.
         */
        private float moltenRockSurfaceMass;

        private Layer moltenRockType;
        
        @Override
        protected void init() {
            moltenRockType = null;
            moltenRockSurfaceMass = 0;
        }

        @Override
        public void applyBuffer() {

        }
        
        /**
         * Adds or removes molten rock from this cell. The total mass and volume
         * of this cell is effected by this addition or subtraction.
         *
         * @param mass The amount of molten rock being added, always positive.
         * @param type
         * @return
         */
        public float putMoltenRockToSurface(float mass, Layer type) {
            if (type.getRockType() != RockType.MOLTENROCK) {
                throw new IllegalArgumentException("Layer type must be of rock type MOLTENROCK");
            }
            moltenRockSurfaceMass += mass;
            if (moltenRockSurfaceMass < 0) {
                float temp = moltenRockSurfaceMass;
                updateMV(moltenRockSurfaceMass, type);
                moltenRockSurfaceMass = 0;
                moltenRockType = null;
                return -temp;
            }

            if (moltenRockType == null) {
                moltenRockType = type;
            } else {
                moltenRockType = Layer.MIXMOLTENROCK;
            }

            updateMV(mass, type);
            return mass < 0 ? -mass : mass;
        }

        public Layer getMoltenRockType() {
            return moltenRockType;
        }

        /**
         * @return The amount of molten rock from this cell in Kilograms.
         */
        public float getMoltenRockFromSurface() {
            return moltenRockSurfaceMass;
        }

        /**
         * Removes all molten rock from this cell.
         *
         * @return The total amount of molten rock that was at this cell.
         */
        public float removeAllMoltenRock() {
            float temp = moltenRockSurfaceMass;
            updateMV(-moltenRockSurfaceMass, MAFICMOLTENROCK);
            moltenRockSurfaceMass = 0;
            return temp;
        }

        
    }
    
    /**
     * A single class buffer part of a transfer map
     */
    private class PlateBuffer extends TBuffer {

        private Deque<Stratum> strata;
        private float totalMass, totalVolume;

        public PlateBuffer() {
            super();
        }

        protected final void init() {
            totalMass = totalVolume = 0;
            if (strata == null) {
                strata = new LinkedList<>();
            } else {
                strata.clear();
            }

        }

        @Override
        public void applyBuffer() {
        }

    }

    /**
     * The list of strata for this cell
     */
    private Deque<Stratum> strata;

    /**
     * Tracks the total thickness of the strata.
     */
    private float totalStrataThickness;
    
    /**
     * When the plates move, data needs to be transfered via buffer
     */
    private PlateBuffer plateBuffer;

    /**
     * Holds pending sediments to be added as well as the layer of sediments.
     */
    private SedimentBuffer erosionBuffer;

    private MoltenRockLayer moltenRockLayer;
    
    /**
     * The total height makes adding up each stratum faster. Each time a stratum
     * is removed or it's thickness is altered the totalMass is updated. The
     * units are in kilograms.
     */
    private float totalMass;

    /**
     * The total volume is calculated each time stratum is added or removed or
     * updated and is used to determine the average density of this cell in
     * cubic meters.
     */
    private float totalVolume;

    /**
     * The amount of this cell that is currently submerged in the mantel.
     */
    private float curAmountSubmerged;

    /**
     * A Point that is represented as the velocity for Plate Tectonics.
     * When a plate collides with a sibling (Cell owned by the same plate)
     * the collision is inelastic and will reduce it's velocity as well as
     * transfering a little bit of it's energy through the system.
     */
    private Point velocity;
    
    /**
     * The parent that updates this cell for Plate Tectonics. This allows
     * for plate updates to add/remove plate-point locations. When a new
     * plate is defined the cell locations are added and each cell is given
     * a velocity and a reference to the parent thread. If the cell is already
     * owned by a thread then it will change. The previous owner will
     * then update it's plate-point list by removing the point from
     * that list.
     */
    private SurfaceThread plateControlParent;
    
	/**
     * The type of crust this cell is.
     */
    private CrustType crustType;

    private long depositAgeTimeStamp;

    private final static Integer[][] heightMap, strataMap;

    public final static int MAX_HEIGHT_INDEX = 17;
    /**
     * The ratio for indexing onto the height map array, by taking a cell height
     * and dividing it by this value will give the proper index to the height
     * map.
     */
    public static int heightIndexRatio = 17 / MAX_HEIGHT_INDEX;

    public static int cellArea;
    
    static {
        Color[] heightColors = {new Color(255, 255, 204), new Color(51, 153, 51),
            new Color(157, 166, 175), new Color(255, 255, 255)};
        heightMap = Tools.constructSamples(heightColors, MAX_HEIGHT_INDEX);
        
        Layer[] layerTypes = Layer.values();
        strataMap = new Integer[layerTypes.length][4];

        for (int i = 0; i < layerTypes.length; i++) {
            Color c = layerTypes[i].getColor();
            strataMap[i][0] = c.getRed();
            strataMap[i][1] = c.getGreen();
            strataMap[i][2] = c.getBlue();
            strataMap[i][3] = c.getAlpha();
        }
        cellArea = 0;
    }

    /**
     * Constructs a new GeoCell at the location (x, y) with the parent surface
     * map provided.
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
        crustType = null;
        erosionBuffer = new SedimentBuffer();
        plateBuffer = new PlateBuffer();
        moltenRockLayer = new MoltenRockLayer();
        velocity = new Point(0, 0);

        totalStrataThickness = 0f;
        totalMass = 0f;
        totalVolume = 0f;
        curAmountSubmerged = 0f;
        depositAgeTimeStamp = 0;

    }

    public SurfaceThread getPlateControlThread() {
		return plateControlParent;
	}

	public void setPlateControlThread(SurfaceThread plateControlParent) {
		this.plateControlParent = plateControlParent;
	}
    
    public void setVelocity(Point vel){
    	velocity = new Point(vel);
    }
    
    public Point getVelocity(){
    	return velocity;
    }
    
    /**
     * Creates a deep copy of this GeoCell and it's strata.
     * @return The copy of this GeoCell.
     */
    public GeoCell copy() {

        GeoCell copy = new GeoCell(getX(), getY());
        Deque<Stratum> copyStrata = new ConcurrentLinkedDeque<>();
        
        strata.forEach(stratum -> {
            copyStrata.add(stratum.copy());
        });
        copy.strata = copyStrata;
        
        return copy;
    }

    /**
     * Sets the age of this cell to the time stamp given.
     *
     * @param depositAgeTimeStamp The new time stamp for this cell.
     */
    public void recordTime(long depositAgeTimeStamp) {
        this.depositAgeTimeStamp = depositAgeTimeStamp;
    }

    public long getAge() {
        return Surface.planetAge.get() - depositAgeTimeStamp;
    }

    public CrustType getCrustType() {
        return crustType;
    }

    public void setCrustType(CrustType type) {
        this.crustType = type;
    }

    /**
     * The density is an average based on the types of stratum that exist in the
     * strata. The amount of sea water also effects the average density.
     *
     * @return The average density of this cell as grams per liter.
     */
    public float getDensity() {

        float oceanMass = ((HydroCell) this).getOceanMass();
        float oceanVolume = oceanMass / OCEAN.getDensity();

        float totalMassWithOceans = (getTotalMass() + oceanMass);
        float totalVolumeWithOceans = (getTotalVolume() + oceanVolume);

        if (totalVolumeWithOceans == 0) {
            return 0;
        }

        return totalMassWithOceans / totalVolumeWithOceans;
    }

    /**
     * Gets the density of this cell without the ocean, if one exists.
     *
     * @return The density of this cell's rock without ocean density considered
     */
    public float getGeoDensity() {
        float tv = getTotalVolume();
        return (tv == 0)? 0 : getTotalMass() / tv;
    }

    public float getTotalMass() {
        return totalMass;
    }

    public float getTotalVolume() {
        return totalVolume;
    }

    /**
     * Gets the sediment buffer for this GeoCell.
     *
     * @return The sediment buffer for this GeoCell.
     */
    public SedimentBuffer getSedimentBuffer() {
        return erosionBuffer;
    }
    
    public MoltenRockLayer getMoltenRockLayer(){
        return moltenRockLayer;
    }

    /**
     * Removes the specified amount in kilograms. applyErosion will apply the
     * erosion factor for each stratum type. The method can remove from the top
     * or bottom of the strata.
     *
     * @param amount The amount to be removed in kilograms
     * @param applyErosion If erosion is applied
     * @param fromTop If removing from the top.
     * @return The amount that was actually removed in kilograms.
     */
    public float remove(float amount, boolean applyErosion, boolean fromTop) {

        float f = (1.0f - peekTopStratum().getLayer().getErosionFactor());
        amount *= (applyErosion) ? f : 1;

        if (amount < 0) {
            throw new IllegalArgumentException("The amount must be positive: " + amount);
        }
        float placedAmount = placeAmount(null, -amount, fromTop);

        return amount - placedAmount;
    }

    /**
     * Adds to the top or bottom of the strata or if the type does not match
     * this cell's top or bottom stratum then a new layer will be added.
     * Negative amounts are not allowed.
     *
     * @param type The type to be added/added to.
     * @param amount The amount as a positive real number, negatives will become
     * positive numbers. Units are in Kilograms.
     * @param toTop Adding to the top or not.
     */
    public void add(Layer type, float amount, boolean toTop) {
        if (amount < 0) {
            throw new IllegalArgumentException("The amount must be positive: " + amount);
        }

        placeAmount(type, amount, toTop);
    }

    /**
     * Adds the given amount and type at the given depth starting from the
     * top and working down the strata.
     * @param type
     * @param amount
     * @param depth 
     */
    public void addAtDepth(Layer type, float amount, float depth){
        float currentDepth = 0;
        Stratum selectedStratum;
        Deque<Stratum> workingStrata = new LinkedList<>();
        
        for (;peekTopStratum() != null && peekTopStratum() != null;){
            
            selectedStratum = removeTopStratum();
            float selectedDepth = selectedStratum.getThickness();
            Layer selectedType = selectedStratum.getLayer();
            
            currentDepth += selectedDepth;
            
            if (currentDepth > depth){
                if (type != selectedType){
                    float diff = currentDepth - depth;
                    float diffInMass = Tools.calcMass(diff, GeoCell.cellArea, selectedType);
                    selectedStratum.addToMass(-diffInMass);
                    Stratum splitLayer = new Stratum(selectedType, diffInMass);
                    workingStrata.push(selectedStratum);
                    Stratum insertedLayer = new Stratum(type, amount);
                    workingStrata.push(insertedLayer);
                    workingStrata.push(splitLayer);
                }else{
                    selectedStratum.addToMass(amount);
                    workingStrata.push(selectedStratum);
                }
                break;
                
            }else{
                workingStrata.push(selectedStratum);
            }
            
            if (peekTopStratum() == null && currentDepth >= depth){
                Stratum insertedLayer = new Stratum(type, amount);
                workingStrata.push(insertedLayer);
                break;
            }
        }
        
        while (!workingStrata.isEmpty()){
            selectedStratum = workingStrata.removeFirst();
            pushStratum(selectedStratum);
        }
    }
    
    /**
     * Adds or removes from the top or bottom of the strata, a negative amount
     * will remove, a positive amount will add.
     * <p>
     * There are several cases to consider when calling this method to utilize
     * it correctly to help save memory.</p>
     * <ul>
     * <li>If the top or bottom stratum is less than the amount then the top or
     * bottom stratum is removed completely and this method will recur until
     * the amount is 0 this will remove other layers too or just work on a 
     * single thick layer.
     * Passing in a null type and a negative amount will remove from the top or
     * bottom layer.</li>
     *
     * <li>If the type is not null and the amount is negative, nothing will
     * happen.
     * </li>
     * <li>
     * If the amount is positive and the type is null then the amount will just
     * be added to the top or bottom stratum regardless of it's type.
     * </li>
     * <li>If the type is not null and the stratum type matches and the amount
     * is positive then it will add to that layer otherwise a new layer of the
     * given type (if the types don't match) will be added.
     * </ul>
     *
     * @param type The type of material to add
     * @param amount The amount to add/remove in kilograms
     * @param workOnTop Add/Remove to the top stratum/strata
     * @return The amount (positive) that could not be removed due to the
     * stratum being less than the amount, otherwise 0 is returned.
     */
    public float placeAmount(Layer type, float amount, boolean workOnTop) {

        Stratum selectedStratum = workOnTop ? peekTopStratum() : peekBottomStratum();

        // Check to make sure that there is strata
        if (selectedStratum == null) { // If no strata exists
            if (amount > 0) { // if trying to add
                addToStrata(type, amount, workOnTop);
            } else if (amount < 0) { // if trying to remove
                return -amount; // Nothing to remove. Return a positive amount
            }
            return 0;
        }

        Layer sType = selectedStratum.getLayer();
        float mass = selectedStratum.getMass(), difference;

        if (amount < 0) { // Removing from the Stratum
            difference = mass + amount;// Take the difference (amount is negative)
            if (difference < 0) {

                if (verifyStratumMass(selectedStratum, workOnTop)) {
                    addToStrata(null, amount - difference, workOnTop);
                }
                try {
                    return placeAmount(type, difference, workOnTop);
                } catch (StackOverflowError e) {
                    Logger.getLogger(GeoCell.class.getName())
                            .log(Level.SEVERE, "Stackoverflow\nmass: {0}\n"
                                    + "amount: {1}\n", new Object[]{mass, amount});
                }
            } else {
                /* not adding but subtracting the amounts from the
                 stratum since the amount is less than 0. */
                addToStrata(null, amount, workOnTop);
                verifyStratumMass(selectedStratum, workOnTop);
            }

        } else if (amount > 0) { // Adding to the Stratum
            if (type != null) {
                if (type == sType) {
                    addToStrata(null, amount, workOnTop);
                } else {
                    addToStrata(type, amount, workOnTop);
                }
            } else { // Type not specified add to the top layer
                addToStrata(null, amount, workOnTop);
            }
        }

        return 0;
    }

    private boolean verifyStratumMass(Stratum stratum, boolean workOnTop) {
        float mass = stratum.getMass();
        if ((mass >= 0.0000001f && mass <= 0.0001f) || mass <= 0) {
            if (workOnTop) {
                removeTopStratum();
            } else {
                removeBottomStratum();
            }
            return false;
        }
        return true;
    }

    /**
     * Adds a new stratum if a type is specified.
     *
     * <p>
     * Consider the cases below when calling this method</p>
     * <ul>
     * <li>If the type is null then the top or bottom stratum receives the
     * amount being added. Checking for existing strata is important before
     * calling this method since negative amounts are allowed but not
     * recommended unless the top or bottom layer mass is greater or equal to
     * the amount being removed, <code>placeAmount()</code> helps to accomplish
     * this.
     * </li>
     * <li>If the type is null then the top or bottom is added or
     * subtracted</li>
     * </ul>
     *
     * @param amount The amount of mass the stratum is or is going to receive in
     * kilograms.
     * @param type The layer type, null adds to any existing stratum at the top.
     * @param toTop Adding to the top or bottom of the strata.
     */
    public void addToStrata(Layer type, float amount, boolean toTop) {

        Stratum stratum = toTop ? peekTopStratum() : peekBottomStratum();

        if (type == null) {

            if (stratum != null) {
                stratum.addToMass(amount);
                type = stratum.getLayer();
                updateMV(amount, type);
            }else{
                if (amount > 0){
                    if (toTop) {
                        pushStratum(type, amount);
                    } else {
                        appendStratum(type, amount);
                    }
                }
            }

        } else {
            if (toTop) {
                pushStratum(type, amount);
            } else {
                appendStratum(type, amount);
            }
        }
    }

    /**
     * Update the mass and volume of this cell with the given stratum. This will
     * take the mass of the stratum and update the total cell mass and volume.
     *
     * @param stratum The stratum being added or removed from the total mass and
     * volume of the strata.
     * @param subtract If the stratum is to be subtracted from the strata.
     */
    private void updateMV(Stratum stratum, boolean subtract) {

        Layer layer = stratum.getLayer();
        float mass = subtract ? -stratum.getMass() : stratum.getMass();

        updateMV(mass, layer);
    }

    /**
     * Updates the total mass and volume for this cell with the given mass and
     * layer type (density).
     *
     * @param mass The amount of mass in kilograms being added or removed.
     * @param type The layer type of the mass, used to calculate volume.
     */
    private void updateMV(float mass, Layer type) {

        if (type == null){
            throw new IllegalArgumentException("The layer type can't be null");
        }
        
        totalStrataThickness += Tools.calcHeight(mass, GeoCell.cellArea, type);
        totalMass += mass;
        totalVolume += mass / type.getDensity();
    }

    /**
     * Adds a new stratum layer to the strata.
     *
     * @param type The layer type
     * @param amount The mass in kilograms
     */
    public void pushStratum(Layer type, float amount) {
        pushStratum(new Stratum(type, amount));
    }

    /**
     * Adds a new stratum layer to the strata, if the stratum is null nothing
     * will happen.
     *
     * @param stratum The stratum being added
     */
    public void pushStratum(Stratum stratum) {

        if (stratum != null) {
            stratum.setBottom(strata.peek());
            if (!strata.isEmpty() && strata.peek() != null) {
                strata.peek().setTop(stratum);
            }
            strata.push(stratum);
            updateMV(stratum, false);
        }
    }

    /**
     * Adds stratum to the bottom of the strata.
     *
     * @param type The layer type
     * @param amount The mass in kilograms
     */
    public void appendStratum(Layer type, float amount) {
        appendStratum(new Stratum(type, amount));
    }

    /**
     * Adds to the bottom of the strata.
     *
     * @param stratum The stratum being appended to this cell.
     */
    public void appendStratum(Stratum stratum) {

        if (stratum != null) {
            Stratum bottom = peekBottomStratum();
            if (bottom != null){
                bottom.setBottom(stratum);
                stratum.setTop(bottom);
            }
            strata.addLast(stratum);
            updateMV(stratum, false);
        }
    }

    /**
     * Removes from the top strata with erosion applied. The amount returned is
     * the actual amount removed.
     *
     * @param amount The amount to be removed in kilograms
     * @return The amount that was actually removed.
     */
    public float erode(float amount) {

        Stratum top = peekTopStratum();
        if (top == null) {
            return 0;
        }

        Stratum bottom = peekTopStratum().next();
        Layer ref;

        ref = (bottom == null) ? peekTopStratum().getLayer() : bottom.getLayer();

        float changedMass = changeMass(amount, ref, MAFIC);

        return remove(changedMass, true, true);
    }

    /**
     * Removes the top stratum from this cell. The bottom reference of the cell
     * being removed is also nullified after removal.
     *
     * @return the stratum being removed from the top, null if there are no
     * strata to be removed.
     */
    public Stratum removeTopStratum() {

        if (strata.peek()==null){
            return null;
        }
        Stratum removed = strata.removeFirst();
        removed.removeBottom();
        if (strata.peek() != null) {
            strata.peek().removeTop();
        }

        return updateRemoved(removed);
    }

    /**
     * Removes the bottom stratum from this cell. The bottom reference shouldn't
     * matter since it is the lowest stratum in the strata.
     *
     * @return the stratum being removed from the bottom, null if there are no
     * strata to be removed.
     */
    public Stratum removeBottomStratum() {

        Stratum removed = strata.removeLast();
        Stratum bottom = peekBottomStratum();

        if (bottom != null) {
            bottom.removeTop();
        }
        
        Stratum nextBottom = peekBottomStratum();
        if (nextBottom != null) {
            nextBottom.removeBottom();
        }
        
        return updateRemoved(removed);
    }

    /**
     * Updates the stratum being removed and performs additional clean up
     *
     * @param removed The stratum that is being removed.
     * @return The removed stratum
     */
    private Stratum updateRemoved(Stratum removed) {
        if (removed == null) {
            throw new IllegalArgumentException("removed cannot be null");
        }
        updateMV(removed, true);
        return removed;
    }

    /**
     * Peeks at the top of the strata of this cell but does not remove the top
     * stratum.
     *
     * @return The stratum at the top of the strata.
     */
    public Stratum peekTopStratum() {
        return strata.peekFirst();
    }

    /**
     * Peeks at the bottom of the strata of this cell but does not remove the
     * bottom stratum.
     *
     * @return The stratum at the bottom of the strata.
     */
    public Stratum peekBottomStratum() {
        return strata.peekLast();
    }

    /**
     * Get the strata list.
     *
     * @return The list of strata for this cell
     */
    public Deque<Stratum> getStrata() {
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
        return hasOcean() ? (getHeight() - ((HydroCell) this).getOceanHeight()) : getHeight();
    }

    /**
     * Fetches the current thickness of all the strata added together. This
     * isn't like the height of the cell where it is calculated from equilibrium
     * and up from the mantel.
     * @return 
     */
    public float getStrataThickness(){
        return totalStrataThickness;
    }
    
    /**
     * The height of this cell is based on the average density of the strata
     * with the ocean depth included. If the timescale is in Geological the
     * height of this cell will be updated to it's equilibrium height.
     *
     * @return The height of this cell with ocean depth included.
     */
    public float getHeight() {

        float cellHeight;
        float oceanVolume = ((HydroCell) this).getOceanVolume();

        cellHeight = (oceanVolume + getTotalVolume()) / instance().getCellArea();

        if (instance().getTimeScale() == Planet.TimeScale.Geological) {
            recalculateHeight();
        }

        return cellHeight - curAmountSubmerged;

    }

    /**
     * Shifts the height of this cell to it's equilibrium height. This method
     * is called while the simulation is in the Geological timescale and the
     * <code>getThickness()</code> method is called.
     */
    public void recalculateHeight() {
        float cellHeight, amountSubmerged, density = getDensity();
        float oceanVolume = ((HydroCell) this).getOceanVolume();

        cellHeight = (oceanVolume + getTotalVolume()) / instance().getCellArea();
        amountSubmerged = cellHeight * (density / mantel_density);

        curAmountSubmerged = amountSubmerged;
    }

    /**
     * Updates the height of the rock based on it's buoyancy with the mantel.
     */
    public void updateHeight() {

        float cellHeight, amountSubmerged, density = getDensity();
        float oceanVolume = ((HydroCell) this).getOceanVolume();

        cellHeight = (oceanVolume + getTotalVolume()) / instance().getCellArea();
        amountSubmerged = cellHeight * (density / mantel_density);

        shiftHeight(amountSubmerged);

    }

    private void shiftHeight(float amountSubmerged) {
        float cas = curAmountSubmerged;
        float diff = Math.abs(amountSubmerged - cas);
        float change = diff / 2f;
        change = (change < 0.01f) ? 0.01f : change;
        if (amountSubmerged > cas) {
            curAmountSubmerged += change;
        } else if (amountSubmerged < cas) {
            curAmountSubmerged -= change;
        }
    }

    public boolean hasOcean() {
        return ((HydroCell) this).getOceanMass() > 0;
    }

    public List<Integer[]> render(List<Integer[]> settings) {
        int setting;
        PlanetSurface surface = (PlanetSurface) instance().getSurface();
        switch (surface.displaySetting) {
            case HEIGHTMAP:
                float thisHeight = getHeightWithoutOceans();
                float height = Math.max(0, thisHeight - surface.getLowestHeight()) * 5;
                setting = (int) Math.min((height / heightIndexRatio), MAX_HEIGHT_INDEX - 1);
                settings.add(heightMap[setting]);

                return super.render(settings);

            case STRATAMAP:
                if (moltenRockLayer.getMoltenRockFromSurface() < 100) {

                    Stratum topStratum = peekTopStratum();

                    if (topStratum != null) {
                        Layer layerType = topStratum.getLayer();
                        settings.add(strataMap[layerType.getID()]);
                    }
                    if (Geosphere.drawSediments) {

                    }
                } else {
                    Integer[] c = {255, 0, 0, 250};
                    settings.add(c);
                    return super.render(settings);
                }
                return super.render(settings);
            default: // The display setting is not listed
                return super.render(settings);
        }
    }
}
