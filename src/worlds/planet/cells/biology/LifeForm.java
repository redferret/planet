
package worlds.planet.cells.biology;

import java.awt.Color;

/**
 *
 * @author Richard DeSilvey
 */
public class LifeForm {
    
    public static enum LifeFormType {Plant, Animal, Robot};
    
    /**
     * The weight of the animal in kilograms.
     */
    private int size;
    
    private int metabolicRate;
    
    /**
     * The amount of energy this life form has. When a life form eats, this
     * value goes up otherwise it decays over time based on the size of the 
     * animal.
     */
    private int energy;
    
    private LifeFormType type;
    
    private Color color;
    
    public LifeForm(int size, int energy, LifeFormType type, Color c){
        this.size = size;
        this.color = c;
        this.type = type;
        this.energy = energy;
        metabolicRate = 0;
    }
    
    public Color getColor() {
        return color;
    }

    public int getCapacity() {
        return size;
    }

    public int getConsumptionRate() {
        return 0;
    }
    
    public void changeEnergy(int amt){
        energy += amt;
        energy = (energy > size) ? size : ((energy <= 0) ? 0 : energy);
    }
    
    public void setEnergy(int energy){
        this.energy = energy;
    }
    
    public int getEnergy() {
        return energy;
    }

    public LifeForm copy(){
        return new LifeForm(size, energy, type, color);
    }
    
}
