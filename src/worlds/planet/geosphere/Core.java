/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package worlds.planet.geosphere;

import engine.surface.Cell;
import engine.surface.SurfaceMap;
import engine.surface.SurfaceThreads;
import engine.util.concurrent.AtomicFloat;

/**
 *
 * @author Richard
 */
public class Core extends SurfaceMap {

  public Core(int totalSize, SurfaceThreads surfaceThreads) {
    super("Core", totalSize, surfaceThreads);
    setupDefaultMap(surfaceThreads.getThreadCount());
    reset();
  }

  @Override
  public Cell generateCell(int x, int y) {
    return new Cell(x, y, 8000) {
      @Override
      public float getHeatConductivity() {
        return 45.0f;
      }
    };
  }
  
}
