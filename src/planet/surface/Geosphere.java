

package planet.surface;

import java.util.ArrayList;
import java.util.List;
import planet.Planet;
import planet.cells.GeoCell;
import planet.cells.HydroCell;
import planet.cells.Mantel;
import planet.enums.Layer;

import static planet.cells.HydroCell.oceanSedimentCapacity;
import static planet.enums.Layer.BASALT;
import static planet.enums.Layer.LAVA;
import static planet.enums.Layer.SANDSTONE;
import static planet.enums.Layer.SEDIMENT;
import static planet.enums.Layer.SHALE;
import static planet.interfaces.SurfaceMap.DIR_X_INDEX;
import static planet.interfaces.SurfaceMap.DIR_Y_INDEX;
import static planet.surface.Surface.rand;
import static planet.util.Tools.calcDepth;
import static planet.util.Tools.calcHeight;
import static planet.util.Tools.calcMass;
import static planet.util.Tools.changeMass;
import static planet.util.Tools.checkXBounds;
import static planet.util.Tools.checkYBounds;
import static planet.util.Tools.clamp;
import static planet.util.Tools.getLowestCellFrom;

/**
 * Contains all logic that works on the geology of the planet.
 * @author Richard DeSilvey
 */
public abstract class Geosphere extends Surface {

    private long strataBuoyancyStamp;
    
