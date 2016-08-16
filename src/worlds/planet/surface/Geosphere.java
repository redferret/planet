package worlds.planet.surface;

import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedList;

import worlds.planet.cells.geology.GeoCell;
import worlds.planet.cells.PlanetCell;
import worlds.planet.enums.Layer;
import worlds.planet.cells.geology.GeoCell.SedimentBuffer;
import worlds.planet.cells.geology.Stratum;
import worlds.planet.enums.RockType;
import worlds.planet.enums.SilicateContent;

import engine.util.Delay;
import engine.util.Task;
import engine.util.TaskFactory;
import engine.util.Tools;
import engine.util.BasicTask;

import static engine.util.Tools.calcDepth;
import static engine.util.Tools.calcHeight;
import static engine.util.Tools.calcMass;
import static engine.util.Tools.changeMass;
import static engine.util.Tools.checkBounds;
import static engine.util.Tools.clamp;

import static worlds.planet.Planet.instance;
import static worlds.planet.Planet.TimeScale.Geological;
import static worlds.planet.Planet.TimeScale.None;
import static worlds.planet.enums.Layer.BASALT;
import static worlds.planet.enums.Layer.MAFICMOLTENROCK;
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
        thermalInc = 100;
        volcanicHeatLoss = 50;
        averageVolcanicMass = 500000;
        rand = new Random();
        drawSediments = true;
    }

    public Geosphere(int worldSize, int surfaceDelay, int threadsDelay, int threadCount) {
        super(worldSize, surfaceDelay, threadsDelay, threadCount);
        ageStamp = 0;
        produceTasks(new GeologicalUpdateFactory());
        produceTasks(new AeolianFactory());
        produceTasks(new SedimentationFactory());
        produceTasks(new MetamorphicFactory());
        produceTasks(new AeolianSpreadFactory());
        produceTasks(new WaveErosionFactory());
        addTask(new HeatMantel());
    }

    /**
     * Add a uniformed layer on the whole surface. Adds a new layer even if
     * the layer type is the same.
     *
     * @param type The layer being added
     * @param amount The amount being added
     */
    public void addToSurface(Layer type, float amount) {
        int cellCount = getTotalNumberOfCells();
        for (int i = 0; i < cellCount; i++) {
            getCellAt(i).pushStratum(new Stratum(type, amount));
        }
    }

    public void addLavaToSurface(float amount) {
        int cellCount = getTotalNumberOfCells();
        for (int i = 0; i < cellCount; i++) {
            getCellAt(i).putMoltenRockToSurface(amount);
        }
    }

    public void getLowestCells(GeoCell from, List<GeoCell> lowestList, int max) {

        int tx, ty, mx, my;
        int x = from.getX(), y = from.getY();
        int xl = DIR_X_INDEX.length;
        GeoCell selectedCell;

        for (int s = 0; s < xl; s++) {

            tx = x + DIR_X_INDEX[s];
            ty = y + DIR_Y_INDEX[s];

            // Check the boundaries
            mx = checkBounds(tx, getGridWidth());
            my = checkBounds(ty, getGridWidth());

            selectedCell = getCellAt(mx, my);

            if (selectedCell.getHeightWithoutOceans() < from.getHeightWithoutOceans()) {
                if (lowestList.size() < max) {
                    lowestList.add(selectedCell);
                } else {
                    break;
                }
            }
        }
    }

    private class MetamorphicFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new MeltRockTask();
        }

        private class MeltRockTask implements Task {

            private Delay meltDelay;
            
            public MeltRockTask() {
                meltDelay = new Delay(5);
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
                PlanetCell cell = getCellAt(x, y);
                List<Stratum> metamorphicStrata = new LinkedList<>();
                boolean metamorphicRockCanForm = true;
                boolean nextLayerUp = false;
                
                while (metamorphicRockCanForm){
                    float density = cell.getDensity();
                    float cellDepth = cell.getHeight();
                    float pressure = Tools.calcPressure(density, 9.8f, cellDepth);

                    Layer bottomType = cell.peekBottomStratum().getLayer();
                    Layer metaType = getMetaLayer(bottomType, pressure);

                    if (metaType != null){
                        metamorphisize(cell, metaType);
                    }else{
                        if (bottomType.getRockType() != RockType.METAMORPHIC){
                            metamorphicRockCanForm = false;
                        }else{
                            nextLayerUp = true;
                        }
                    }
                    bottomType = cell.peekBottomStratum().getLayer();
                    
                    if (nextLayerUp && bottomType.getRockType() == RockType.METAMORPHIC){
                        metamorphicStrata.add(cell.removeBottomStratum());
                        nextLayerUp = false;
                    }
                    
                }
                
                for (int i = metamorphicStrata.size() - 1; i >= 0; i--){
                    Stratum toAdd = metamorphicStrata.get(i);
                    Layer type = toAdd.getLayer();
                    float mass = toAdd.getMass();
                    cell.add(type, mass, false);
                }
                
                melt(cell, calcDepth(cell.getDensity(), 9.8f, 85000));
            }

            @Override
            public void after() {
            }

            private void metamorphisize(GeoCell cell, Layer metaType){
                float massToChange;
                
                if (cell.peekBottomStratum() == null) {
                    return;
                }
                
                Stratum bottom = cell.peekBottomStratum();
                Layer bottomType = bottom.getLayer();

                massToChange = calcMass(0.25f, instance().getCellArea(), bottomType);
                massToChange = removeAndChangeMass(cell, massToChange, bottomType, metaType);
                cell.add(metaType, massToChange, false);
            }
            
            private Layer getMetaLayer(Layer bottomType, float pressure){
                Layer metaType = null;
                if (bottomType.getRockType() == RockType.SEDIMENTARY &&
                        pressure >= 75000) {
                    if (bottomType == Layer.FELSIC_SANDSTONE) {
                        metaType = Layer.QUARTZITE;
                    } else if (bottomType == Layer.LIMESTONE) {
                        metaType = Layer.MARBLE;
                    } else {
                        metaType = Layer.SLATE;
                    }
                } else if (bottomType.getRockType() == RockType.IGNEOUS &&
                        pressure >= 82000){
                    metaType = Layer.GNEISS;
                } else {
                    if (bottomType == Layer.SLATE && pressure >= 78000){
                        metaType = Layer.PHYLITE;
                    }else if (bottomType == Layer.PHYLITE && pressure >= 80000){
                        metaType = Layer.SCHIST;
                    }else if (bottomType == Layer.SCHIST && pressure >= 82000){
                        metaType = Layer.GNEISS;
                    }
                }
                return metaType;
            }
            
            private float removeAndChangeMass(GeoCell cell, float mass, Layer bottomType, Layer toType){
                float massToChange = cell.remove(mass, false, false);
                massToChange = Tools.changeMass(massToChange, bottomType, toType);
                return massToChange;
            }
            
            private void melt(GeoCell cell, float maxHeight) {

                float height, massToChange;
                if (cell.peekBottomStratum() == null) {
                    return;
                }
                Layer bottomType = cell.peekBottomStratum().getLayer();

                height = cell.getHeight();

                if (height > maxHeight) {
                    massToChange = calcMass(0.25f, instance().getCellArea(), bottomType);
                    cell.remove(massToChange, false, false);
                }
            }
        }
    }

    public static float windErosionConstant;
    static {
        windErosionConstant = 100;
    }
    
    private class AeolianFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new AeolianTask();
        }

        private class AeolianTask implements Task {

            private Delay delay;
            
            public AeolianTask() {
                delay = new Delay(1);
            }

            @Override
            public void before() {
            }
            
            @Override
            public void perform(int x, int y) {
                aeolianErosion(getCellAt(x, y));
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
                
                float height = calcHeight(windErosionConstant, instance().getCellArea(), rockType);
                
                SedimentBuffer eb = spreadFrom.getSedimentBuffer();
                // Wind erosion
                if (eb.getSediments() == 0 && !spreadFrom.hasOcean()
                        && spreadFrom.getMoltenRockFromSurface() < 1000) {

                    rockMass = calcMass(height, instance().getCellArea(), rockType);
                    Layer sedimentType;
                    if (rockType.getSilicates() == Rich) {
                        sedimentType = Layer.FELSIC;
                    } else if (rockType.getSilicates() == Mix) {
                        sedimentType = Layer.MFMIX;
                    } else {
                        sedimentType = Layer.MAFIC;
                    }

                    rockMass = spreadFrom.erode(rockMass);
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

    private class WaveErosionFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new WaveErosionTask();
        }
        
        private class WaveErosionTask implements Task {

            private Delay delay;
            
            public WaveErosionTask() {
                delay = new Delay(20);
            }
            
            @Override
            public boolean check() {
                return false;
            }
            
            @Override
            public void before() {
            }
            
            @Override
            public void perform(int x, int y) {
                
            }

            @Override
            public void after() {
            }

            
            
        }
        
    }
    
    private class AeolianSpreadFactory implements TaskFactory {

        @Override
        public Task buildTask() {
            return new SpreadSedimentTask();
        }

        private class SpreadSedimentTask implements Task {

            private Delay delay;

            public SpreadSedimentTask() {
                delay = new Delay(25);
            }

            @Override
            public void before() {
            }

            @Override
            public void perform(int x, int y) {
                spreadToLowest(getCellAt(x, y));
            }

            public void spreadToLowest(GeoCell spreadFrom) {
                if (!spreadFrom.hasOcean()) {
                    int maxCellCount = 8;
                    ArrayList<GeoCell> lowestList = new ArrayList<>(maxCellCount);
                    getLowestCells(spreadFrom, lowestList, maxCellCount);
                    spread(lowestList, spreadFrom);
                }
            }

            /**
             * Selects a random cell from the given list and spreads the
             * sediments to that cell.
             *
             * @param lowestList The list of lowest cells from the central cell
             * @param spreadFrom The central cell
             */
            public void spread(ArrayList<GeoCell> lowestList, GeoCell spreadFrom) {

                GeoCell lowestGeoCell;
                SedimentBuffer spreadFromEB = spreadFrom.getSedimentBuffer();
                Layer spreadFromSedType = spreadFromEB.getSedimentType();

                if (spreadFromSedType == null) {
                    return;
                }

                SedimentBuffer lowestBuffer;
                float spreadFromHeight, lowestHeight, diff, mass;

                if (lowestList.size() > 0) {

                    lowestGeoCell = lowestList.get(random.nextInt(lowestList.size()));
                    spreadFromHeight = spreadFrom.getHeightWithoutOceans() / 2.5f;
                    lowestHeight = lowestGeoCell.getHeightWithoutOceans() / 2.5f;

                    diff = (spreadFromHeight - lowestHeight) / 2.5f;

                    diff = clamp(diff, -lowestHeight, spreadFromHeight);

                    if (spreadFromEB.getSediments() > 0) {
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
            mantelHeatingDelay = new Delay(50);
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
                cell.addOceanMass(65000f);
            }
        }

        @Override
        public void before() {
        }

        @Override
        public void after() {
        }
    }

    private class SedimentationFactory implements TaskFactory {

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
                updateBasaltFlows(x, y);
            }

            public void depositSediment(int x, int y) {
                GeoCell cell = getCellAt(x, y);
                formSedimentaryRock(cell);
            }

            public void formSedimentaryRock(GeoCell cell) {

                float height, diff, massBeingDeposited;
                SedimentBuffer eb = cell.getSedimentBuffer();
                
                Layer sedimentType = eb.getSedimentType();
                Layer depositType;

                if (sedimentType == null){
                    return;
                }
                eb.applyBuffer();
                height = calcHeight(eb.getSediments(), instance().getCellArea(), sedimentType);
                float maxHeight = calcDepth(sedimentType, 9.8f, 1500);
                
                if (height > maxHeight) {

                    diff = (height - maxHeight);

                    massBeingDeposited = calcMass(diff, instance().getCellArea(), sedimentType);

                    if (sedimentType.getSilicates() == SilicateContent.Rich){
                        depositType = Layer.FELSIC_SANDSTONE;
                    }else if (sedimentType.getSilicates() == SilicateContent.Mix){
                        if (cell.hasOcean()){
                            depositType = Layer.SHALE;
                        }else{
                            depositType = Layer.MIX_SANDSTONE;
                        }
                    }else {
                        depositType = Layer.MAFIC_SANDSTONE;
                    }
                    
                    eb.updateSurfaceSedimentMass(-massBeingDeposited);

                    massBeingDeposited = changeMass(massBeingDeposited, sedimentType, depositType);
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
            public void updateBasaltFlows(int x, int y) {

                GeoCell toUpdate = getCellAt(x, y);

                if (toUpdate.getMoltenRockFromSurface() > 1000) {
                    int maxCellCount = 7;
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

                            float mass = calcMass(diff, instance().getCellArea(), MAFICMOLTENROCK);

                            toUpdate.putMoltenRockToSurface(-mass);
                            float sediments = lowest.getSedimentBuffer().removeAllSediments();
                            lowest.putMoltenRockToSurface(mass + sediments);
                        }
                    }

                    float rate = toUpdate.hasOcean() ? 0.50f : 0.35f;

                    //solidify the rock
                    float massToSolidify = toUpdate.getMoltenRockFromSurface() * rate;
                    toUpdate.putMoltenRockToSurface(-massToSolidify);
                    massToSolidify = changeMass(massToSolidify, MAFICMOLTENROCK, BASALT);
                    toUpdate.add(BASALT, massToSolidify, true);
                    toUpdate.recalculateHeight();
                } else {
                    float massToSolidify = toUpdate.removeAllMoltenRock();
                    massToSolidify = changeMass(massToSolidify, MAFICMOLTENROCK, BASALT);
                    toUpdate.add(BASALT, massToSolidify, true);
                    toUpdate.recalculateHeight();
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
