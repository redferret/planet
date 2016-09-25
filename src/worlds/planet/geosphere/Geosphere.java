package worlds.planet.geosphere;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

import worlds.planet.PlanetCell;
import worlds.planet.enums.Layer;
import worlds.planet.geosphere.GeoCell.SedimentBuffer;
import worlds.planet.enums.RockType;

import engine.util.Delay;
import engine.util.Point;
import engine.util.task.Task;
import engine.util.task.TaskFactory;
import engine.util.Tools;
import engine.util.task.BasicTask;
import engine.util.task.Boundaries;
import engine.util.task.CompoundTask;
import worlds.planet.PlanetSurface;
import worlds.planet.Surface;

import static engine.util.Tools.calcDepth;
import static engine.util.Tools.calcMass;
import static engine.util.Tools.checkBounds;
import static engine.util.Tools.clamp;
import static worlds.planet.Planet.instance;

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
    private static int worldSize;
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
        Geosphere.worldSize = worldSize;
        ageStamp = 0;
        produceTasks(new GeologicalUpdateFactory());
        produceTasks(new SedimentationFactory());
        produceTasks(new PlateTectonicsFactory());
//        produceTasks(new MetamorphicAndMeltingFactory());
//        produceTasks(new HeatMantelFactory());
    }

    public long getAgeStamp() {
        return ageStamp;
    }

    public void setAgeStamp(long ageStamp) {
        this.ageStamp = ageStamp;
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


    public static void getLowestCells(List<PlanetCell> lowest, List<PlanetCell> cells){
        PlanetCell center = cells.get(cells.size() - 1);
        float geoHeight = center.getHeightWithoutOceans();
        for (int i = 0; i < cells.size() - 1; i++){
            PlanetCell cell = cells.get(i);
            if (cell.getHeightWithoutOceans() < geoHeight){
                lowest.add(cell);
            }
        }
    }

    public static Point[] getCellIndexesFrom(Point from){
        int tx, ty, mx, my;
        int x = from.getX(), y = from.getY();
        int xl = DIR_X_INDEX.length;
        Point[] points = new Point[xl+1];
        for (int s = 0; s < xl; s++) {

            tx = x + DIR_X_INDEX[s];
            ty = y + DIR_Y_INDEX[s];

            // Check the boundaries
            mx = checkBounds(tx, worldSize);
            my = checkBounds(ty, worldSize);

            Point p = new Point(mx, my);
            points[s] = p;
        }
        points[xl] = from;
        return points;
    }
    
    public static int MELTING_PRESSURE;
    public static int SEDIMENTARY_TO_METAMORPHIC;
    public static int IGNEOUS_TO_METAMORPHIC;
    public static int SLATE_TO_PHYLITE;
    public static int PHYLITE_TO_SCHIST;
    public static int SCHIST_TO_GNEISS;
    public static float MASS_TO_MELT;

    static {
        MELTING_PRESSURE = 850;
        SEDIMENTARY_TO_METAMORPHIC = 750;
        IGNEOUS_TO_METAMORPHIC = 820;
        SLATE_TO_PHYLITE = 780;
        PHYLITE_TO_SCHIST = 800;
        SCHIST_TO_GNEISS = 820;
        MASS_TO_MELT = 2500;
    }

    private class ChangeRockFactory implements TaskFactory {

        @Override
        public Task buildResource() {
            return new ChangeRock();
        }

        private class ChangeRock extends CompoundTask {

            public void setup() {
                addSubTask(new MeltSubTask());
                addSubTask(new MetaRockSubTask());
            }

            private class MeltSubTask extends Task {

                private Delay meltDelay;

                public void construct() {
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
                    float maxDepth = calcDepth(cell.getDensity(), 9.8f, MELTING_PRESSURE);
                    melt(cell, maxDepth);
                    release(cell);
                }

                @Override
                public void after() {
                }

                private void melt(GeoCell cell, float maxDepth) {

                    float height;
                    if (cell.peekBottomStratum() == null) {
                        return;
                    }
                    height = cell.getHeight();

                    if (height > maxDepth) {
                        float diff = Tools.calcMass(height - maxDepth, 
                                instance().getCellArea(), cell.getDensity());
                        cell.remove(diff, false, false);
                    }
                }
            }

            private class MetaRockSubTask extends Task {

                private Delay metaDelay;
                private Stratum metaStratum;

                public void construct() {
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

    @Deprecated
    private class SedimentSpreadFactory implements TaskFactory {

        @Override
        public Task buildResource() {
            return new SpreadSedimentTask();
        }

        private class SpreadSedimentTask extends Task {

            private Delay delay;

            public void construct() {
                delay = new Delay(5);
            }

            @Override
            public void before() {
            }

            @Override
            public void perform(int x, int y) {
                Point[] cellPos = getCellIndexesFrom(new Point(x, y));
                List<PlanetCell> workingCells = waitForCells(cellPos);
                int numCells = workingCells.size();
                PlanetCell spreadFrom = workingCells.get(numCells - 1);

                List<PlanetCell> lowestList = new ArrayList<>();
                getLowestCells(lowestList, workingCells);
                spread(lowestList, spreadFrom);

                release(workingCells);
            }

            /**
             * Selects a random cell from the given list and spreads the
             * sediments to that cell.
             *
             * @param lowestList The list of lowest cells from the central cell
             * @param spreadFrom The central cell
             */
            public void spread(List<PlanetCell> lowestList, PlanetCell spreadFrom) {

                PlanetCell lowestGeoCell;

                SedimentBuffer lowestBuffer;
                float spreadFromHeight = spreadFrom.getHeightWithoutOceans() / 2f,
                        lowestHeight, diff, mass;

                if (lowestList.size() > 0) {

                    lowestGeoCell = lowestList.get(random.nextInt(lowestList.size()));

                    lowestHeight = lowestGeoCell.getHeightWithoutOceans() / 2f;

                    diff = (spreadFromHeight - lowestHeight) / 2f;
                    diff = clamp(diff, -lowestHeight, spreadFromHeight);

                    SedimentBuffer spreadFromEB = spreadFrom.getSedimentBuffer();
                    Layer spreadFromSedType = spreadFromEB.getSedimentType();

                    if (spreadFromSedType == null) {
                        return;
                    }

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

    /**
     * Performs plate tectonic updates.
     */
    private class PlateTectonicsFactory implements TaskFactory {

        @Override
        public Task buildResource() {
            return new PlateTectonicsResource();
        }
        
        private class PlateTectonicsResource extends PlateTectonicsTask {

            @Override
            public void construct() {
            }

            @Override
            public void before() {
            }

            @Override
            public void perform() {
                updatePlates();
            }

            @Override
            public void after() {
            }
        }
    }
    
    /**
     * Connects the GeosphereUpdateTask to this surface extension.
     */
    private class GeologicalUpdateFactory implements TaskFactory {

        @Override
        public Task buildResource() {
            return new GeologicalUpdateResource(Geosphere.this);
        }

        private class GeologicalUpdateResource extends GeologicalUpdateTask {

            private Delay geologicDelay;

            public GeologicalUpdateResource(Geosphere geosphere) {
                super(geosphere);
            }

            public void construct() {
                geologicDelay = new Delay(250);
            }

            @Override
            public void perform(int x, int y) {
                PlanetCell cell = waitForCellAt(x, y);
                updateGeology(cell);
                release(cell);
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

    @Deprecated
    private class HeatMantelFactory implements TaskFactory {

        @Override
        public Task buildResource() {
            return new HeatMantel();
        }

        private class HeatMantel extends BasicTask {

            private Delay mantelHeatingDelay;

            public void construct() {
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

    /**
     * Forms sedimentary rock due to compression of sediments.
     */
    private class SedimentationFactory implements TaskFactory {

        @Override
        public Task buildResource() {
            return new RockFormationResource();
        }

        private class RockFormationResource extends RockFormationTask {

            private Delay updateDelay;

            public void construct() {
                updateDelay = new Delay(1);
            }

            @Override
            public void before() {
            }

            @Override
            public void perform(int x, int y) {
                PlanetCell cell = waitForCellAt(x, y);
                formSedimentaryRock(cell);
                release(cell);
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
