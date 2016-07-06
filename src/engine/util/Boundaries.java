

package engine.util;

/**
 *
 * @author Richard DeSilvey
 */
public final class Boundaries {

    private int lowerXBound, upperXBound, lowerYBound, upperYBound;

    public Boundaries(int lowerBound, int upperBound){
        this(lowerBound, upperBound, lowerBound, upperBound);
    }
    
    public Boundaries(int lowerXBound, int upperXBound, int lowerYBound, int upperYBound) {
        this.lowerXBound = lowerXBound;
        this.upperXBound = upperXBound;
        this.lowerYBound = lowerYBound;
        this.upperYBound = upperYBound;
    }

    public int getLowerXBound() {
        return lowerXBound;
    }

    public int getLowerYBound() {
        return lowerYBound;
    }

    public int getUpperXBound() {
        return upperXBound;
    }

    public int getUpperYBound() {
        return upperYBound;
    }
    
    
    
}

