package planet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import planet.Planet;
import planet.surface.generics.SurfaceMap;

/**
 * Panel for the JFrame where all rendering takes place for SurfaceMaps
 */
public class Frame extends JPanel {

    private DisplayAdapter<Graphics2D> adapter;
    private SurfaceMap map;
    private List<BufferedImage> images;

    public Frame(int w, int h) {
        super();
        setup(w, h, null);
    }

    public Frame(int w, int h, DisplayAdapter adapter) {
        super();
        setup(w, h, adapter);
    }

    private void setup(int w, int h, DisplayAdapter adapter) {
        setSize(w, h);
        setBackground(Color.WHITE);
        this.adapter = adapter;
        images = new ArrayList<>();
    }

    public void registerMap(SurfaceMap map) {
        this.map = map;
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        // Used for double buffering the screen reducing any flicker.
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics;
        
        if (map != null) {
            setRasterOfEachImage(map);
            
            images.forEach(image -> {
                g2d.drawImage(image, 0, 0, getWidth(), getHeight(), null);
            });
        }
 
        if (adapter != null) {
            adapter.draw(g2d);
        }

        g2d.dispose();
    }

    /**
     * Accesses the given surface map for each cell's render data and
     * sets each individual image's raster to that data.
     *
     * @param map The SurfaceMap being rendered.
     */
    private void setRasterOfEachImage(SurfaceMap map) {

        WritableRaster raster = null;
        List<Integer[]> settings;
        
        int bounds = Planet.self().getGridSize();

        for (int x = 0; x < bounds; x++) {
            for (int y = 0; y < bounds; y++) {

                settings = map.getCellSettings(x, y);

                if (images.isEmpty()){
                    settings.forEach(setting ->{
                        images.add(new BufferedImage(bounds, bounds, BufferedImage.TYPE_INT_ARGB));
                    });
                }
                
                for (int i = settings.size() - 1; i >= 0; i--){
                    
                    BufferedImage image = images.get(i);
                    Integer[] color = settings.get(i);
                    
                    int rgba[] = {color[0], color[1], color[2], color[3]};
                    
                    raster = image.getRaster();
                    raster.setPixel(x, y, rgba);
                }

            }
        }
    }
}
