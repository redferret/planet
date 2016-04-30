
package planet.surface;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import planet.Planet;

import planet.surface.GeoCell.SedimentBuffer;
import planet.surface.generics.SurfaceThread;
import planet.surface.generics.SurfaceMap;
import planet.surface.HydroCell.SuspendedSediments;
import planet.surface.HydroCell.WaterBuffer;

import planet.util.Boundaries;

import static planet.surface.generics.SurfaceMap.DIR_X_INDEX;
import static planet.surface.generics.SurfaceMap.DIR_Y_INDEX;
import static planet.surface.Layer.BASALT;
import static planet.surface.Layer.LAVA;
import static planet.surface.Layer.OCEAN;
import static planet.surface.Layer.SANDSTONE;
import static planet.surface.Layer.SEDIMENT;
import static planet.surface.Layer.SHALE;

import static planet.surface.Surface.GEOUPDATE;
import static planet.surface.Surface.planetAge;

import static planet.surface.HydroCell.MIN_ANGLE;
import static planet.surface.HydroCell.evapScale;
import static planet.surface.HydroCell.oceanSedimentCapacity;
import static planet.surface.HydroCell.rainProb;
import static planet.surface.HydroCell.rainScale;

import static planet.util.Tools.calcDepth;
import static planet.util.Tools.calcHeight;
import static planet.util.Tools.calcMass;
import static planet.util.Tools.changeMass;
import static planet.util.Tools.checkXBounds;
import static planet.util.Tools.checkYBounds;
import static planet.util.Tools.clamp;
import static planet.util.Tools.getLowestCellFrom;

/**
 * The Surface is the geology for the planet. It provides a foundation
 * for life to grow on and to influence climate in many ways. Through
 * the placement of continents, the type of surface which provides albedo
 * rating for solar radiation absorbtions, and volcanism which pours out
 * CO2 and water vapor.
 *
 * @author Richard DeSilvey
 */
public final class Surface extends SurfaceMap<AtmoCell> {

    /**
     * The average density of the mantel. The units are in kilograms per cubic
     * meter.
     */
    public static float mantel_density = 3700f;
    
    public static int waterDepthShale;
    
    /**
     * A mutable erosion quantity (works best around 32 - 128) during
     * geological time scales
     */
    public static float erosionAmount;
    
    /**
     * A variable that controls how thick sediment layers need to be
     * before converting them to sedimentary rock.
     */
    public static float ssMul = 1.0f;
    
    /**
     * The number of years that pass for each step of erosion
     */
    public static long GEOUPDATE;
    
    private long geologicalTimeStamp;
    
    /**
     * The age of the planet in years
     */
    public static AtomicLong planetAge;
    
    /**
     * The number of years that pass for each update to the geosphere
     */
    public static long ageStep;
    
    public static int planetTemp;
    
    public static AtomicInteger lowestHeight;
    private float averageHeight;
    private long strataBuoyancyStamp;
    
    /**
     * Used primarily for erosion algorithms.
     */
    private static final Random rand;
    
    
    static {
        rand = new Random();
        erosionAmount = 1;
        ageStep = 100000;
        GEOUPDATE = 100000;
        waterDepthShale = 10;
        lowestHeight = new AtomicInteger(Integer.MAX_VALUE);
    }
    
    /**
     * Constructs a new Surface.
     * @param width The width of the surface
     * @param height The height of the surface
     * @param delay The amount of time to delay each frame in milliseconds.
     */
    public Surface(int width, int height, int delay) {
        super(width, height, delay, "Geosphere");
        reset();
    }

