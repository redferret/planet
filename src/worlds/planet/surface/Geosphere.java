package worlds.planet.surface;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import worlds.planet.Planet;
import worlds.planet.cells.geology.GeoCell;
import worlds.planet.cells.PlanetCell;
import worlds.planet.enums.Layer;
import worlds.planet.cells.geology.GeoCell.SedimentBuffer;
import worlds.planet.cells.geology.Stratum;
import worlds.planet.enums.RockType;
import worlds.planet.enums.SilicateContent;
import worlds.planet.cells.geology.GeoCell.MoltenRockLayer;
import engine.util.Delay;
import engine.util.Point;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import engine.util.Tools;
import engine.util.task.BasicTask;
import engine.util.task.Boundaries;
import engine.util.task.CompoundTask;
import static engine.util.Tools.calcDepth;
import static engine.util.Tools.calcHeight;
import static engine.util.Tools.calcMass;
import static engine.util.Tools.changeMass;
import static engine.util.Tools.checkBounds;
import static engine.util.Tools.clamp;
import engine.util.concurrent.AtomicData;
import static worlds.planet.Planet.TimeScale.Geological;
import static worlds.planet.Planet.TimeScale.None;
import static worlds.planet.Planet.instance;
import static worlds.planet.enums.SilicateContent.Mix;
import static worlds.planet.enums.SilicateContent.Rich;
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
        thermalInc = 300;
        volcanicHeatLoss = 50;
        averageVolcanicMass = 250;
        rand = new Random();
        drawSediments = true;
    }

    public Geosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        ageStamp = 0;
//        produceTasks(new GeologicalUpdateFactory());
        produceTasks(new AeolianFactory());
        produceTasks(new SedimentationFactory());
