package worlds.planet.geosphere;

import worlds.planet.PlanetCell;
import worlds.planet.enums.Layer;
import worlds.planet.enums.SilicateContent;
import engine.util.task.Task;

import static engine.util.Tools.calcDepth;
import static engine.util.Tools.calcHeight;
import static engine.util.Tools.calcMass;
import static engine.util.Tools.changeMass;
import static worlds.planet.Planet.instance;

/**
 * Forms sedimentary rock.
 * @author Richard DeSilvey
 */
public abstract class RockFormationTask extends Task {

    public void formSedimentaryRock(PlanetCell cell) {

        float height, diff, massBeingDeposited;
        GeoCell.SedimentBuffer eb = cell.getSedimentBuffer();

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
}
