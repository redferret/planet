

package engine.util;

/**
 *
 * @author Richard DeSilvey
 */
public class Point {
    private float x, y;

    public Point(Point toCopy){
        x = toCopy.x;
        y = toCopy.y;
    }
    
    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public Point copy(){
    	return new Point(this);
    }
    
    public Point add(Point p) {
        this.x += p.x;
        this.y += p.y;
        return this.copy();
    }
    
    public Point mul(Point p) {
    	this.x *= p.x;
    	this.y *= p.y;
    	return this.copy();
    }
    
    public Point set(Point p) {
        this.x = p.x;
        this.y = p.y;
        return this.copy();
    }
    
    public Point neg() {
        x = -x;
        y = -y;
        return this.copy();
    }
    
    public Point truncate(){
        return new Point((int)x, (int)y);
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
        hash = 17 * hash + (int)this.x;
        hash = 17 * hash + (int)this.y;
        return hash;
    }
    
    public String toString(){
    	StringBuilder str = new StringBuilder();
    	str.append("[").append(x).append(", ").append(y).append("]");
    	return str.toString();
    }
    
}