//        produceTasks(new MetamorphicAndMeltingFactory());
//        produceTasks(new SedimentSpreadFactory());
//        produceTasks(new HeatMantelFactory());
    }

    /**
     * Add a uniformed layer on the whole surface. Adds a new layer even if the
     * layer type is the same.
     *
     * @param type The layer being added
     * @param amount The amount being added
     */
    public void addToSurface(Layer type, float amount) {
        int cellCount = getTotalNumberOfCells();
        for (int i = 0; i < cellCount; i++) {
            PlanetCell cell = waitForCellAt(i);
            cell.pushStratum(new Stratum(type, amount));
            release(cell);
        }
    }


    public void getLowestCells(PlanetCell from, List<Point> lowestList, int max) {

        int tx, ty, mx, my;
        int x = from.getX(), y = from.getY();
        float heightWithoutOceans = from.getHeightWithoutOceans();
        release(from);
        int xl = DIR_X_INDEX.length;
        PlanetCell selectedCell;

        for (int s = 0; s < xl; s++) {

            tx = x + DIR_X_INDEX[s];
            ty = y + DIR_Y_INDEX[s];

            // Check the boundaries
            mx = checkBounds(tx, getGridWidth());
            my = checkBounds(ty, getGridWidth());

            selectedCell = waitForCellAt(mx, my);
            
            if (selectedCell != null && 
                    (selectedCell.getHeightWithoutOceans() < heightWithoutOceans)) {
                Point p = selectedCell.getPosition();
                release(selectedCell);
                if (lowestList.size() < max) {
                    lowestList.add(p);
                } else {
                    break;
                }
            }
        }
    }

    public static int MELTING_PRESSURE;
    public static int SEDIMENTARY_TO_METAMORPHIC;
    public static int IGNEOUS_TO_METAMORPHIC;
    public static int SLATE_TO_PHYLITE;
    public static int PHYLITE_TO_SCHIST;
    public static int SCHIST_TO_GNEISS;
    public static float MASS_TO_MELT;

    static {
        MELTING_PRESSURE = 8500;
        SEDIMENTARY_TO_METAMORPHIC = 7500;
        IGNEOUS_TO_METAMORPHIC = 8200;
        SLATE_TO_PHYLITE = 7800;
        PHYLITE_TO_SCHIST = 8000;
        SCHIST_TO_GNEISS = 8200;
        MASS_TO_MELT = 2500;
    }

    private class MetamorphicAndMeltingFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new ChangeRockSubTasks();
        }

        private class ChangeRockSubTasks extends CompoundTask {

            public ChangeRockSubTasks() {
                addSubTask(new MeltSubTask());
                addSubTask(new MetaRockSubTask());
            }

            private class MeltSubTask extends Task {

                private Delay meltDelay;

                public MeltSubTask() {
                    meltDelay = new Delay(200);
                }

                @Override
                public boolean check() {
                    return meltDelay.check();
                }

                @Override
                public void before() {
                }

                @Override
                public void perform(int x, int y) {
                    PlanetCell cell = waitForCellAt(x, y);
                    melt(cell, calcDepth(cell.getDensity(), 9.8f, MELTING_PRESSURE));
                    release(cell);
                }

                @Override
                public void after() {
                }

                private void melt(GeoCell cell, float maxHeight) {

                    float height;
                    if (cell.peekBottomStratum() == null) {
                        return;
                    }
                    height = cell.getHeight();

                    if (height > maxHeight) {
                        float diff = height - maxHeight;
                        cell.remove(getMassToMelt(diff), false, false);
                    }
                }

                private float getMassToMelt(float heightDiff) {
                    return (20329 + (15000 * heightDiff * heightDiff)) / 10f;
                }
            }

            private class MetaRockSubTask extends Task {

                private Delay metaDelay;
                private Stratum metaStratum;

                public MetaRockSubTask() {
                    metaDelay = new Delay(10);
                }

                @Override
                public boolean check() {
                    return metaDelay.check();
                }

                @Override
                public void before() {
                }

                @Override
                public void perform(int x, int y) {
                    PlanetCell cell = waitForCellAt(x, y);
                    List<Stratum> metamorphicStrata = new LinkedList<>();
                    boolean metamorphicRockCanForm = true;
                    Layer metaType = null, prevType;

                    while (metamorphicRockCanForm) {
                        float density = cell.getDensity();
                        float cellDepth = cell.getHeight();
                        float pressure = Tools.calcPressure(density, 9.8f, cellDepth);

                        Stratum bottom = cell.peekBottomStratum();
                        if (bottom == null) {
                            return;
                        }

                        Layer bottomType = cell.peekBottomStratum().getLayer();
                        prevType = metaType;
                        metaType = getMetaLayer(bottomType, pressure);

                        if (metaType != null) {
                            metamorphisize(cell, metaType);
                        } else {
                            metamorphicRockCanForm = false;
                        }

                        if (prevType != null && metaType != prevType) {
                            metamorphicStrata.add(metaStratum.copy());
                            metaStratum = null;
                        }

                    }

                    for (int i = metamorphicStrata.size() - 1; i >= 0; i--) {
                        Stratum toAdd = metamorphicStrata.get(i);
                        Layer type = toAdd.getLayer();
                        float mass = toAdd.getMass();
                        cell.add(type, mass, false);
                    }
                    release(cell);
                }

                @Override
                public void after() {
                }

                private void metamorphisize(GeoCell cell, Layer metaType) {
                    float massToChange;

                    if (cell.peekBottomStratum() == null) {
                        return;
                    }

                    Stratum bottom = cell.peekBottomStratum();
                    Layer bottomType = bottom.getLayer();

                    massToChange = calcMass(MASS_TO_MELT, instance().getCellArea(), bottomType);
                    massToChange = removeAndChangeMass(cell, massToChange, bottomType, metaType);
                    if (metaStratum == null) {
                        metaStratum = new Stratum(metaType, massToChange);
                    } else {
                        metaStratum.addToMass(massToChange);
                    }
                }

                private Layer getMetaLayer(Layer bottomType, float pressure) {
                    Layer metaType = null;
                    if (bottomType.getRockType() == RockType.SEDIMENTARY
                            && pressure >= SEDIMENTARY_TO_METAMORPHIC) {
                        if (bottomType == Layer.FELSIC_SANDSTONE) {
                            metaType = Layer.QUARTZITE;
                        } else if (bottomType == Layer.LIMESTONE) {
                            metaType = Layer.MARBLE;
                        } else {
                            metaType = Layer.SLATE;
                        }
                    } else if (bottomType.getRockType() == RockType.IGNEOUS
                            && pressure >= IGNEOUS_TO_METAMORPHIC) {
                        metaType = Layer.GNEISS;
                    } else {
                        if ((bottomType == Layer.SLATE || bottomType == Layer.MARBLE
                                || bottomType == Layer.QUARTZITE) && pressure >= SLATE_TO_PHYLITE) {
                            metaType = Layer.PHYLITE;
                        } else if (bottomType == Layer.PHYLITE && pressure >= PHYLITE_TO_SCHIST) {
                            metaType = Layer.SCHIST;
                        } else if (bottomType == Layer.SCHIST && pressure >= SCHIST_TO_GNEISS) {
                            metaType = Layer.GNEISS;
                        }
                    }
                    return metaType;
                }

                private float removeAndChangeMass(GeoCell cell, float mass, Layer bottomType, Layer toType) {
                    float massToChange = cell.remove(mass, false, false);
                    massToChange = Tools.changeMass(massToChange, bottomType, toType);
                    return massToChange;
                }
            }
        }
    }

    public static float windErosionConstant;

    static {
        windErosionConstant = 10;
    }

    private class AeolianFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new AeolianTask();
        }

        private class AeolianTask extends Task {

            private Delay delay;

            public AeolianTask() {
                delay = new Delay(150);
            }

            @Override
            public void before() {
            }

            @Override
            public void perform(int x, int y) {
                PlanetCell cell = waitForCellAt(x, y);
                aeolianErosion(cell);
                release(cell);
            }

            public void aeolianErosion(GeoCell spreadFrom) {
                convertTopLayer(spreadFrom);
            }

            public void convertTopLayer(GeoCell spreadFrom) {

                float rockMass, sandMass;

                if (spreadFrom.peekTopStratum() == null) {
                    return;
                }

                Layer rockType = spreadFrom.peekTopStratum().getLayer();
                
                SedimentBuffer eb = spreadFrom.getSedimentBuffer();
                // Wind erosion
                if (eb.getSediments() == 0 && !spreadFrom.hasOcean()
                        && spreadFrom.getMoltenRockLayer().getMoltenRockFromSurface() < 1000) {

                    Layer sedimentType;
                    if (rockType.getSilicates() == Rich) {
                        sedimentType = Layer.FELSIC;
                    } else if (rockType.getSilicates() == Mix) {
                        sedimentType = Layer.MIX;
                    } else {
                        sedimentType = Layer.MAFIC;
                    }

                    rockMass = spreadFrom.erode(windErosionConstant);
                    sandMass = changeMass(rockMass, rockType, sedimentType);

                    eb.transferSediment(sedimentType, sandMass);
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

    private class SedimentSpreadFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new SpreadSedimentTask();
        }

        private class SpreadSedimentTask extends Task {

            private Delay delay;

            public SpreadSedimentTask() {
                delay = new Delay(5);
            }

            @Override
            public void before() {
            }

            @Override
            public void perform(int x, int y) {
                PlanetCell cell = waitForCellAt(x, y);
                spreadToLowest(cell);
            }

            public void spreadToLowest(PlanetCell spreadFrom) {
                int maxCellCount;
                if (!spreadFrom.hasOcean()) {
                    maxCellCount = 8;
                } else {
                    maxCellCount = 3;
                }
                ArrayList<Point> lowestList = new ArrayList<>(maxCellCount);
                boolean hasOceans = spreadFrom.hasOcean();
                getLowestCells(spreadFrom, lowestList, maxCellCount);
                spread(lowestList, spreadFrom);
            }

            /**
             * Selects a random cell from the given list and spreads the
             * sediments to that cell.
             *
             * @param lowestList The list of lowest cells from the central cell
             * @param spreadFrom The central cell
             */
            public void spread(ArrayList<Point> lowestList, PlanetCell spreadFrom) {

                PlanetCell lowestGeoCell;
                SedimentBuffer spreadFromEB = spreadFrom.getSedimentBuffer();
                Layer spreadFromSedType = spreadFromEB.getSedimentType();

                if (spreadFromSedType == null) {
                    return;
                }

                SedimentBuffer lowestBuffer;
                float spreadFromHeight, lowestHeight, diff, mass;

                if (lowestList.size() > 0) {

                    Point p = lowestList.get(random.nextInt(lowestList.size()));
                    lowestGeoCell = waitForCellAt(p.getX(), p.getY());
                    spreadFromHeight = spreadFrom.getHeightWithoutOceans() / 2f;
                    lowestHeight = lowestGeoCell.getHeightWithoutOceans() / 2f;
                    diff = (spreadFromHeight - lowestHeight) / 2f;

                    diff = clamp(diff, -lowestHeight, spreadFromHeight);

                    if (spreadFromEB.getSediments() > 0) {
                        if (spreadFrom.hasOcean()) {
                            diff *= 0.05f;
                        }
                        mass = calcMass(diff, instance().getCellArea(), spreadFromSedType);
                        mass = calcFlow(mass);

                        spreadFromEB.transferSediment(spreadFromSedType, -mass);
                        
                        lowestBuffer = lowestGeoCell.getSedimentBuffer();
                        lowestBuffer.transferSediment(spreadFromSedType, mass);
                    }
                    release(lowestGeoCell);
                }
            }

            /**
             * As the mass increases the flow decreases.
             */
            private float calcFlow(float mass) {
                return (float) Math.pow(90f * mass, 0.5f);
            }

            @Override
            public void after() {
            }

            @Override
            public boolean check() {
                return delay.check();
            }
        }

    }

    private class GeologicalUpdateFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new GeologicalUpdate();
        }

        private class GeologicalUpdate extends Task {

            private Delay geologicDelay;

            public GeologicalUpdate() {
                geologicDelay = new Delay(250);
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

                PlanetCell cell = waitForCellAt(x, y);

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
                release(cell);
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

    private class HeatMantelFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new HeatMantel();
        }

        private class HeatMantel extends BasicTask {

            private Delay mantelHeatingDelay;

            public HeatMantel() {
                mantelHeatingDelay = new Delay(5);
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
                Boundaries bounds = taskThread.getManager().getBounds();

                int min = bounds.getLowerXBound();
                int max = bounds.getUpperXBound();
                int x = ThreadLocalRandom.current().nextInt(min, max);

                min = bounds.getLowerXBound();
                max = bounds.getUpperXBound();
                int y = ThreadLocalRandom.current().nextInt(min, max);

                PlanetCell cell = waitForCellAt(x, y);
                cell.addHeat(thermalInc);
                release(cell);
            }

            @Override
            public void before() {
            }

            @Override
            public void after() {
            }
        }
    }

    private class SedimentationFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new RockFormation();
        }

        private class RockFormation extends Task {

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

//                if (!Planet.instance().isTimeScale(Geological)) {
//                    updateBasaltFlows(x, y);
//                }
            }

            public void depositSediment(int x, int y) {
                PlanetCell cell = waitForCellAt(x, y);
                formSedimentaryRock(cell);
                release(cell);
            }

            public void formSedimentaryRock(PlanetCell cell) {

                float height, diff, massBeingDeposited;
                SedimentBuffer eb = cell.getSedimentBuffer();

                Layer sedimentType = eb.getSedimentType();
                Layer depositType;

                if (sedimentType == null) {
                    return;
                }
                eb.applyBuffer();
                height = calcHeight(eb.getSediments(), instance().getCellArea(), sedimentType);
                float maxHeight = calcDepth(sedimentType, 9.8f, 200);

                if (height > maxHeight) {

                    diff = (height - maxHeight);

                    massBeingDeposited = calcMass(diff, instance().getCellArea(), sedimentType);

                    if (sedimentType.getSilicates() == SilicateContent.Rich) {
                        depositType = Layer.FELSIC_SANDSTONE;
                    } else if (sedimentType.getSilicates() == SilicateContent.Mix) {
                        if (cell.getOceanMass() >= 4000) {
                            depositType = Layer.SHALE;
                        } else {
                            depositType = Layer.MIX_SANDSTONE;
                        }
                    } else {
                        depositType = Layer.MAFIC_SANDSTONE;
                    }

                    eb.updateSurfaceSedimentMass(-massBeingDeposited);

                    massBeingDeposited = changeMass(massBeingDeposited, sedimentType, depositType);
                    cell.add(depositType, massBeingDeposited, true);

                }
            }

