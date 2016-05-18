package planet.surface;

import java.util.ArrayList;
import java.util.List;
import planet.Planet;
import planet.cells.GeoCell;
import planet.cells.HydroCell;
import planet.cells.Mantel;
import planet.enums.Layer;

import static planet.enums.Layer.BASALT;
import static planet.enums.Layer.LAVA;
import static planet.enums.Layer.SANDSTONE;
import static planet.enums.Layer.SEDIMENT;
import static planet.enums.Layer.SHALE;
import static planet.surface.SurfaceMap.DIR_X_INDEX;
import static planet.surface.SurfaceMap.DIR_Y_INDEX;
import static planet.surface.Surface.rand;
import static planet.util.Tools.calcDepth;
import static planet.util.Tools.calcHeight;
import static planet.util.Tools.calcMass;
import static planet.util.Tools.changeMass;
import static planet.util.Tools.checkBounds;
import static planet.util.Tools.clamp;
import static planet.util.Tools.getLowestCellFrom;
import static planet.Planet.TimeScale.Geological;
import static planet.Planet.TimeScale.None;

/**
 * Contains all logic that works on the geology of the planet.
 *
 * @author Richard DeSilvey
 */
public abstract class Geosphere extends Surface {

    /**
     * The maximum number of cells that receive heating.
     */
    public static int heatDistributionCount;

    /**
     * The amount of heat added to a cell.
     */
    public static int thermalInc;

    /**
     * The amount of volcanic rock added to the surface when a volcano erupts.
     */
    public static float averageVolcanicMass;

    /**
     * The amount of heat lost after a volcanic eruption.
     */
    public static float volcanicHeatLoss;
    
    private long ageStamp;

    static {
        heatDistributionCount = 5;
        thermalInc = 100;
        volcanicHeatLoss = 100;
        averageVolcanicMass = 2500000;
    }

    public Geosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        ageStamp = 0;
    }

    /**
     * Add a uniformed layer on the whole surface.
     *
     * @param type The layer being added
     * @param amount The amount being added
     */
    public void addToSurface(Layer type, float amount) {
        int cellCount = Planet.self().getTotalNumberOfCells();
        for (int i = 0; i < cellCount; i++){
            getCellAt(i).add(type, amount, true);
        }
    }

    public void depositSediment(int x, int y) {

        float maxPressure;
        long age;

        GeoCell cell = getCellAt(x, y);
        cell.getSedimentBuffer().applyBuffer();

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
        if (cell.peekBottomStratum() == null) {
            return;
        }
        Layer bottomType = cell.peekBottomStratum().getLayer();

        height = cell.getHeight();

        if (height > maxHeight) {
            diff = (height - maxHeight) / 2f;
            massToChange = calcMass(diff, Planet.self().getCellArea(), bottomType);
            cell.remove(massToChange, false, false);
        }

    }

    public void formNewRock(GeoCell cell, float maxHeight) {

        float height, diff, massBeingDeposited;
        Layer depositType;
        GeoCell.SedimentBuffer eb = cell.getSedimentBuffer();

        height = calcHeight(eb.getSediments(), Planet.self().getCellArea(), SEDIMENT);
        if (height > maxHeight) {

            diff = (height - maxHeight);

            massBeingDeposited = calcMass(diff, Planet.self().getCellArea(), SEDIMENT);
            depositType = (((HydroCell) cell).getOceanMass() > 9000) ? SHALE : SANDSTONE;

            eb.updateSurfaceSedimentMass(-massBeingDeposited);

            massBeingDeposited = changeMass(massBeingDeposited, SEDIMENT, depositType);
            cell.add(depositType, massBeingDeposited, true);

        }
    }

    public void spreadToLowest(GeoCell spreadFrom) {

        dust(spreadFrom);

        float height = calcHeight(0.00001f, Planet.self().getCellArea(), SEDIMENT);
        convertTopLayer(spreadFrom, height);

        int maxCellCount = 8;
        ArrayList<GeoCell> lowestList = new ArrayList<>(maxCellCount);
        getLowestCells(spreadFrom, lowestList, maxCellCount);
        spread(lowestList, spreadFrom);

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

            rockMass = calcMass(height, Planet.self().getCellArea(), SEDIMENT);
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
            mx = checkBounds(tx, Planet.self().getGridWidth());
            my = checkBounds(ty, Planet.self().getGridWidth());

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

                mass = calcMass(diff, Planet.self().getCellArea(), SEDIMENT);
                eb.updateSurfaceSedimentMass(-mass);

                lowestBuffer = lowestGeoCell.getSedimentBuffer();
                lowestBuffer.updateSurfaceSedimentMass(mass);
            }
        }
    }

    /**
     * Updates surface lava.
     *
     * @see planet.surface.Geosphere#updateGeology(int, int)
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

                float mass = calcMass(diff, Planet.self().getCellArea(), LAVA);

                toUpdate.putMoltenRockToSurface(-mass);
                lowest.putMoltenRockToSurface(mass);
                lowest.getSedimentBuffer().removeAllSediments();
            }

            float rate = ((HydroCell) toUpdate).getOceanMass() > 300 ? 0.95f : 0.10f;

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

    public boolean checkForGeologicalUpdate() {
        long curPlanetAge = planetAge.get();
        long diff = (curPlanetAge - ageStamp);
        return diff > GEOUPDATE;
    }

    private void timeStamp() {
        long curPlanetAge = planetAge.get();
        ageStamp = curPlanetAge;
    }

    /**
     * Updating the surface results in updating lava flows and depositing
     * sediments.
     *
     * @see planet.surface.Geosphere#updateLavaFlows(int, int)
     * @see planet.surface.Geosphere#depositSediment(int, int)
     * @param x The x coordinate of the cell
     * @param y The y coordinate of the cell
     */
    public void updateGeology(int x, int y) {

        GeoCell cell = getCellAt(x, y);

        // Update the geosphere
        if (Planet.self().isTimeScale(Geological)) {
            geologicalUpdate(cell);
        } else if (!Planet.self().isTimeScale(None)) {
            if (checkForGeologicalUpdate()) {
                geologicalUpdate(cell);
                cell.updateHeight();
                timeStamp();
            }
        }
    }

    public void updateRockFormation(int x, int y){
        depositSediment(x, y);
        updateLavaFlows(x, y);
    }
    
    private void geologicalUpdate(GeoCell cell) {
        spreadToLowest(cell);
        cell.cool(1);
    }

    public void heatMantel() {
        int n = rand.nextInt(heatDistributionCount);
        for (int i = 0; i < n; i++) {
            int x = rand.nextInt(worldSize);
            int y = rand.nextInt(worldSize);

            Mantel cell = getCellAt(x, y);
            GeoCell geo;
            cell.addHeat(thermalInc);

            if (cell.checkVolcano()) {
                geo = (GeoCell) cell;
                geo.putMoltenRockToSurface(averageVolcanicMass);
                cell.cool(volcanicHeatLoss);
            }
        }
    }

    public void dust(GeoCell cell) {
        if (cell.getMoltenRockFromSurface() < 1) {
            cell.getSedimentBuffer().updateSurfaceSedimentMass(0.001f);
        }
    }

}
