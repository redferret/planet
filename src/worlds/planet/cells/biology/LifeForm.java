
package worlds.planet.cells.biology;

import java.awt.Color;

/**
 *
 * @author Richard DeSilvey
 */
public class LifeForm {
    
    public static enum LifeFormType {Plant, Animal, Robot};
    
    /**
     * The amount of energy this life form has. When a life form eats, this
     * value goes up otherwise it decays over time based on the size of the 
     * animal.
     */
    private int energy;
    
    private LifeFormType type;
    
    private Color color;
    
    private Genetics genes;
    
    public LifeForm(int energy, LifeFormType type, Color c){
        this.color = c;
        this.type = type;
        this.energy = energy;
        genes = new Genetics();
    }
    
    public Color getColor() {
        return color;
    }

    public int getConsumptionRate() {
        return 0;
    }
    
    public void changeEnergy(int amt){

    }
    
    public void setEnergy(int energy){
        this.energy = energy;
    }
    
    public int getEnergy() {
        return energy;
    }

    public LifeForm copy(){
        return new LifeForm(energy, type, color);
    }
    
}
