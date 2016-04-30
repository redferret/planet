
package planet.util;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import planet.Planet;
import planet.surface.Layer;
import planet.surface.GeoCell;
import planet.surface.Surface;

import static planet.surface.generics.SurfaceMap.DIR_X_INDEX;
import static planet.surface.generics.SurfaceMap.DIR_Y_INDEX;

/**
 * A utility class for the system.
 * 
 * @author Richard DeSilvey
 */
public class Tools {

    /**
     * To help create height maps, heat maps, etc, the method
     * takes a list of colors, the colors are then interpolated with
     * each other evenly across the returned sample array.
     * Depending on the width, the number of color samples returned
     * should be greater than the number of colors given.
     * The width specifies the number of samples that will be returned.
     * 
     * @param colors The list of colors
     * @param width The number of colors after interpolation
     * @return The samples array
     */
    public static Integer[][] constructSamples(Color[] colors, int width){
        
        if (colors.length > width){
            throw new IndexOutOfBoundsException("The width of the gradient is invalid");
        }
        
        float[] dist = new float[colors.length];
        float distAmount = 1f / colors.length, totalDist = 0;
        
        for (int i = 0; i < colors.length; i++){
            
            dist[i] = totalDist;
            totalDist += distAmount;
            
        }
        
        dist[colors.length - 1] = 1.0f;
        
        return constructGradient(colors, dist, width);
        
    }
    
    /**
     * In some cases you may not want an even distribution of each color
     * given in the colors array after interpolation. The distribution array must be
     * the same length as the colors array and add up to 1.0f
     * For example, for three colors <code>dist = {0.0f, 0.35f, 1.0f};</code>
     * 
     * @param colors The list of colors
     * @param dist The list of distributions for each color
     * @param width The number of colors after interpolation
     * @return The samples array
     */
    public static Integer[][] constructGradient(Color[] colors, float[] dist, int width){
        
        Integer[][] colorArray = new Integer[width][4];
        
        BufferedImage buffer = new BufferedImage(width, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = buffer.createGraphics();
        
        Point2D start = new Point2D.Float(0, 0);
        Point2D end = new Point2D.Float(width, 0);
        
        LinearGradientPaint lgp = new LinearGradientPaint(start, end, dist, colors);
        
        g2d.setPaint(lgp);
        g2d.fillRect(0, 0, width, 1);
        g2d.dispose();
        
        int[][] b = new int[width][4];
        for (int x = 0, y = 0; x < width; x++){
            buffer.getRaster().getPixel(x, y, b[x]);
            for (int a = 0; a < 4; a ++){
                colorArray[x][a] = b[x][a];
            }
        }
        
        return colorArray;
        
    }
    
    /**
     * After interpolation the samples can be converted to an array
     * of Colors.
     * 
     * @param samples The list of samples
     * @return The list of colors given by the samples array.
     */
    public static Color[] constructColors(int[][] samples){
        
        Color[] colors = new Color[samples.length];
        
        final int RED = 0, GREEN = 1, BLUE = 2, ALPHA = 3;
        
        for (int i = 0; i < colors.length; i++){
            
            colors[i] = new Color(samples[i][RED], samples[i][GREEN], 
                    samples[i][BLUE], samples[i][ALPHA]);
            
        }
        
        return colors;
        
    }

    
    public static int checkXBounds(int tx, int b){
        return (tx >= b) ? tx - b :(tx < 0)? b + tx : tx;
    }

    public static int checkYBounds(int ty, int length){
        
        return (ty >= length) ? ty - length : (ty < 0) ? length + ty : ty;
    }
    
    /**
     * Calculates the mass in kilograms with the provided height and density.
     * The height is based on each cell's base measurements given by the 
     * <code>BASE</code>.
     * 
     * @param height The height in kilometers
     * @param layerType The density in kilograms per cubic meter.
     * @param base The base of the cell in square kilometers.
     * @return The calculated mass in kilograms
     */
    public static float calcMass(float height, long base, Layer layerType){
        return calcMass(height, base, layerType.getDensity());
    }
    
    public static float calcMass(float height, long base, float density){
        return (height * base * density);
    }
    
    /**
     * Calculates the height of some stratum/strata with the given mass
     * and density.
     * @param mass The amount of mass in kilograms
     * @param base The base of the cell in square kilometers.
     * @param layerType The density of the mass in kilograms per cubic meter.
     * @return The height in kilometers.
     */
    public static float calcHeight(float mass, long base, Layer layerType){
        return calcHeight(mass, base, layerType.getDensity());
    }
    
    public static float calcHeight(float mass, long base, float density){
        return mass / (base * density);
    }
    
    public static float changeMass(float massToChange, Layer from, Layer to){
        return changeMass(massToChange, from.getDensity(), to.getDensity());
    }

    public static float changeMass(float massToChange, float fromDensity, float toDensity){
        return (massToChange * fromDensity) / toDensity;
    }
    
    public static float calcDepth(Layer layer, float gravity, float maxPressure){
        return calcDepth(layer.getDensity(), gravity, maxPressure);
    }
    
    public static float calcDepth(float density, float gravity, float maxPressure){
        return (maxPressure / (gravity * density));
    }
    
    public static float calcPressure(float density, float gravity, float depth){
        return depth * density * gravity;
    }
    
    public static float calcLimit(float x) {
        return 0.9f / (1 + (float)Math.exp(2*x - 4)) + 0.1f;
    }
    
    public static float clamp(float heightDiff, float min, float max){
        return (heightDiff < min) ? min : (heightDiff > max ? max : heightDiff);
    }

    public static GeoCell getLowestCellFrom(GeoCell central) {

        if (central == null) {
            return null;
        }

        int tx, ty, mx, my;
        Surface geo = Planet.self().getSurface();

        int x = central.getX(), y = central.getY();
        int xl = DIR_X_INDEX.length;

        GeoCell lowest = central, cell;

        for (int s = 0; s < xl; s++) {
            tx = x + DIR_X_INDEX[s];
            ty = y + DIR_Y_INDEX[s];

            // Check the boundaries
            mx = checkXBounds(tx, Planet.self().getGridSize());
            my = checkYBounds(ty, Planet.self().getGridSize());

            cell = geo.getCellAt(mx, my);

            if (cell.getHeight() < lowest.getHeight()) {
                lowest = cell;
            }
        }

        return lowest;
    }
    
    private Tools() {
    }
    
}
