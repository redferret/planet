package worlds.planet.geosphere.tasks;

import static engine.util.Tools.calcMass;
import static worlds.planet.Planet.instance;

import java.util.LinkedList;
import java.util.List;

import worlds.planet.PlanetCell;
import worlds.planet.enums.Layer;
import worlds.planet.enums.RockType;
import worlds.planet.geosphere.GeoCell;
import worlds.planet.geosphere.Stratum;
import engine.util.Tools;
import engine.util.task.CompoundTask;

/**
 * The transformation task performs both metamorphic and melts rock. Rock
 * will start to transform into different types of metamorphic rock then
 * eventually will melt.
 * 
 * @author Richard DeSilvey
 *
 */
public abstract class TransformationTask extends CompoundTask {

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
	
    private Stratum metaStratum;
    
	public void melt(GeoCell cell, float maxDepth) {

        float height;
        if (cell.peekBottomStratum() == null) {
            return;
        }
        height = cell.getHeight();

        if (height > maxDepth) {
            float diff = Tools.calcMass(height - maxDepth, 
                    PlanetCell.cellArea, cell.getDensity());
            cell.remove(diff, false, false);
        }
    }
	
	public void metamorphisize(PlanetCell cell){
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
    }
    
    public void metamorphisize(GeoCell cell, Layer metaType) {
        float massToChange;

        if (cell.peekBottomStratum() == null) {
            return;
        }

        Stratum bottom = cell.peekBottomStratum();
        Layer bottomType = bottom.getLayer();

        massToChange = calcMass(MASS_TO_MELT, PlanetCell.cellArea, bottomType);
        massToChange = removeAndChangeMass(cell, massToChange, bottomType, metaType);
        if (metaStratum == null) {
            metaStratum = new Stratum(metaType, massToChange);
        } else {
            metaStratum.addToMass(massToChange);
        }
    }

    public Layer getMetaLayer(Layer bottomType, float pressure) {
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

    public float removeAndChangeMass(GeoCell cell, float mass, Layer bottomType, Layer toType) {
        float massToChange = cell.remove(mass, false, false);
        massToChange = Tools.changeMass(massToChange, bottomType, toType);
        return massToChange;
    }
	
}
