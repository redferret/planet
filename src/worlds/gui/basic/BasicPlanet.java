package worlds.gui.basic;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Deque;
import java.util.LinkedList;
import javax.swing.JFrame;
import worlds.planet.Planet;
import worlds.planet.TestWorld;
import worlds.planet.enums.Layer;
import engine.gui.DisplayAdapter;
import engine.surface.SurfaceMap;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import worlds.planet.surface.Geosphere;
import worlds.planet.surface.Hydrosphere;
import worlds.planet.surface.PlanetSurface;

/**
 * The BasicPlanet is to allow a user to see what is happening to the planet
 using Java API JFrame and Java graphics.
 *
 * @author Richard DeSilvey
 */
public class BasicPlanet extends JFrame implements DisplayAdapter {

    private Frame renderFrame;
    private TestWorld testWorld;
    private Deque<Integer> averages;
    private int totalAvg;
    private CrossSection crossSection;
    
    private static final int THREAD_COUNT = 2;
    private static final int SIZE = 512;

    public BasicPlanet() {
        super("Test World");

        averages = new LinkedList<>();
        totalAvg = 0;
        crossSection = new CrossSection(512, 512);
        constructWorld();
        setupJFrame();
        prepareWorld();
    }

    private void prepareWorld() {
        PlanetSurface surface = (PlanetSurface) testWorld.getSurface();
        surface.addToSurface(Layer.BASALT, 100000);
        surface.addWaterToAllCells(10000);
        testWorld.setTimescale(Planet.TimeScale.Geological);
        Geosphere.heatDistributionCount = 100;

        testWorld.play();
    }

    private void setupJFrame() {
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        addMouseListener(new MouseController());
        addMouseMotionListener(new MouseController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void constructWorld() {
        testWorld = new TestWorld(128, THREAD_COUNT);
        testWorld.getSurface().setDisplay(this);
        renderFrame = new Frame(SIZE, SIZE);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
    }

    @Override
    public void update() {
        renderFrame.repaint();
        crossSection.update();
        calculateAverage();

        long age = testWorld.getSurface().getPlanetAge();
        float lowestHeight = testWorld.getSurface().getLowestHeight();
        setTitle("Age: " + age + " F:" + totalAvg + " L:" + lowestHeight);
    }

    private void calculateAverage() {
        averages.add(testWorld.getSurface().getAverageThreadTime());
        final int SAMPLES = 10;

        if (averages.size() == SAMPLES) {
            totalAvg = 0;
            while (!averages.isEmpty()) {
                totalAvg += averages.poll();
            }
            totalAvg /= SAMPLES;
        }
    }
    
    class MouseController extends MouseAdapter {

        @Override
        public void mouseReleased(MouseEvent e) {
            crossSection.setViewX((int) ((e.getX() - 5) * (128f / 512f)));
            crossSection.setViewY((int) ((e.getY() - 28) * (128f / 512f)));
        }
    }

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
            Graphics2D g2d = (Graphics2D) graphics;
            render(g2d);
            g2d.dispose();
        }

        private void render(Graphics2D g2d) {
            if (map != null) {
                setRasterOfEachImage();
                renderEachImage(g2d);
            }
            int x = (int)(crossSection.getViewX() * (512f/128f));
            int y = (int)(crossSection.getViewY() * (512f/128f));
            g2d.drawLine(x, y, x + 35, y);
        }

        private void renderEachImage(Graphics2D g2d) {
            for (int i = images.size() - 1; i >= 0; i--) {
                g2d.drawImage(images.get(i), 0, 0, getWidth(), getHeight(), null);
            }
        }

        /**
         * Accesses the given surface map for each cell's render data and sets
         * each individual image's raster to that data.
         */
        private void setRasterOfEachImage() {

            WritableRaster raster = null;
            List<Integer[]> settings;

            int bounds = Planet.instance().getSurface().getGridWidth();

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

    public static void main(String[] args) {
        new BasicPlanet();
        
    }

}

class KeyController extends KeyAdapter {

    @Override
    public void keyReleased(KeyEvent e) {
        Planet p = Planet.instance();
        switch (e.getKeyCode()) {

            case KeyEvent.VK_INSERT:
                Hydrosphere.drawOcean = !Hydrosphere.drawOcean;
                break;

            case KeyEvent.VK_PAGE_UP:
                p.getSurface().displaySetting++;

                if (p.getSurface().displaySetting > 2) {
                    p.getSurface().displaySetting = 2;
                }
                break;

            case KeyEvent.VK_PAGE_DOWN:
                p.getSurface().displaySetting--;

                if (p.getSurface().displaySetting < 0) {
                    p.getSurface().displaySetting = 0;
                }
                break;

            case KeyEvent.VK_HOME:
                Geosphere.drawSediments = !Geosphere.drawSediments;
                break;
            case KeyEvent.VK_ENTER:

                if (p.getSurface().paused()) {
                    p.play();
                } else {
                    p.pause();
                }
                break;
        }
    }

}

class JAdapter extends WindowAdapter {

    @Override
    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }
}
