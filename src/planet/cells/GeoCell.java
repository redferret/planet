
package planet.cells;

import java.awt.Color;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import planet.Planet;
import planet.util.TBuffer;
import planet.util.Tools;
import planet.enums.CrustType;
import planet.enums.Layer;
import planet.surface.Stratum;
import planet.surface.Surface;

import static planet.surface.Surface.*;
import static planet.enums.Layer.*;
import static planet.util.Tools.*;

/**
 * A GeoCell is a Cell representing land geographically. The cell contains
 * strata and specialized methods for adding and/or removing from the strata.
 * The GeoCell doesn't contain information about oceans but accesses the
 * Hydrosphere to obtain the quantity of sea water at this cell location to
 * calculate the average density of this cell.
 *
 * @author Richard DeSilvey
 */
public class GeoCell extends Mantel {

    /**
     * A single class buffer part of a transfer map
     */
    public final class SedimentBuffer extends TBuffer {
        
        /**
         * Pending sediments to be added
         */
        private float sediments;
        
        /**
         * Represents the amount of sediments on the surface of this cell.
         */
        private float totalSediments;
        
        public SedimentBuffer(){
            super();
        }
        
        protected final void init(){
            sediments = 0;
        }
        
        /**
         * During erosion, sediments need to be transfered to other cells, this
         * acts as a sediment map where changes will be applied later after
         * erosion occurs.
         *
         * @param amount The amount to transfer in kilograms
         */
        public void transferSediment(float amount) {
            if (!bufferSet()) {
                bufferSet(true);
            }
            sediments += amount;
        }

        /**
         * Apply the changes that need to be made to this cell, if the buffer is
         * not set nothing will happen.
         */
        public void applySedimentBuffer() {
            if (bufferSet()) {
                updateSurfaceSedimentMass(sediments);
                resetBuffer();
            }
        }

        public void removeAllSediments() {
            updateMV(-totalSediments, SEDIMENT);
            totalSediments = 0;
        }

        /**
         * Adds or removes sediments from this cell.
         *
         * @param mass The amount of sediments in kilograms.
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
            updateMV(mass, SEDIMENT);
            return mass;
        }

        /**
         *
         * @return The amount of sediments from this cell in kilograms.
         */
        public float getSediments() {
            return totalSediments;
        }

    }

    
    /**
     * A single class buffer part of a transfer map
     */
    private class PlateBuffer extends TBuffer {
        
        private Deque<Stratum> strata;
        private float totalMass, totalVolume;
        
        public PlateBuffer(){
            super();
        }
        
        protected final void init(){
            totalMass = totalVolume = 0;
            if (strata == null){
                strata = new LinkedList<>();
            }else{
                strata.clear();
            }
            
        }
        
    }
    
    /**
     * The thickness of sediment layers to become visible on screen.
     */
    public static float minSedimentThickness = 32;
    
    /**
     * The list of strata for this cell
     */
    private Deque<Stratum> strata;
    
    /**
     * When the plates move, data needs to be transfered via buffer
     */
    private PlateBuffer plateBuffer;
    
    /**
     * Temporarily holds sediments, rain water, and ice to be transfered
     * over to this cell. This better to use than a separate transfer map.
     */
    private SedimentBuffer erosionBuffer;
    
    
    /**
     * The total height makes adding up each stratum faster.
     * Each time a stratum is removed or it's thickness is altered
     * the totalMass is updated. 
     * The units are in kilograms. 
     */
    private float totalMass;
    
    /**
     * The total volume is calculated each time stratum is added or
     * removed or updated and is used to determine the average
     * density of this cell in cubic meters.
     */
    private float totalVolume;
    
    /**
     * Represents the amount of molten rock on the surface of
     * this cell. 
     */
    private float moltenRockSurfaceMass;
    
    /**
     * The amount of this cell that is currently submerged in the mantel.
     */
    private float curAmountSubmerged;
    
    /**
     * The type of crust this cell is.
     */
    private CrustType crustType;
    
    private long depositAgeTimeStamp;
    
    private static Integer[][] heightMap, strataMap, lavaMap;
    
