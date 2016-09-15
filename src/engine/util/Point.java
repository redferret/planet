

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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Point)){
            return false;
        }else {
            Point o = (Point)obj;
            return o.x == x && o.y == y;
        }
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 17 * hash + this.x;
        hash = 17 * hash + this.y;
        return hash;
    }
    
    
}
