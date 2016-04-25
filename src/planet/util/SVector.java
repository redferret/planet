
package planet.util;

/**
 *
 * @author Richard DeSilvey
 */
public class SVector {

    private float x, y;
    
    public SVector(){
        x = y = 0;
    }

    public SVector(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public SVector(SVector v){
        this.x = v.x;
        this.y = v.y;
    }
    
    public void add(SVector vec){
        this.x += vec.x;
        this.y += vec.y;
    }
    
    public void mul(float s){
        this.x *= s;
        this.y *= s;
    }
    
    public void set(float x, float y){
        this.x = x;
        this.y = y;
    }
    
    public float getX(){
        return x;
    }
    
    public float getY(){
        return y;
    }
    
    public float mag(){
        return (float)Math.sqrt((double)((x*x)+(y*y)));
    }
}