    static {
        Color[] colors = {new Color(255, 255, 204), new Color(51, 153, 51),
                        new Color(157, 166, 175)};
        
        heightMap = Tools.constructSamples(colors, 50);
        
        Color[] strataColors = {SEDIMENT.getColor(), SOIL.getColor(),
                                GRAVEL.getColor(), SANDSTONE.getColor(),
                                SHALE.getColor(), LIMESTONE.getColor(),
                                METAMORPHIC.getColor(), BASALT.getColor(),
                                GRANITE.getColor(), OCEAN.getColor(),
                                LAVA.getColor(), ICE.getColor()};
        
        strataMap = new Integer[strataColors.length][4];
        
        for (int i = 0; i < strataColors.length; i++){
            Color c = strataColors[i];
            strataMap[i][0] = c.getRed();
            strataMap[i][1] = c.getGreen();
            strataMap[i][2] = c.getBlue();
            strataMap[i][3] = c.getAlpha();
        }
        
    }
    
    /**
     * Constructs a new GeoCell at the location (x, y) with the parent
     * surface map provided.
     * @param x The x coordinate
     * @param y The y coordinate
     */
    public GeoCell(int x, int y) {
        super(x, y);
        setup();
    }

    private void setup(){
        
        strata = new LinkedList<>();
        crustType = null;
        erosionBuffer = new SedimentBuffer();
        plateBuffer = new PlateBuffer();
        
        totalMass = 0;
        totalVolume = 0;
        moltenRockSurfaceMass = 0;
        curAmountSubmerged = 0;
        depositAgeTimeStamp = 0;
        
        recalculateHeight();
    }
    
    /**
     * Sets the age of this cell to the time stamp given.
     * @param depositAgeTimeStamp The new time stamp for this cell.
     */
    public void recordTime(long depositAgeTimeStamp){
        this.depositAgeTimeStamp = depositAgeTimeStamp;
    }
    
    public long getAge(){
        return Surface.planetAge.get() - depositAgeTimeStamp;
    }
    
    public CrustType getCrustType() {
        return crustType;
    }
    
    public void setCrustType(CrustType type){
        this.crustType = type;
    }
    
    /**
     * The density is an average based on the types of stratum that
     * exist in the strata. The amount of sea water also effects the
     * average density.
     * @return The average density of this cell as grams per liter.
     */
    public float getDensity() {

        float oceanMass = ((HydroCell) this).getOceanMass();
        float oceanVolume = oceanMass / OCEAN.getDensity();
        
        float totalMassWithOceans = (totalMass + oceanMass);
        float totalVolumeWithOceans = (totalVolume + oceanVolume);
        
        if (totalVolumeWithOceans == 0){
            return 0;
        }
        
        return totalMassWithOceans / totalVolumeWithOceans;
    }
    
    /**
     * Gets the density of this cell without the ocean, if one exists.
     * @return The density of this cell's rock without ocean density considered
     */
    public float getGeoDensity(){
        
        if (totalVolume == 0){
            return 0;
        }
        
        return totalMass / totalVolume;
    }
    
    public float getTotalMass(){
        return totalMass;
    }
    
    public float getTotalVolume(){
        return totalVolume;
    }
    
                                                                                /* %% Marked as another abstraction %%*/
    
    /** 
     * Adds or removes molten rock from this cell. The total mass and
     * volume of this cell is effected by this addition or subtraction.
     * @param mass The amount of molten rock being added.
     */
    public void putMoltenRockToSurface(float mass){
        moltenRockSurfaceMass += mass;
        updateMV(mass, LAVA);
    }
    
    /**
     * @return The amount of molten rock from this cell in Kilograms.
     */
    public float getMoltenRockFromSurface(){
        return moltenRockSurfaceMass;
    }
    
    /**
     * Removes all molten rock from this cell.
     */
    public void removeAllMoltenRock(){
        updateMV(-moltenRockSurfaceMass, LAVA);
        moltenRockSurfaceMass = 0;
    }
    
                                                                                /* !! Marked as another abstraction !!*/
    
