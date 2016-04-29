package planet.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import javax.swing.JPanel;
import planet.Planet;
import planet.surface.generics.SurfaceMap;

/**
 * Panel for the JFrame where all rendering takes place for SurfaceMaps
 */
public class Frame extends JPanel {

    private DisplayAdapter<Graphics2D> adapter;
    private ArrayList<SurfaceMap> maps;

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
        maps = new ArrayList<>();
        this.adapter = adapter;
    }

    public void registerMap(SurfaceMap map) {
        maps.add(map);
    }

    @Override
    protected void paintComponent(Graphics graphics) {

        // Used for double buffering the screen reducing any flicker.
        super.paintComponent(graphics);

        Graphics2D g2d = (Graphics2D) graphics;

        if (maps != null) {

            maps.forEach((SurfaceMap map) -> {
                setRaster(map);
                g2d.drawImage(map.getImage(), 0, 0, getWidth(), getHeight(), null);
            });
        }

        if (adapter != null) {
            adapter.draw(g2d);
        }

        g2d.dispose();
    }

    /**
     * Sets the raster of the image to the data given by the SurfaceMap.
     *
     * @param image
     * @param map
     */
    private void setRaster(SurfaceMap map) {

        Object[] colors = map.renderLookup();

        if (colors == null) {
            return;
        }

        WritableRaster raster;

        BufferedImage image = map.getImage();
        raster = image.getRaster();

        Color color;

        int dataIndex, bounds = Planet.self().getGridSize();

        for (int x = 0; x < bounds; x++) {
            for (int y = 0; y < bounds; y++) {

                dataIndex = map.getCellRenderIndex(x, y);

                if (dataIndex < 0 || dataIndex > colors.length - 1) {
                    continue;
                }

                color = (Color) colors[dataIndex];
                int rgba[] = {color.getRed(), color.getGreen(),
                    color.getBlue(), color.getAlpha()};

                raster.setPixel(x, y, rgba);
            }
        }
    }
}
