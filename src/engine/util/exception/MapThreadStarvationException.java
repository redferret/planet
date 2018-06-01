package engine.util.exception;

/**
 *
 * @author Richard DeSilvey
 */
public class MapThreadStarvationException extends RuntimeException {

  public MapThreadStarvationException(String threadName, Object data) {
    super("Starvation on thread '" + threadName
            + "' for resource " + data.toString());
  }
}