    /**
     * Gets the sediment buffer for this GeoCell. 
     * @return The sediment buffer for this GeoCell.
     */
    public SedimentBuffer getSedimentBuffer(){
        return erosionBuffer;
    }
     
    /**
     * Removes the specified amount in kilograms. applyErosion will
     * apply the erosion factor for each stratum type. The method can remove
     * from the top or bottom of the strata.
     * 
     * @param amount The amount to be removed in kilograms
     * @param applyErosion If erosion is applied
     * @param fromTop If removing from the top.
     * @return The amount that was actually removed in kilograms.
     */
    public float remove(float amount, boolean applyErosion, boolean fromTop) {

        float f = (1.0f - peekTopStratum().getLayer().getErosionFactor());
        amount *= (applyErosion) ? f : 1;
        
        float removal = (amount > 0) ? -amount : amount;
        float placedAmount = placeAmount(null, removal, fromTop);
        
        return amount - placedAmount;
    }
    
    /**
     * Adds to the top or bottom of the strata or if the type does not match
     * this cells top or bottom stratum type then a new stratum will be added.
     *
     * @param type The type to be added/added to.
     * @param amount The amount as a positive real number, negatives will become
     * positive numbers. Units are in Kilograms.
     * @param toTop Adding to the top or not.
     */
    public void add(Layer type, float amount, boolean toTop) {
        float addition = (amount < 0) ? -amount : amount;
        placeAmount(type, addition, toTop);
    }
    
