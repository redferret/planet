package planet.gui.basic;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import planet.Planet;
import planet.surface.SurfaceMap;

/**
 * Panel for the JFrame where all rendering takes place for SurfaceMaps
 */
public class Frame extends JPanel {

    private SurfaceMap map;
    private List<BufferedImage> images;

    public Frame(int w, int h) {
        super();
        setSize(w, h);
        setBackground(Color.WHITE);
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

            for (int i = images.size() - 1; i >= 0; i--) {
                g2d.drawImage(images.get(i), 0, 0, getWidth(), getHeight(), null);
            }
        }

        g2d.dispose();
    }

    /**
     * Accesses the given surface map for each cell's render data and sets each
     * individual image's raster to that data.
     *
     * @param map The SurfaceMap being rendered.
     */
    private void setRasterOfEachImage(SurfaceMap map) {

        WritableRaster raster = null;
        List<Integer[]> settings;

        int bounds = Planet.self().getSurface().getGridWidth();

        for (int x = 0; x < bounds; x++) {
            for (int y = 0; y < bounds; y++) {

                settings = map.getCellData(x, y);

                firstTimeInit(settings, bounds);

                for (int i = 0; i < settings.size(); i++) {

                    BufferedImage image = images.get(i);
                    Integer[] color = settings.get(i);

                    int rgba[] = {color[0], color[1], color[2], color[3]};

                    raster = image.getRaster();
                    raster.setPixel(x, y, rgba);
                }

            }
        }
    }

    private void firstTimeInit(List<Integer[]> settings, int bounds) {
        if (images.size() < settings.size()) {
            images.clear();
            settings.forEach(setting -> {
                BufferedImage image = new BufferedImage(bounds, bounds, BufferedImage.TYPE_INT_ARGB);
                images.add(image);
            });
        }
    }
    

}
