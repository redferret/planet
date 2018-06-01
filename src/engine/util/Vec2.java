package engine.util;

/**
 *
 * @author Richard DeSilvey
 */
public class Vec2 {

  private float x, y;

  public Vec2() {
    this(0, 0);
  }

  public Vec2(Vec2 toCopy) {
    this(toCopy.x, toCopy.y);
  }

  public Vec2(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public Vec2 copy() {
    return new Vec2(this);
  }
  
  public void add(Vec2 p) {
    this.x += p.x;
    this.y += p.y;
  }

  public void mul(Vec2 p) {
    this.x *= p.x;
    this.y *= p.y;
  }
  
  public void div(float n) {
    this.x /= n;
    this.y /= n;
  }

  public void normalize() {
    float mag = mag();
    if (mag != 0) {
      div(mag);
    }
  }
  
  public void set(Vec2 p) {
    this.x = p.x;
    this.y = p.y;
  }

  public void neg() {
    x = -x;
    y = -y;
  }

  public float mag() {
    return (float) Math.sqrt((x * x + y * y));
  }

  public Vec2 truncate() {
    return new Vec2((int) x, (int) y);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null || !(obj instanceof Vec2)) {
      return false;
    } else {
      Vec2 o = (Vec2) obj;
      return o.x == x && o.y == y;
    }
  }

  @Override
  public int hashCode() {
    int hash = 5;
    hash = 17 * hash + (int) this.x;
    hash = 17 * hash + (int) this.y;
    return hash;
  }

  public String toString() {
    StringBuilder str = new StringBuilder();
    str.append("[").append(x).append(", ").append(y).append("]");
    return str.toString();
  }

  public boolean isZero() {
    return mag() == 0;
  }

  public void zero() {
    this.set(new Vec2());
  }

}
