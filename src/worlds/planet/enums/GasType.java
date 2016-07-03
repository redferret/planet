
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
 * source: http://www.esrl.noaa.gov/csd/assessments/ozone/2010/twentyquestions/Q2.pdf
 * 
 * @author Richard DeSilvey
 */
public enum GasType {
    
    Argon(39.948f), CarbonDioxide(44.01f), CarbonMonoxide(28.011f), Chlorine(70.906f), 
    Methane(16.043f), Nitrogen(28.0134f), Oxygen(32f), Ozone(48), WaterVapor(18);
    
    public static int MAX_ALTITUDE = 50000;
    private float moles;
    
    private GasType(float moles){
        this.moles = moles;
    }

    public float getMoles() {
        return moles;
    }
    
}
