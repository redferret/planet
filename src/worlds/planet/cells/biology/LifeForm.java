
package worlds.planet.cells.biology;

import java.awt.Color;
import java.util.Random;

/**
 *
 * @author Richard DeSilvey
 */
public class LifeForm {
    
    private int capacity, metabolicRate, energy;
    private Color color;
    private float energyTransfer;
    
    public LifeForm(int capacity, int eatRate, int initEnergy, float energyTransfer, Color c){
        
        this.capacity = capacity;
        this.metabolicRate = eatRate;
        this.color = c;
        
        this.energy = initEnergy;
        this.energyTransfer = energyTransfer;
        
        
    }
    
    public Color getColor() {
        return color;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getConsumptionRate() {
        return metabolicRate;
    }
    
    private static int checkBounds(int b, int max, int min){
        
        return (b >= max) ? max : ((b <= min) ? min : b);
        
    }
    
    public void changeEnergy(int amt){
        energy += amt;
        energy = (energy > capacity) ? capacity : ((energy <= 0) ? 0 : energy);
    }
    
    public void setEnergy(int energy){
        this.energy = energy;
    }
    
    public int getEnergy() {
        return energy;
    }

    public float getEnergyTransfer() {
        return energyTransfer;
    }
    
    public static LifeForm mate(LifeForm m1, LifeForm m2){
        
        int energyCapacity = (m1.getCapacity() + m2.getCapacity()) / 2;
        int consumptionRate = (m1.getConsumptionRate() + m2.getConsumptionRate()) / 2;
        float energyTransfer = (m1.getEnergyTransfer() + m2.getEnergyTransfer()) / 2;
        
        if ((Math.abs(m1.getColor().getRed() - m2.getColor().getRed()) > 25) &&
                (Math.abs(m1.getColor().getGreen() - m2.getColor().getGreen()) > 25) &&
                (Math.abs(m1.getColor().getBlue() - m2.getColor().getBlue()) > 25)){
            
            return null;
            
        }
        
        int red = (m1.getColor().getRed() + m1.getColor().getRed()) / 2;
        int green = (m1.getColor().getGreen() + m1.getColor().getGreen()) / 2;
        int blue = (m1.getColor().getBlue() + m1.getColor().getBlue()) / 2;
        
        Random r = new Random();
        
        if (r.nextFloat() <= .4){
        
            int amt = Math.max(1, r.nextInt(3));
            energyCapacity = (r.nextBoolean()) ? energyCapacity + amt : energyCapacity - amt;
            
            amt = Math.max(1, r.nextInt(3));
            consumptionRate = (r.nextBoolean()) ? consumptionRate + amt : consumptionRate - amt;
            
            energyTransfer = (r.nextBoolean()) ? energyTransfer + 0.10f : energyTransfer - 0.10f;

            amt = Math.max(1, r.nextInt(3));
            red = (r.nextBoolean()) ? red + amt : red - amt;
            
            amt = Math.max(1, r.nextInt(3));
            green = (r.nextBoolean()) ? green + amt : green - amt;
            
            amt = Math.max(1, r.nextInt(3));
            blue = (r.nextBoolean()) ? blue + amt : blue - amt;

            energyTransfer = (energyTransfer <= 0) ? 0 : (energyTransfer > 1) ? 1 : energyTransfer;
            
            consumptionRate = checkBounds(consumptionRate, 500, 10);
            energyCapacity = checkBounds(energyCapacity, 500, 10);
            red = checkBounds(red, 255, 0);
            green = checkBounds(green, 255, 0);
            blue = checkBounds(blue, 255, 0);
        
        }
        
        int energy = (int)(m1.getEnergyTransfer() * m1.getEnergy()) + (int)(m2.getEnergyTransfer() * m2.getEnergy());
        
        m1.changeEnergy(-(int)(m1.getEnergyTransfer() * m1.getEnergy()));
        m2.changeEnergy(-(int)(m2.getEnergyTransfer() * m2.getEnergy()));
        
        LifeForm lf = new LifeForm(energyCapacity, consumptionRate, energy, energyTransfer, new Color(red, green, blue));
        
        return lf;
        
    }
    
    public LifeForm copy(){
        return new LifeForm(capacity, metabolicRate, energy, energyTransfer, color);
    }
    
}
