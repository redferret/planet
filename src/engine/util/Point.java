

package engine.util;

/**
 *
 * @author Richard DeSilvey
 */
public class Point {
    private int x, y;

    public Point(Point toCopy){
        x = toCopy.x;
        y = toCopy.y;
    }
    
    public Point(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
    
}
