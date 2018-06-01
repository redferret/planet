
import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.util.concurrent.MThread;
import engine.util.task.Boundaries;
import engine.util.task.TaskAdapter;
import java.util.List;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.Before;

/**
 *
 * @author Richard
 */
public class EventsTest {

  private EventTestSurface surface;
  
  @Before
  public void setup() {
    surface = new EventTestSurface(2, 1, 2);
    surface.reset();
  }
  
  @Test
  public void executeEventsTest() {
    
  }

}

class EventTestSurface extends SurfaceMap<EventTestCell> {


  public EventTestSurface(int planetWidth, int surfaceThreadDelay, int threadCount) {
    super(planetWidth, surfaceThreadDelay);
    setupThreads(threadCount, surfaceThreadDelay);
    setupDefaultMap(planetWidth, threadCount);
    addTaskToThreads(new EventTask());
  }

  @Override
  public void reset() {
    buildMap();
  }

  @Override
  public EventTestCell generateCell(int x, int y) {
    return new EventTestCell(x, y);
  }

  @Override
  public boolean load() {
    return false;
  }

  private class EventTask extends TaskAdapter {

    @Override
    public void perform(int x, int y) {
      EventTestCell cell = getCellAt(x + 1, y);
      if (isNotChildCell(cell)) {
        cell.getParentThread().pushEvent(() -> {
          cell.update();
        });
      }
    }

    @Override
    public void before() {
    }

    @Override
    public void after() {
    }
  }

}

class EventTestCell extends Cell {

  private int counter;

  public EventTestCell(int x, int y) {
    super(x, y);
    counter = 0;
  }

  public int getCount() {
    return counter;
  }
  
  public void update() {
    counter++;
  }

  @Override
  public List<Integer[]> render(List<Integer[]> settings) {
    return settings;
  }

}