    public void reset(){
        planetAge = new AtomicLong(0);
        geologicalTimeStamp = 0;
        averageHeight = 0;
        strataBuoyancyStamp = 0;
    }

    
    public long getPlanetAge(){
        return planetAge.get();
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
        SedimentBuffer eb = cell.getSedimentBuffer();
        
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

        SedimentBuffer eb = spreadFrom.getSedimentBuffer();
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
        SedimentBuffer eb = spreadFrom.getSedimentBuffer();
        SedimentBuffer lowestBuffer;
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

    public void updateOceans(int x, int y) {

        HydroCell cellToUpdate, lowestCell;
        
        float lowestHeight, curCellHeight, displacedMass,
                diffGeoHeight, differenceHeight, totalMass;

        cellToUpdate = (HydroCell)getCellAt(x, y);
        lowestCell = (HydroCell)getLowestCellFrom(cellToUpdate);

        if (lowestCell == null || cellToUpdate == null) {
            return;
        }

        WaterBuffer toUpdateWaterBuffer = cellToUpdate.getWaterBuffer();
        WaterBuffer lowestHydroBuffer = lowestCell.getWaterBuffer();
        
        toUpdateWaterBuffer.applyWaterBuffer();
        lowestHydroBuffer.applyWaterBuffer();
        
        SuspendedSediments lowestSSediments = cellToUpdate.getSedimentMap();
        SuspendedSediments toUpdateSSediments = lowestCell.getSedimentMap();
        
        lowestSSediments.applyBuffer();
        toUpdateSSediments.applyBuffer();
        
        if (rand.nextInt(rainProb) == 0) {
            toUpdateWaterBuffer.transferWater(rainScale);
        }

        if (lowestCell != cellToUpdate && cellToUpdate.hasOcean()) {

            lowestHeight = lowestCell.getHeight();
            curCellHeight = cellToUpdate.getHeight();

            // Move the water
            differenceHeight = (curCellHeight - lowestHeight) / 2.5f;
            curCellHeight = cellToUpdate.getHeight() / 2.5f;
            lowestHeight = lowestCell.getHeight() / 2.5f;

            differenceHeight = clamp(differenceHeight, -lowestHeight, curCellHeight);

            displacedMass = calcMass(differenceHeight, Planet.self().getBase(), OCEAN);

            toUpdateWaterBuffer.transferWater(-displacedMass);
            lowestHydroBuffer.transferWater(displacedMass);

            // Erosion/Deposition
            lowestHeight = lowestCell.getHeightWithoutOceans();
            curCellHeight = cellToUpdate.getHeightWithoutOceans();
            diffGeoHeight = curCellHeight - lowestHeight;

            if (cellToUpdate.getOceanMass() <= oceanSedimentCapacity) {

                float angle, velocity, slope;
                SedimentBuffer sedimentBuffer = cellToUpdate.getSedimentBuffer();
                // Erosion
                angle = (float) Math.atan(diffGeoHeight / Planet.self().getSqrtBase());
                slope = Math.max((float) Math.sin(angle), MIN_ANGLE);

                totalMass = cellToUpdate.getOceanMass() * slope;

                if (sedimentBuffer.getSediments() <= 10) {
                    
                    velocity = cellToUpdate.erode(totalMass);
                    
                    velocity += sedimentBuffer.getSediments();
                    sedimentBuffer.removeAllSediments();
                } else {
                    velocity = -sedimentBuffer.updateSurfaceSedimentMass(-totalMass);
                }
                lowestSSediments.transferSediment(velocity);
                toUpdateSSediments.transferSediment(-velocity);
            }

            // Only evaporate if in oceans. Will probably be removed later.
            else if (cellToUpdate.getOceanMass() > oceanSedimentCapacity) {
                // Evaporate Water
                toUpdateWaterBuffer.transferWater(-evapScale);
            }
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
        averageHeight += cell.getHeightWithoutOceans();

        // Update the geosphere
        if (geoScale) {

            spreadToLowest(cell, true);
        } else {
            if ((curPlanetAge - strataBuoyancyStamp) > GEOUPDATE) {

                spreadToLowest(cell, false);
                cell.updateHeight();
                strataBuoyancyStamp = curPlanetAge;
            }
        }
        depositSediment(x, y);
        updateLavaFlows(x, y);

        
    }

    public void dust(GeoCell cell) {
        if (cell.getMoltenRockFromSurface() < 1) {
            cell.getSedimentBuffer().updateSurfaceSedimentMass(10);
        }
    }

    public void checkForMinimumHeight(int x, int y){
        float cellHeight = getCellAt(x, y).getHeight();
        
        if (cellHeight < lowestHeight.get()){
            lowestHeight.set((int) cellHeight);
        }
    }
    
    public float getAverageHeight(){
        return averageHeight;
    }
    
    protected void postUpdate() {
        averageHeight /= Planet.self().getTotalNumberOfCells();
    }
    
    @Override
    public void update() {
        super.update();
        long curPlanetAge = planetAge.getAndAdd(ageStep);
        if (curPlanetAge - geologicalTimeStamp > GEOUPDATE){
            // < Update to major geological events go here >
            
            geologicalTimeStamp = curPlanetAge;
        }
        
        postUpdate();
    }
    
    
    @Override
    public SurfaceThread generateSurfaceThread(int delay, Boundaries bounds, String name) {
        return null;
    }

    @Override
    public AtmoCell generateCell(int x, int y) {
        AtmoCell gen = new AtmoCell(x, y);
        gen.add(BASALT, rand.nextInt(100000), true);
        gen.addOceanMass(1);
        return gen;
    }

}