    /**
     * Adds or removes from the top or bottom of the strata, a negative amount
     * will remove, a positive amount will add. If the top or bottom stratum is
     * less than the amount then the top or bottom stratum is removed completely
     * and the difference will be returned. Passing in a null type and a
     * negative amount will remove from the top or bottom layer. If the type is
     * not null and the amount is negative, nothing will happen. If the amount
     * is positive and the type is null then the amount will just be added to
     * the top or bottom stratum regardless of it's type. If the type is not
     * null and the stratum type matches and the amount is positive then it will
     * add to that layer otherwise a new layer of the given type (if the types
     * don't match) will be added.
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
            if (amount > 0){ // if trying to add
                addToStrata(type, amount, workOnTop);
            } else if (amount < 0){ // if trying to remove
                return -amount; // Nothing to remove. Return a positive amount
            }
            return 0;
        }
            
        Layer sType = selectedStratum.getLayer();
        float mass = selectedStratum.getMass(), difference;
        
        if (amount < 0){ // Removing from the Stratum
            difference = mass + amount;// Take the difference (amount is negative)
            if (difference < 0){
                difference = Math.min(-0.01f, difference);
                if (verifyStratumMass(selectedStratum, workOnTop)){
                    addToStrata(null, amount - difference, workOnTop);
                }
                return placeAmount(type, difference, workOnTop);
            }else{
                /* not adding but subtracting the amounts from the
                   stratum since the amount is less than 0. */
                addToStrata(null, amount, workOnTop);
                verifyStratumMass(selectedStratum, workOnTop);
            }
            
        }else if (amount > 0){ // Adding to the Stratum
            if (type != null){
                if (type == sType){
                    addToStrata(null, amount, workOnTop);
                }else{
                    addToStrata(type, amount, workOnTop); 
                }
            }else{ // Type not specified add to the top layer
                addToStrata(null, amount, workOnTop);
            }
        }
        
        return 0;
    }

    private boolean verifyStratumMass(Stratum stratum, boolean workOnTop){
        float mass = stratum.getMass();
        if ((mass >= 0.0000001f && mass <= 0.0001f) || mass <= 0){
            if (workOnTop){
                removeTopStratum();
            }else{
                removeBottomStratum();
            }
            return false;
        }
        return true;
    }
    
    /**
     * Adds a new stratum if a type is specified. If the type is null then the
     * top or bottom stratum receives the amount being added. Checking for
     * existing strata is important before calling this method since negative
     * amounts are allowed but not recommended unless the top or bottom layer
     * mass is greater or equal to the amount being removed,
     * <code>placeAmount()</code> helps to accomplish this. If the type is null
     * then a new layer is added of that type even if the top or bottom layer
     * is the same.
     *
     * @param amount The amount of mass the stratum is or is going to receive in
     * kilograms.
     * @param type The layer type, null adds to any existing stratum at the top.
     * @param toTop Adding to the top or bottom of the strata.
     */
    public void addToStrata(Layer type, float amount, boolean toTop){
        
        Stratum stratum = toTop ? peekTopStratum() : peekBottomStratum();
        
        if (type == null){

            if (stratum != null){
                stratum.addToMass(amount);
                type = stratum.getLayer();
                updateMV(amount, type);
            }
            
        }else{
            if (toTop)
                pushStratum(type, amount);
            else
                appendStratum(type, amount);
        }
    }
    
    /**
     * Update the mass and volume of this cell with the given stratum. This
     * will take the mass of the stratum and update the total cell
     * mass and volume.
     * @param stratum The stratum being added or removed from the total mass 
     * and volume of the strata.
     * @param subtract If the stratum is to be subtracted from the strata.
     */
    private void updateMV(Stratum stratum, boolean subtract){
        
        Layer layer = stratum.getLayer();
        float mass = subtract? -stratum.getMass() : stratum.getMass();
        
        updateMV(mass, layer);
    }
    
    /**
     * Updates the total mass and volume for this cell with the given mass
     * and layer type (density).
     * @param mass The amount of mass in kilograms being added or removed.
     * @param type The layer type of the mass, used to calculate volume.
     */
    private void updateMV(float mass, Layer type) {

        totalMass += mass;
        totalVolume += mass / type.getDensity();
        
    }
    
    /**
     * Adds a new stratum layer to the strata.
     * @param type The layer type
     * @param amount The mass in kilograms
     */
    public void pushStratum(Layer type, float amount){
        pushStratum(new Stratum(type, amount));
    }
    
    /**
     * Adds a new stratum layer to the strata, if the stratum is null
     * nothing will happen.
     * @param stratum The stratum being added
     */
    public void pushStratum(Stratum stratum){
        
        if (stratum != null){
            stratum.setBottom(strata.peek());
            if (!strata.isEmpty() && strata.peek() != null)
                strata.peek().setTop(stratum);
            strata.push(stratum);
            updateMV(stratum, false);
        }
    }
    
    /**
     * Adds stratum to the bottom of the strata.
     * @param type The layer type
     * @param amount The mass in kilograms
     */
    public void appendStratum(Layer type, float amount){
        appendStratum(new Stratum(type, amount));
    }
    
    /**
     * Adds to the bottom of the strata.
     * @param stratum The stratum being appended to this cell.
     */
    public void appendStratum(Stratum stratum){
        
        if (stratum != null){
            peekBottomStratum().setBottom(stratum);
            stratum.setTop(peekBottomStratum());
            strata.addLast(stratum);
            updateMV(stratum, false);
        }
    }
    
    /**
     * Removes from the top strata with erosion applied. The amount
     * returned is the actual amount removed.
     * 
     * @param amount The amount to be removed in kilograms
     * @return The amount that was actually removed.
     */
    public float erode(float amount) {
        
        Stratum top = peekTopStratum();
        if (top == null) return 0;
        
        Stratum bottom = peekTopStratum().next();
        Layer ref;

        ref = (bottom == null) ? peekTopStratum().getLayer() : bottom.getLayer();
        
        float changedMass = changeMass(amount, ref, SEDIMENT);
        
        return remove(changedMass, true, true);
    }
    
    /**
     * Removes the top stratum from this cell. The bottom reference of the cell
     * being removed is also nullified after removal.
     *
     * @return the stratum being removed from the top, null if there are no strata
     * to be removed.
     */
    public Stratum removeTopStratum(){
        
        Stratum removed = strata.removeFirst();
        removed.removeBottom();
        strata.peek().removeTop();
        
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
       
       if (bottom == null){
           throw new RuntimeException("No strata!");
       }
       bottom.removeTop();
       peekBottomStratum().removeBottom();
       
       return updateRemoved(removed);
    }
 
    /**
     * Updates the stratum being removed and performs additional clean up
     *
     * @param removed The stratum that is being removed.
     * @return The removed stratum
     */
    private Stratum updateRemoved(Stratum removed){
        if (removed == null){
            throw new IllegalArgumentException("removed cannot be null");
        }
        updateMV(removed, true);
        return removed;
    }
    
    /**
     * Peeks at the top of the strata of this cell but does not remove the top
     * stratum.
     * @return The stratum at the top of the strata.
     */
    public Stratum peekTopStratum(){
        return strata.peekFirst();
    }
    
    /**
     * Peeks at the bottom of the strata of this cell but does not remove the
     * bottom stratum.
     * 
     * @return The stratum at the bottom of the strata.
     */
    public Stratum peekBottomStratum(){
        return strata.peekLast();
    }
    
    
    /**
     * Get the strata list
     * @return The list of strata for this cell
     */
    public Deque<Stratum> getStrata(){
        return strata;
    }
    
    /**
     * The height of this cell can be represented as a height with or without
     * seawater. This method subtracts the ocean depth but leaves the average
     * density the same as if the ocean were there but doesn't include the depth
     * of the ocean.
     * @return The height of this cell without ocean depth.
     */
    public float getHeightWithoutOceans(){
        return hasOcean() ? (getHeight() - ((HydroCell) this).getOceanHeight()) : getHeight();
    }
    
    /**
     * The height of this cell is based on the average density of the strata 
     * with the ocean depth included. If the timescale is in Geological
     * the height of this cell will be updated to it's equilibrium height.
     * @return The height of this cell with ocean depth included.
     */
    public float getHeight(){
        
        float cellHeight;
        float oceanVolume = ((HydroCell) this).getOceanVolume();
        
        cellHeight = (oceanVolume + totalVolume) / Planet.self().getBase();
        
        if (Planet.self().getTimeScale() == Planet.TimeScale.Geological) {
            recalculateHeight();
        }
        
        return cellHeight - curAmountSubmerged;
        
    }

    /**
     * Shifts the height of this cell to it's equilibrium height
     */
    public void recalculateHeight(){
        float cellHeight, amountSubmerged, density = getDensity();
        float oceanVolume = ((HydroCell) this).getOceanVolume();
        
        cellHeight = (oceanVolume + totalVolume) / Planet.self().getBase();
        amountSubmerged = cellHeight * (density / mantel_density);

        curAmountSubmerged = amountSubmerged;
    }
    
    /**
     * Updates the height of the rock based on it's buoyancy with the
     * mantel.
     */
    public void updateHeight(){
        
        float cellHeight, amountSubmerged, density = getDensity();
        float oceanVolume = ((HydroCell) this).getOceanVolume();
        
        cellHeight = (oceanVolume + totalVolume) / Planet.self().getBase();
        amountSubmerged = cellHeight * (density / mantel_density);

        float diff = Math.abs(amountSubmerged - curAmountSubmerged);
        float change = diff / 2f;
        change = (change < 0.01f)? 0.01f : change;
        if (amountSubmerged > curAmountSubmerged){
            curAmountSubmerged += change;
        }else if(amountSubmerged < curAmountSubmerged){
            curAmountSubmerged -= change;
        }
        
    }
    
    public boolean hasOcean(){
        return ((HydroCell) this).getOceanMass() > 0;
    }
    
    public List<Integer[]> render(List<Integer[]> settings) {
        int setting;
        switch(Planet.self().getSurface().displaySetting){
            case HEIGHTMAP:
                float height = Math.abs((getHeightWithoutOceans() - Surface.absLowestHeight.get())) * 10;
                setting = (int) (height / heightIndexRatio) % MAX_HEIGHT_INDEX;
                settings.add(heightMap[setting]);
                
                return super.render(settings);
                
            case STRATAMAP:
                if (moltenRockSurfaceMass < 100){
                
                    Stratum topStratum = peekTopStratum();

                    if (topStratum != null){
                        Layer layerType = topStratum.getLayer();
                        settings.add(strataMap[layerType.getID()]);
                        return super.render(settings);
                    }
                }else{
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
