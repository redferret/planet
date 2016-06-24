

package planet.util;

/**
 *
 * @author Richard DeSilvey
 */
public abstract class BasicTask implements Task {

    @Override
    public final void perform(int x, int y) {
    }
    
    public abstract void perform();
    
    @Override
    public final boolean check() {
        perform();
        return false;
    }

    
}
