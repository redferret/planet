
package worlds.planet.cells.geology;

import worlds.planet.enums.Layer;
import worlds.planet.Planet;


/**
 * The Stratum class represents a layer within the strata of a GeoCell.
 * 
 * @author Richard DeSilvey
 */
public class Stratum {
        
    /**
     * The type of layer this stratum is.
     */
    private Layer type;

    /**
     * The mass of this layer in kilograms.
     */
    private float mass;
    
    /**
     * Used to determine if this layer is a conglomerate or not. 1f means 
     * mostly solid rock, 0f means silt like sediments. This percentage is used
     * to create a measurement for the distance a cell covers. 
     * If a cell is 10 km long with a granularity of 0.6f then on average
     * each boulder or spike or nodule would be (10 km * 0.6) / 1000 km = 6 meters long.
     */
    private float granularity;
    
    /**
     * The age of the stratum in years
     */
    private long age, lastAdd;
    
    /**
     * An object reference of the layer below this Stratum.
     */
    private Stratum bottom, top;

    public Stratum(Stratum stratum){
        this.mass = stratum.mass;
        this.age = stratum.age;
        this.bottom = stratum.bottom;
        this.top = stratum.top;
        this.type = stratum.type;
        granularity = 1;
    }
    
    public Stratum(Layer type, float mass){

        this.mass = mass;

        this.type = type;
        
        this.age = Planet.instance().getSurface().getPlanetAge();
        
        lastAdd = this.age;
        top = null;
        bottom = null;
    }

    /**
     * Sets the age of the Stratum to the current age of the planet.
     */
    public void resetAge(){
        this.age = Planet.instance().getSurface().getPlanetAge();
    }
    
    public void setGranularity(float value){
        
        if (value > 1f || value < 0){
            throw new IllegalArgumentException("The granularity must be a value in [0, 1]");
        }
        
        granularity = value;
    }
    
    public float getGranularity() {
        return granularity;
    }
    
    /**
     * Returns the mass of this stratum in kilograms.
     * @return The mass of this stratum.
     */
    public float getMass() {
        return mass;
    }
    
    /**
     * Returns the volume of this stratum in cubic meters
     * @return The volume of this stratum
     */
    public float getVolume(){
        return mass / type.getDensity();
    }
    
    /**
     * The height is calculated based on the volume of the stratum and the
 Planet.instance().getCellArea()_SQRD.
     *
     * @return The height of this stratum.
     */
    public float getHeight() {
        return getVolume() / Planet.instance().getCellArea();
    }
    
    public void setMass(float m){
        mass = m;
    }
    
    public void addToMass(float value){
        this.mass += value;
        lastAdd = Planet.instance().getSurface().getPlanetAge();
    }

    public long getLastAdd(){
        return lastAdd;
    }
    
    public void setStratumType(Layer type){
        this.type = type;
    }
    
    public Layer getLayer() {
        return type;
    }
    
    public void setAge(long age) {
        this.age = age;
    }

    public long getAge() {
        return age;
    }

    /**
     * Create a reference to the layer below this stratum. This
     * helps create another data structure for easier iteration through the
     * strata.
     * @param stratum The stratum being referenced below this stratum.
     */
    public void setBottom(Stratum stratum){
        bottom = stratum;
    }
    
    public void setTop(Stratum stratum){
        top = stratum;
    }
    
    /**
     * Fetches the stratum layer above this one if it exists, null otherwise.
     * @return The layer above this layer.
     */
    public Stratum previous(){
        return top;
    }
    
    /**
     * The stratum below this stratum. 
     * @return The next stratum below this.
     */
    public Stratum next() {
        return bottom;
    }

    /**
     * Nullifies the bottom reference of this stratum.
     */
    public void removeBottom() {
        bottom = null;
    }

    public void removeTop(){
        top = null;
    }

    public Stratum copy() {
        return new Stratum(this);
    }
}