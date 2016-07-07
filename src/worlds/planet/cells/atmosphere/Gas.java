

package worlds.planet.cells.atmosphere;

import worlds.planet.enums.GasType;

/**
 *
 * @author Richard DeSilvey
 */
public class Gas {

    private GasType gasType;
    private float mass;
    
    public Gas(GasType gasType){
        mass = 0;
        this.gasType = gasType;
    }

    public float getMolarMass() {
        return mass / gasType.getMoles();
    }

    public float getMass() {
        return mass;
    }
    
    public GasType getGasType() {
        return gasType;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }
    
}
