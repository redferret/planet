

package engine.util.exception;

/**
 *
 * @author Richard DeSilvey
 */
public class SurfaceThreadStarvationException extends RuntimeException{

    public SurfaceThreadStarvationException(String threadName, Object data){
        super("Starvation on thread '" + threadName
                    + "' for resource " + data.toString());
    }
}
