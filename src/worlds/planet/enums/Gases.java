
package worlds.planet.enums;

/**
 * Each gas that can exist in the atmosphere of a planet. Each can turn into the
 * other based on biological, geological, or other chemical process. For
 * instance Ozone is naturally produced in the stratosphere by a two-step
 * reactive process. In the first step, solar ultraviolet radiation (sunlight)
 * breaks apart an oxygen molecule to form two separate oxygen atoms. In the
 * second step, each atom then undergoes a binding collision with another oxygen
 * molecule to form an ozone molecule. In the overall process, three oxygen
 * molecules plus sunlight react to form two ozone molecules.
 *
 * atmospheric pressure can be calculated with p = (m / v) R T
 * R = Gas Constant
 * T = Temperature
 * m = mass
 * v = volume
 * 
 * @author Richard DeSilvey
 */
public enum Gases {
    Argon(208), CarbonDioxide(189), CarbonMonoxide(297), 
    Methane(518), Nitrogen(297), Oxygen(260), Ozone(173);
    
    public static int ALTITUDE = 50000;
    private int gasConstant;
    
    private Gases(int gasConstant){
        this.gasConstant = gasConstant;
    }

    public int getGasConstant() {
        return gasConstant;
    }
    
}