//            /**
//             * Updates surface lava.
//             *
//             * @see planet.surface.Geosphere#updateGeology(int, int)
//             * @param x Cell's x
//             * @param y Cell's y
//             */
//            public void updateBasaltFlows(int x, int y) {
//                PlanetCell toUpdate = waitForCellAt(x, y);
//                MoltenRockLayer moltenLayer = toUpdate.getMoltenRockLayer();
//                Layer moltenType = moltenLayer.getMoltenRockType(), layerType;
//
//                if (moltenType != null) {
//                    if (moltenType.getSilicates() == SilicateContent.Rich) {
//                        layerType = Layer.RHYOLITE;
//                    } else if (moltenType.getSilicates() == SilicateContent.Poor) {
//                        layerType = Layer.BASALT;
//                    } else {
//                        layerType = Layer.ANDESITE;
//                    }
//
//                    if (moltenLayer.getMoltenRockFromSurface() > 8000) {
//                        int maxCellCount = 8;
//                        ArrayList<Point> lowestList = new ArrayList<>(maxCellCount);
//                        getLowestCells(toUpdate, lowestList, maxCellCount);
//
//                        if (!lowestList.isEmpty()) {
//                            int rIndex = rand.nextInt(lowestList.size());
//                            Point p = lowestList.get(rIndex);
//                            PlanetCell lowest = waitForCellAt(p.getX(), p.getY());
//
//                            if (lowest != null && lowest != toUpdate) {
//                                float currentCellHeight = toUpdate.getHeightWithoutOceans() / 2f;
//                                float lowestHeight = lowest.getHeightWithoutOceans() / 2f;
//                                float diff = (currentCellHeight - lowestHeight) / 2f;
//
//                                double theta = Math.atan((currentCellHeight - lowestHeight) / instance().getCellLength());
//                                float angle = (float) Math.sin(theta);
//
//                                diff = clamp(diff, -lowestHeight, currentCellHeight);
//
//                                float mass = calcMass(diff, instance().getCellArea(), moltenType);
//                                mass = moltenLayer.putMoltenRockToSurface(-mass, moltenType) / 2f;
//                                if (angle >= 0.71f) {
//                                    mass = changeMass(mass * angle * 200f, moltenType, layerType);
//                                } else {
//                                    float rate = toUpdate.hasOcean() ? 0.15f : 0.05f;
//                                    float massToSolidify = moltenLayer.getMoltenRockFromSurface() * rate;
//                                    moltenLayer.putMoltenRockToSurface(-massToSolidify, moltenType);
//                                    massToSolidify = changeMass(massToSolidify, moltenType, layerType);
//                                    toUpdate.add(layerType, massToSolidify, true);
//                                    toUpdate.recalculateHeight();
//                                }
//                                float carvedOutMass = toUpdate.remove(mass, true, true);
//                                float sediments = lowest.getSedimentBuffer().removeAllSediments();
//                                float totalMoved = carvedOutMass + sediments + mass;
//
//                                lowest.getMoltenRockLayer().putMoltenRockToSurface(totalMoved, moltenType);
//                            }
//                            release(lowest);
//                        }
//                    } else {
//                        float massToSolidify = moltenLayer.removeAllMoltenRock();
//                        massToSolidify = changeMass(massToSolidify, moltenType, layerType);
//                        toUpdate.add(layerType, massToSolidify, true);
//                        toUpdate.recalculateHeight();
//                    }
//                }
//                release(toUpdate);
//            }

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