    public Geosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
    }

    public void depositSediment(int x, int y) {

        float maxPressure;
        long age;
        
        Stratum stratum;
        GeoCell cell = getCellAt(x, y);
        cell.getSedimentBuffer().applySedimentBuffer();
        stratum = cell.peekTopStratum();

        if (stratum == null) {
            return;
        }

        formNewRock(cell, calcDepth(SEDIMENT, 9.8f, 400));
        age = cell.getAge();

        if (age > 1E8) {
            maxPressure = 886655;
        } else {
            maxPressure = (float) Math.exp((-((age - 717928560.98) / 5E7)) + 25000);
        }
        melt(cell, calcDepth(cell.getDensity(), 9.8f, maxPressure));

    }

    public void melt(GeoCell cell, float maxHeight) {

        float height, diff, massToChange;
        Stratum bottom = cell.peekBottomStratum();
        if (bottom == null) {
            return;
        }
        Layer bottomType = cell.peekBottomStratum().getLayer();

        height = cell.getHeight();

        if (height > maxHeight) {
            diff = (height - maxHeight) / 2f;
            massToChange = calcMass(diff, Planet.self().getBase(), bottomType);
            cell.remove(massToChange, false, false);
        }

    }

    public void formNewRock(GeoCell cell, float maxHeight) {

        float height, diff, massBeingDeposited;
        Layer depositType;
        GeoCell.SedimentBuffer eb = cell.getSedimentBuffer();
        
        height = calcHeight(eb.getSediments(), Planet.self().getBase(), SEDIMENT);
        if (height > maxHeight) {

            diff = (height - maxHeight);

            massBeingDeposited = calcMass(diff, Planet.self().getBase(), SEDIMENT);
            depositType = (((HydroCell)cell).getOceanMass() > 900) ? SHALE : SANDSTONE;

            eb.updateSurfaceSedimentMass(-massBeingDeposited);

            massBeingDeposited = changeMass(massBeingDeposited, SEDIMENT, depositType);
            cell.add(depositType, massBeingDeposited, true);

        }
    }

    public void spreadToLowest(GeoCell spreadFrom, boolean geoScale) {

        dust(spreadFrom);

        if (geoScale) {
            convertTopLayer(spreadFrom, calcHeight(100, Planet.self().getBase(), SEDIMENT));
        }

        if ((((HydroCell)spreadFrom).getOceanMass() > oceanSedimentCapacity) || geoScale) {

            int maxCellCount = 8;
            ArrayList<GeoCell> lowestList = new ArrayList<>(maxCellCount);
            getLowestCells(spreadFrom, lowestList, maxCellCount);
            spread(lowestList, spreadFrom);
        }
    }

    public void convertTopLayer(GeoCell spreadFrom, float height) {

        float rockMass, sandMass;

        if (spreadFrom.peekTopStratum() == null) {
            return;
        }

        GeoCell.SedimentBuffer eb = spreadFrom.getSedimentBuffer();
        Layer rockLayer = spreadFrom.peekTopStratum().getLayer();
        // Wind erosion
        if (eb.getSediments() < 50 && !spreadFrom.hasOcean()
                && spreadFrom.getMoltenRockFromSurface() < 300) {

            rockMass = calcMass(height, Planet.self().getBase(), SEDIMENT);
            rockMass = spreadFrom.erode(rockMass);

            sandMass = changeMass(rockMass, rockLayer, SEDIMENT);

            eb.updateSurfaceSedimentMass(sandMass);
        }
    }

    public void getLowestCells(GeoCell spreadFrom, List<GeoCell> lowestList, int max) {

        int tx, ty, mx, my;
        int x = spreadFrom.getX(), y = spreadFrom.getY();
        int xl = DIR_X_INDEX.length;
        GeoCell spreadTo;

        for (int s = 0; s < xl; s++) {

            tx = x + DIR_X_INDEX[s];
            ty = y + DIR_Y_INDEX[s];

            // Check the boundaries
            mx = checkXBounds(tx, Planet.self().getGridSize());
            my = checkYBounds(ty, Planet.self().getGridSize());

            spreadTo = getCellAt(mx, my);

            if (spreadTo.getHeightWithoutOceans() < spreadFrom.getHeightWithoutOceans()) {
                if (lowestList.size() < max) {
                    lowestList.add(spreadTo);
                } else {
                    break;
                }
            }
        }
    }

    /**
     * Selects a random cell from the given list and spreads the sediments to
     * that cell.
     *
     * @param lowestList The list of lowest cells from the central cell
     * @param spreadFrom The central cell
     */
    public void spread(ArrayList<GeoCell> lowestList, GeoCell spreadFrom) {

        GeoCell lowestGeoCell;
        GeoCell.SedimentBuffer eb = spreadFrom.getSedimentBuffer();
        GeoCell.SedimentBuffer lowestBuffer;
        float spreadFromHeight, lowestHeight, diff, mass;

        if (lowestList.size() > 0) {

            lowestGeoCell = lowestList.get(rand.nextInt(lowestList.size()));
            spreadFromHeight = spreadFrom.getHeightWithoutOceans() / 2.5f;
            lowestHeight = lowestGeoCell.getHeightWithoutOceans() / 2.5f;

            diff = (spreadFromHeight - lowestHeight) / 2.5f;

            diff = clamp(diff, -lowestHeight, spreadFromHeight);

            if (eb.getSediments() > 0) {

                mass = calcMass(diff, Planet.self().getBase(), SEDIMENT);
                eb.updateSurfaceSedimentMass(-mass);
                
                lowestBuffer = lowestGeoCell.getSedimentBuffer();
                lowestBuffer.updateSurfaceSedimentMass(mass);
            }
        }
    }

    /**
     * Updates surface lava.
     * @see planet.surface.Surface#updateGeology(int, int) 
     * @param x Cell's x
     * @param y Cell's y
     */
    public void updateLavaFlows(int x, int y) {

        GeoCell toUpdate = getCellAt(x, y);
        
        if (toUpdate.getMoltenRockFromSurface() > 300) {
            GeoCell lowest = getLowestCellFrom(toUpdate);

            if (lowest != null && lowest != toUpdate) {
                float currentCellHeight = toUpdate.getHeightWithoutOceans() / 2.5f;
                float lowestHeight = lowest.getHeightWithoutOceans() / 2.5f;
                float diff = (currentCellHeight - lowestHeight) / 2.5f;

                diff = clamp(diff, -lowestHeight, currentCellHeight);

                float mass = calcMass(diff, Planet.self().getBase(), LAVA);

                toUpdate.putMoltenRockToSurface(-mass);
                lowest.putMoltenRockToSurface(mass);
                lowest.getSedimentBuffer().removeAllSediments();
            }

            float rate = ((HydroCell)toUpdate).getOceanMass() > 300 ? 0.95f : 0.10f;

            //solidify the rock
            float massToSolidify = toUpdate.getMoltenRockFromSurface() * rate;
            toUpdate.putMoltenRockToSurface(-massToSolidify);
            massToSolidify = changeMass(massToSolidify, LAVA, BASALT);
            toUpdate.add(BASALT, massToSolidify, true);
            toUpdate.recalculateHeight();
        } else {
            toUpdate.removeAllMoltenRock();
        }
    }
    
    /**
     * Updating the surface results in updating lava flows and depositing 
     * sediments.
     * @see planet.surface.Surface#updateLavaFlows(int, int) 
     * @see planet.surface.Surface#depositSediment(int, int) 
     * @param x The x coordinate of the cell
     * @param y The y coordinate of the cell
     */
    public void updateGeology(int x, int y) {

        long curPlanetAge = planetAge.get();
        boolean geoScale = Planet.self().getTimeScale() == Planet.TimeScale.Geological;

        GeoCell cell = getCellAt(x, y);
        long diff = (curPlanetAge - strataBuoyancyStamp);
        
        // Update the geosphere
        if (geoScale) {
            spreadToLowest(cell, geoScale);
        } else {
            if (diff > GEOUPDATE) {
                spreadToLowest(cell, geoScale);
                cell.updateHeight();
                strataBuoyancyStamp = curPlanetAge;
            }
        }
        depositSediment(x, y);
        updateLavaFlows(x, y);
        cell.cool(1);
    }
    
    public void heatMantel(){
        int n = rand.nextInt(1000);
        for (int i = 0; i < n; i++){
            int x = rand.nextInt(worldSize);
            int y = rand.nextInt(worldSize);

            Mantel cell = getCellAt(x, y);
            GeoCell geo;
            cell.addHeat(100);

            if (cell.checkVolcano()){
                geo = (GeoCell)cell;
                geo.putMoltenRockToSurface(250000);
                cell.cool(200);
            }
        }
    }
    
    public void dust(GeoCell cell) {
        if (cell.getMoltenRockFromSurface() < 1) {
            cell.getSedimentBuffer().updateSurfaceSedimentMass(10);
        }
    }
    
}
