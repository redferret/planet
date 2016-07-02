

package worlds.planet.surface;

import worlds.planet.enums.Gases;

/**
 *
 * @author Richard DeSilvey
 */
public class Gas {

    private Gases gasType;
    private float mass;
    
    public Gas(Gases gasType){
        mass = 0;
        this.gasType = gasType;
    }

    public float getMass() {
        return mass / gasType.getMoles();
    }

    public Gases getGasType() {
        return gasType;
    }

    public void setMass(float mass) {
        this.mass = mass;
    }
    
}
