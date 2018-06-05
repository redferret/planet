package worlds;

import engine.util.Delay;
import engine.util.concurrent.AtomicFloat;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import java.util.ArrayList;
import java.util.List;
import worlds.planet.PlanetCell;
import worlds.planet.geosphere.Crust;
import worlds.planet.geosphere.Lithosphere;

public class MinMaxHeightFactory implements TaskFactory {

  private final List<MinMaxHeightTask> taskReferences;
  private final Lithosphere surface;

  public MinMaxHeightFactory(Lithosphere surface) {
    taskReferences = new ArrayList<>();
    this.surface = surface;
  }

  public float getHighestHeight() {
    float highest = Integer.MIN_VALUE;

    for (MinMaxHeightTask task : taskReferences) {
      float testHeight = task.getHighestHeight();
      if (testHeight > highest) {
        highest = testHeight;
      }
    }
    return highest;
  }

  public float getLowestHeight() {

    float lowestHeight = Integer.MAX_VALUE;

    for (MinMaxHeightTask task : taskReferences) {
      float testHeight = task.getLowestHeight();
      if (testHeight < lowestHeight) {
        lowestHeight = testHeight;
      }
    }

    return lowestHeight;
  }

  @Override
  public Task buildTask() {
    MinMaxHeightTask task = new MinMaxHeightTask();
    taskReferences.add(task);
    return task;
  }

  private class MinMaxHeightTask extends Task {

    private Delay delayTask;
    private float absLowestHeight, absHighestHeight;
    private AtomicFloat absLowest, absHighest;

    @Override
    public void construct() {
      absLowest = new AtomicFloat(0);
      absHighest = new AtomicFloat(0);
      delayTask = new Delay(50);
    }

    @Override
    public void before() {
      absLowestHeight = absLowestHeight < 0 ? 0 : absLowestHeight;
      absLowest.set(absLowestHeight);

      absHighest.set(absHighestHeight);

      absLowestHeight = Integer.MAX_VALUE;
      absHighestHeight = Integer.MIN_VALUE;
    }

    @Override
    public void perform(int x, int y) {
      updateMinMaxHeight(x, y);
    }

    @Override
    public void after() {
    }

    private void updateMinMaxHeight(int x, int y) {
      Crust cell = surface.getCellAt(x, y);
      float cellHeight = cell.getHeightWithoutOceans();
      if (cellHeight < absLowestHeight) {
        absLowestHeight = cellHeight;
      }

      if (cellHeight > absHighestHeight) {
        absHighestHeight = cellHeight;
      }
    }

    public float getHighestHeight() {
      return absHighest.get();
    }

    public float getLowestHeight() {
      return absLowest.get();
    }

    @Override
    public boolean check() {
      return delayTask.check();
    }

  }
}
