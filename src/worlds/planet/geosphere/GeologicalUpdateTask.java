

package worlds.planet.geosphere;

import engine.util.task.Task;
import worlds.planet.PlanetCell;
import worlds.planet.geosphere.Geosphere;

import static worlds.planet.Planet.TimeScale.Geological;
import static worlds.planet.Planet.TimeScale.None;
import static worlds.planet.Planet.instance;
import static worlds.planet.Surface.GEOUPDATE;
import static worlds.planet.Surface.planetAge;

/**
 * Updates the basic geological elements such as the heat of the mantel
 * and the height of each cell (if not in the geological timescale).
 * 
 * @author Richard DeSilvey
 */
public abstract class GeologicalUpdateTask extends Task {

    private Geosphere geosphereRef;
    
    public GeologicalUpdateTask(Geosphere geosphere) {
        geosphereRef = geosphere;
    }
    
    public void updateGeology(PlanetCell cell) {

        // Update the geosphere
        if (instance().isTimeScale(Geological)) {
            cell.cool(1);
        } else if (!instance().isTimeScale(None)) {
            if (checkForGeologicalUpdate()) {
                cell.cool(1);
                cell.updateHeight();
                timeStamp();
            }
        } else {
            cell.updateHeight();
        }
    }

    private void timeStamp() {
        long curPlanetAge = planetAge.get();
        geosphereRef.setAgeStamp(curPlanetAge);
    }

    public boolean checkForGeologicalUpdate() {
        long curPlanetAge = planetAge.get();
        long diff = (curPlanetAge - geosphereRef.getAgeStamp());
        return diff > GEOUPDATE;
    }
}
