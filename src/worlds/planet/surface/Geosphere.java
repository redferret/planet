package worlds.planet.surface;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import engine.util.BasicTask;
import worlds.planet.cells.geology.GeoCell;
import worlds.planet.cells.HydroCell;
import worlds.planet.cells.PlanetCell;
import worlds.planet.enums.Layer;
import worlds.planet.cells.geology.GeoCell.SedimentBuffer;
import engine.util.Delay;
import engine.util.Task;
import engine.util.TaskAdapter;
import engine.util.TaskFactory;

import static worlds.planet.enums.Layer.BASALT;
import static worlds.planet.enums.Layer.LAVA;
import static worlds.planet.enums.Layer.SANDSTONE;
import static worlds.planet.enums.Layer.SEDIMENT;
import static worlds.planet.enums.Layer.SHALE;
import static engine.util.Tools.calcDepth;
import static engine.util.Tools.calcHeight;
import static engine.util.Tools.calcMass;
import static engine.util.Tools.changeMass;
import static engine.util.Tools.checkBounds;
import static engine.util.Tools.clamp;
import static worlds.planet.Planet.instance;
import static worlds.planet.Planet.TimeScale.Geological;
import static worlds.planet.Planet.TimeScale.None;
import static worlds.planet.surface.Surface.planetAge;
import static worlds.planet.surface.Surface.random;

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

    public static boolean drawSediments;
    private static Random rand;

    static {
        heatDistributionCount = 5;
        thermalInc = 100;
        volcanicHeatLoss = 100;
        averageVolcanicMass = 25000;
        rand = new Random();
        drawSediments = true;
    }

    public Geosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        ageStamp = 0;
        produceTasks(new GeologicalUpdateFactory());
        produceTasks(new WindErosionFactory());
        produceTasks(new RockFormationFactory());
        addTaskToThreads(new SpreadSedimentTask());
        addTask(new HeatMantel());
    }

    /**
     * Add a uniformed layer on the whole surface.
     *
     * @param type The layer being added
     * @param amount The amount being added
     */
    public void addToSurface(Layer type, float amount) {
        int cellCount = getTotalNumberOfCells();
        for (int i = 0; i < cellCount; i++) {
            getCellAt(i).add(type, amount, true);
        }
    }

    public void addLavaToSurface(float amount) {
        int cellCount = getTotalNumberOfCells();
        for (int i = 0; i < cellCount; i++) {
            getCellAt(i).putMoltenRockToSurface(amount);
        }
    }

    public void meltRock(GeoCell cell) {
        float age = cell.getAge(), maxPressure;

        if (age > 1E8) {
            maxPressure = 886655;
        } else {
            maxPressure = (float) Math.exp((-((age - 717928560.98) / 5E7)) + 25000);
        }
        melt(cell, calcDepth(cell.getDensity(), 9.8f, maxPressure));
    }

    private void melt(GeoCell cell, float maxHeight) {

        float height, diff, massToChange;
        if (cell.peekBottomStratum() == null) {
            return;
        }
        Layer bottomType = cell.peekBottomStratum().getLayer();

        height = cell.getHeight();

        if (height > maxHeight) {
            diff = (height - maxHeight) / 2f;
            massToChange = calcMass(diff, instance().getCellArea(), bottomType);
            cell.remove(massToChange, false, false);
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
            mx = checkBounds(tx, getGridWidth());
            my = checkBounds(ty, getGridWidth());

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

    private float calcFlow(float mass) {
        return (float) Math.pow(90f * mass, 0.5f);
    }

    public static float windErosionConstant;
    
    static {
        windErosionConstant = 5f;
    }
    
    private class WindErosionFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new WindErosionTask();
        }

        private class WindErosionTask implements Task {

            private Delay delay;
            
            public WindErosionTask() {
                delay = new Delay(1);
            }

            @Override
            public void before() {
            }
            
            @Override
            public void perform(int x, int y) {
                windErosion(getCellAt(x, y));
            }

            public void windErosion(GeoCell spreadFrom) {
                float height = calcHeight(windErosionConstant, instance().getCellArea(), SEDIMENT);
                convertTopLayer(spreadFrom, height);
            }

            public void convertTopLayer(GeoCell spreadFrom, float height) {

                float rockMass, sandMass;

                if (spreadFrom.peekTopStratum() == null) {
                    return;
                }

                SedimentBuffer eb = spreadFrom.getSedimentBuffer();
                Layer rockLayer = spreadFrom.peekTopStratum().getLayer();
                // Wind erosion
                if (eb.getSediments() == 0 && !spreadFrom.hasOcean()
                        && spreadFrom.getMoltenRockFromSurface() < 1) {

                    rockMass = calcMass(height, instance().getCellArea(), SEDIMENT);
                    rockMass = spreadFrom.erode(rockMass);

                    sandMass = changeMass(rockMass, rockLayer, SEDIMENT);

                    eb.updateSurfaceSedimentMass(sandMass);
                }
            }

            @Override
            public boolean check() {
                return delay.check();
            }

            @Override
            public void after() {
            }

        }

    }

    private class SpreadSedimentTask extends TaskAdapter {

        @Override
        public void before() {
        }
        
        @Override
        public void perform(int x, int y) {
            spreadToLowest(getCellAt(x, y));
        }

        public void spreadToLowest(GeoCell spreadFrom) {
            int maxCellCount = 8;
            ArrayList<GeoCell> lowestList = new ArrayList<>(maxCellCount);
            getLowestCells(spreadFrom, lowestList, maxCellCount);
            spread(lowestList, spreadFrom);
        }

        /**
         * Selects a random cell from the given list and spreads the sediments
         * to that cell.
         *
         * @param lowestList The list of lowest cells from the central cell
         * @param spreadFrom The central cell
         */
        public void spread(ArrayList<GeoCell> lowestList, GeoCell spreadFrom) {

            GeoCell lowestGeoCell;
            SedimentBuffer eb = spreadFrom.getSedimentBuffer();
            SedimentBuffer lowestBuffer;
            float spreadFromHeight, lowestHeight, diff, mass;

            if (lowestList.size() > 0) {

                lowestGeoCell = lowestList.get(random.nextInt(lowestList.size()));
                spreadFromHeight = spreadFrom.getHeightWithoutOceans() / 2.5f;
                lowestHeight = lowestGeoCell.getHeightWithoutOceans() / 2.5f;

                diff = (spreadFromHeight - lowestHeight) / 2.5f;

                diff = clamp(diff, -lowestHeight, spreadFromHeight);

                if (eb.getSediments() > 0) {

                    mass = calcMass(diff, instance().getCellArea(), SEDIMENT);
                    mass = calcFlow(mass);

                    eb.updateSurfaceSedimentMass(-mass);

                    lowestBuffer = lowestGeoCell.getSedimentBuffer();
                    lowestBuffer.updateSurfaceSedimentMass(mass);
                }
            }
        }

        @Override
        public void after() {
        }
    }

    private class GeologicalUpdateFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new GeologicalUpdate();
        }

        private class GeologicalUpdate implements Task {

            private Delay geologicDelay;

            public GeologicalUpdate() {
                geologicDelay = new Delay(150);
            }

            @Override
            public void perform(int x, int y) {
                updateGeology(x, y);
            }

            /**
             * Updating the surface results in updating lava flows and
             * depositing sediments.
             *
             * @see planet.surface.Geosphere#updateLavaFlows(int, int)
             * @see planet.surface.Geosphere#depositSediment(int, int)
             * @param x The x coordinate of the cell
             * @param y The y coordinate of the cell
             */
            public void updateGeology(int x, int y) {

                GeoCell cell = getCellAt(x, y);

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
                ageStamp = curPlanetAge;
            }

            public boolean checkForGeologicalUpdate() {
                long curPlanetAge = planetAge.get();
                long diff = (curPlanetAge - ageStamp);
                return diff > GEOUPDATE;
            }

            @Override
            public boolean check() {
                return geologicDelay.check();
            }

            @Override
            public void before() {
            }

            @Override
            public void after() {
            }
        }
    }

    private class HeatMantel extends BasicTask {

        private Delay mantelHeatingDelay;

        public HeatMantel() {
            mantelHeatingDelay = new Delay(75);
        }

        @Override
        public void perform() {
            if (mantelHeatingDelay.check()) {
                if (!PlanetSurface.suppressMantelHeating) {
                    heatMantel();
                }
            }
        }

        public void heatMantel() {
            int totalCells = getTotalNumberOfCells();
            int index = random.nextInt(totalCells);

            PlanetCell cell = getCellAt(index);
            cell.addHeat(thermalInc);

            if (cell.checkVolcano()) {
                cell.putMoltenRockToSurface(averageVolcanicMass);
                cell.cool(volcanicHeatLoss);
                cell.addOceanMass(0.001f);
            }
        }

        @Override
        public void before() {
        }

        @Override
        public void after() {
        }
    }

    private class RockFormationFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new RockFormation();
        }

        private class RockFormation implements Task {

            private Delay updateDelay;

            public RockFormation() {
                updateDelay = new Delay(5);
            }

            @Override
            public void before() {
            }
            
            @Override
            public void perform(int x, int y) {
                depositSediment(x, y);
                updateLavaFlows(x, y);
            }

            public void depositSediment(int x, int y) {
                GeoCell cell = getCellAt(x, y);
                cell.getSedimentBuffer().applyBuffer();
                formSedimentaryRock(cell, calcDepth(SEDIMENT, 9.8f, 500));
            }

            public void formSedimentaryRock(GeoCell cell, float maxHeight) {

                float height, diff, massBeingDeposited;
                Layer depositType;
                SedimentBuffer eb = cell.getSedimentBuffer();

                height = calcHeight(eb.getSediments(), instance().getCellArea(), SEDIMENT);
                if (height > maxHeight) {

                    diff = (height - maxHeight);

                    massBeingDeposited = calcMass(diff, instance().getCellArea(), SEDIMENT);
                    depositType = (((HydroCell) cell).getOceanMass() > 9000) ? SHALE : SANDSTONE;

                    eb.updateSurfaceSedimentMass(-massBeingDeposited);

                    massBeingDeposited = changeMass(massBeingDeposited, SEDIMENT, depositType);
                    cell.add(depositType, massBeingDeposited, true);

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

                if (toUpdate.getMoltenRockFromSurface() > 10) {
                    int maxCellCount = 5;
                    ArrayList<GeoCell> lowestList = new ArrayList<>(maxCellCount);
                    getLowestCells(toUpdate, lowestList, maxCellCount);

                    if (!lowestList.isEmpty()) {
                        int rIndex = rand.nextInt(lowestList.size());
                        GeoCell lowest = lowestList.get(rIndex);

                        if (lowest != null && lowest != toUpdate) {
                            float currentCellHeight = toUpdate.getHeightWithoutOceans() / 2.5f;
                            float lowestHeight = lowest.getHeightWithoutOceans() / 2.5f;
                            float diff = (currentCellHeight - lowestHeight) / 2.5f;

                            diff = clamp(diff, -lowestHeight, currentCellHeight);

                            float mass = calcMass(diff, instance().getCellArea(), LAVA);

                            toUpdate.putMoltenRockToSurface(-mass);
                            float sediments = lowest.getSedimentBuffer().removeAllSediments();
                            lowest.putMoltenRockToSurface(mass + sediments);
                        }
                    }

                    float rate = ((HydroCell) toUpdate).getOceanMass() > 1 ? 0.35f : 0.05f;

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

            @Override
            public boolean check() {
                return updateDelay.check();
            }

            @Override
            public void after() {
            }
        }
    }
}
