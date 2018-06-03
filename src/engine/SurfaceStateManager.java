
package engine;

import com.jme3.app.Application;
import com.jme3.app.state.AppStateManager;
import engine.util.concurrent.MThread;
import engine.util.task.BasicTask;

/**
 *
 * @author Richard
 */
public class SurfaceStateManager extends AppStateManager {
  
  private static final MThread STATE_MTHREAD = new MThread(0);
  private static final Thread STATE_THREAD = new Thread(STATE_MTHREAD);
  
  public SurfaceStateManager(Application app) {
    super(app);
    STATE_MTHREAD.addTask(new UpdateTask());
  }

  public void start() {
    STATE_THREAD.start();
    STATE_MTHREAD.play();
  }
  
  private class UpdateTask extends BasicTask {
    @Override
    public void perform() throws Exception {
      SurfaceStateManager.super.update(0);
    }
  }

  @Override
  public void cleanup() {
    super.cleanup(); 
    STATE_MTHREAD.kill();
  }
}
