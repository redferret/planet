

package worlds.planet.cells.biology;

import java.util.List;

/**
 *
 * @author Richard DeSilvey
 */
public class Genetics {

    /**
     * The genes that deal with processing certain foods. 1f = fully carnivore
     * while -1f = fully herbivore and 0 being omnivore. This is used as a 
     * choice for the life form. -1f means the lifeform always picks plants
     * to eat, likewise with 1f the lifeform picks meat always 0 the lifeform
     * will randomly pick what to eat.
     */
    private float foodProcessing;
    
    /**
     * The rate at which this life forms reproduces. Promiscuous or not.
     */
    private float reproductionRate;
    
    /**
     * The size of this animal is represented in kilograms. The larger the
     * creature the more energy they need to consume. Also when eating
     * other lifeforms they will pick a smaller mass to consume.
     */
    private float size;
    
    /**
     * LifeForms have different types of food, but only certain life forms can
     * process other life forms. LifeForms can only process so many types of
     * forms. In the simulation a cap is placed on this list size.
     */
    private List<EdibleForm> edibleForms; 
}
