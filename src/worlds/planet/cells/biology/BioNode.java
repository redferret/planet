

package worlds.planet.cells.biology;

import engine.cells.Cell;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Contains life forms
 * @author Richard DeSilvey
 */
public class BioNode extends Cell {

    private static Random rand = new Random();
    private List<LifeForm> animalLife; /*The food chain for animal life*/
    private List<LifeForm> plantLife; /*The plant and bacterial food chain*/
    
    public BioNode(int x, int y) {
        super(x, y);
        animalLife = new ArrayList<>();
        plantLife = new ArrayList<>();
    }

    public boolean hasLife(){
        return !animalLife.isEmpty() || !plantLife.isEmpty();
    }
    
    /**
     * The neighbors next to this node. These nodes contain life, either plant
     * or animal.
     * @param neighbors The nodes that contain life that are neighbors to this
     * node.
     */
    public void update(BioNode[] neighbors){
        
        plantLife.forEach(lifeForm -> {
            
        });
        
        animalLife.forEach(lifeForm -> {
            
        });
        
        // Does the life form need to die?
        if ((neighbors.length < 2 || neighbors.length > 3)) {
            // kill life form
        // Can the cell reproduce?
        } else if (neighbors.length == 3 && !hasLife()) {

            int m1 = rand.nextInt(3);
            int m2 = rand.nextInt(3);

            while (m1 == m2) {
                m1 = rand.nextInt(3);
            }

//                    LifeForm lf = LifeForm.mate(neighbors[m1], neighbors[m2]);
//                    if (lf != null) {
//                        gridNode[x][y].setAlive(lf);
//                    }
        }

        // Remove energy from enviornment or eat prey.
        // Plants get energy from sun, animals that eat plants eat
        // the plants for energy, carnivors will eat animals and may be eaten
        // by other larger carnivors.
        // Plants suck away nutrients washed out by erosion and suck away water.
        // Solar energy is used to generate joules based on
        // the size of the plant.
        if (hasLife()) {

//                    LifeForm lf = gridNode[x][y].getLifeForm();
//
//                    int output = lf.getCapacity();
//                    int rate = lf.getConsumptionRate();
//
//                    lf.changeEnergy(-output);
//
//                    if (gridNode[x][y].getFoodStock() > 0) {
//                        gridNode[x][y].changeFood(-rate);
//                        lf.changeEnergy(rate);
//                    }
//
//                    if (lf.getEnergy() <= 0) {
//                        gridNode[x][y].setDead();
//                        return;
//                    }
        }
    }
    
    @Override
    public List<Integer[]> render(List<Integer[]> settings) {
        return settings;
    }

}
