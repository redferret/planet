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
import engine.surface.SurfaceThread;
import engine.util.Delay;
import engine.util.Tools;
import engine.util.task.BasicTask;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JPanel;
import worlds.planet.cells.PlanetCell;
import worlds.planet.cells.geology.GeoCell;
import worlds.planet.cells.geology.Stratum;
import worlds.planet.surface.Geosphere;
import worlds.planet.surface.Hydrosphere;
import worlds.planet.surface.PlanetSurface;

/**
 * The BasicPlanet is to allow a user to see what is happening to the planet
 * using Java API JFrame and Java graphics.
 *
 * @author Richard DeSilvey
 */
public class BasicPlanet extends JFrame implements DisplayAdapter {

    private Frame renderFrame;
    private TestWorld testWorld;
    private Deque<Integer> averages;
    private int totalAvg;
    private StrataFrame crossSection;

    private static final int SIZE = 512;

    public BasicPlanet() {
        super("Test World");

        averages = new LinkedList<>();
        totalAvg = 0;
        constructWorld();
        setupJFrame();
        prepareWorld();
    }

    private void prepareWorld() {
        PlanetSurface surface = testWorld.getSurface();
        PlanetCell cell;
        for (int i = 0; i < 30; i++) {
            surface.addToSurface(Layer.BASALT, 2000);
        }
        for (int i = 0; i < 50; i++) {
            cell = surface.waitForCellAt(i, 0);
            cell.add(Layer.BASALT, 250000, true);
            surface.release(cell);

            cell = surface.waitForCellAt(i, 2);
            cell.add(Layer.BASALT, 250000, true);
            surface.release(cell);
        }
        cell = surface.waitForCellAt(0, 1);
        cell.add(Layer.BASALT, 250000, true);
        surface.release(cell);
        for (int i = 0; i < 10; i++) {
            cell = surface.waitForCellAt(i, 1);
            cell.add(Layer.BASALT, 200000, true);
            surface.release(cell);
        }
        for (int i = 10; i < 20; i++) {
            cell = surface.waitForCellAt(i, 1);
            cell.add(Layer.BASALT, 150000, true);
            surface.release(cell);
        }
        for (int i = 20; i < 30; i++) {
            cell = surface.waitForCellAt(i, 1);
            cell.add(Layer.BASALT, 100000, true);
            surface.release(cell);
        }
        for (int i = 30; i < 40; i++) {
            cell = surface.waitForCellAt(i, 1);
            cell.add(Layer.BASALT, 50000, true);
            surface.release(cell);
        }

//        surface.addTask(new AddWaterTask());
//        surface.addWaterToAllCells(9000);
        testWorld.setTimescale(Planet.TimeScale.Geological);
        Geosphere.heatDistributionCount = 100;
        testWorld.play();
    }

    private void setupJFrame() {
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(2 * SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);

        GridLayout layout = new GridLayout(1, 2);
        setLayout(layout);
        crossSection = new StrataFrame();
        crossSection.setSize(128, 128);
        add(renderFrame);
        add(crossSection);
    }

    private void constructWorld() {
        testWorld = new TestWorld(50, 1);
        testWorld.getSurface().setDisplay(this);
        renderFrame = new Frame(SIZE, SIZE);
    }

    @Override
    public void update() {
        repaint();
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

    private class AddWaterTask extends BasicTask {

        private Delay delay;

        @Override
        public void before() {
            if (delay == null) {
                delay = new Delay(2);
            }
        }

        @Override
        public void after() {
        }

        @Override
        public void perform() {
            if (delay.check()) {
                PlanetCell cell = testWorld.getSurface().waitForCellAt(1, 1);
                cell.addOceanMass(1);
                testWorld.getSurface().release(cell);
            }
        }

    }

    /* **************************** Keyboard ******************************/
    private class KeyController extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            checkKeys(e);
        }

        private void checkKeys(KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    int curY = crossSection.viewY;
                    if (--curY < 0) {
                        curY = testWorld.getSurface().getGridWidth() - 1;
                    }
                    crossSection.viewY = curY;
                    break;
                case KeyEvent.VK_DOWN:
                    curY = crossSection.viewY;
                    if (++curY >= testWorld.getSurface().getGridWidth()) {
                        curY = 0;
                    }
                    crossSection.viewY = curY;
                    break;
                case KeyEvent.VK_LEFT:
                    int curX = crossSection.viewX;
                    if (--curX < 0) {
                        curX = testWorld.getSurface().getGridWidth() - 1;
                    }
                    crossSection.viewX = curX;
                    break;
                case KeyEvent.VK_RIGHT:
                    curX = crossSection.viewX;
                    if (++curX < 0) {
                        curX = testWorld.getSurface().getGridWidth() - 1;
                    }
                    crossSection.viewX = curX;
                    break;
            }
        }

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
                case KeyEvent.VK_NUMPAD7:
                    crossSection.LAYER_THICKNESS--;
                    if (crossSection.LAYER_THICKNESS < 1) {
                        crossSection.LAYER_THICKNESS = 1;
                    }
                    break;
                case KeyEvent.VK_NUMPAD4:
                    crossSection.LAYER_THICKNESS++;
                    break;
            }
        }

    }

    /* **************************** Panels ********************************/
    private class StrataFrame extends JPanel {

        public int viewX = 0, viewY = 0;
        public int LAYER_THICKNESS = 8;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            try {
                draw((Graphics2D) g);
                setBackground(Color.BLACK);
            } catch (Exception e) {
                Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        public void draw(Graphics2D g2d) {

            if (Planet.instance() == null) {
                return;
            }

            PlanetSurface surface = testWorld.getSurface();

            if (surface == null) {
                return;
            }

            int windowWidth = getWidth(),
                    windowHeight = getHeight(), cutToolWidth = 50;
            int cellWidth = windowWidth / cutToolWidth, cx = viewX, cy = viewY;

            double layerThickness, cellThicknessRatio, startDrawHeight;

            Color color;
            PlanetCell cell;
            Stratum nextStratum;

            StringBuilder sb = new StringBuilder();
            sb.append("[").append(cx).append(", ").append(cy).append("]");

            g2d.drawString(sb.toString(), cx, cy);

            // Draw the cross section
            for (int cellIndex = 0; cellIndex < cutToolWidth; cellIndex++) {

                cx = Tools.checkBounds(cellIndex + viewX, surface.getGridWidth());
                cy = Tools.checkBounds(viewY, surface.getGridWidth());

                cell = surface.getCellAt(cx, cy);

                if (cell != null) {

                    cellThicknessRatio = ((float) windowHeight / LAYER_THICKNESS);
                    startDrawHeight = (LAYER_THICKNESS - cell.getHeight());
                    startDrawHeight *= cellThicknessRatio;

                    // Draw Ocean
                    if (cell.hasOcean()) {
                        layerThickness = cell.getOceanHeight() * cellThicknessRatio;

                        drawLayer(g2d, Color.BLUE, cellIndex, cellWidth,
                                startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);

                        startDrawHeight += layerThickness;
                    }
                    // Draw Sediments
                    GeoCell.SedimentBuffer sedimentBuffer = cell.getSedimentBuffer();
                    Layer sedimentType = sedimentBuffer.getSedimentType();
                    if (sedimentType != null) {
                        float sedDepth = Tools.calcHeight(sedimentBuffer.getSediments(), Planet.instance().getCellArea(), sedimentType.getDensity());
                        layerThickness = sedDepth * cellThicknessRatio;
                        drawLayer(g2d, sedimentType.getColor(), cellIndex, cellWidth,
                                startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);

                        startDrawHeight += layerThickness;
                    }

                    nextStratum = cell.peekTopStratum();

                    while (nextStratum != null) {
                        layerThickness = nextStratum.getThickness() * cellThicknessRatio;
                        color = nextStratum.getLayer().getColor();
                        drawLayer(g2d, color, cellIndex, cellWidth,
                                startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);
                        startDrawHeight += layerThickness;
                        nextStratum = nextStratum.next();
                    }
                }
                surface.release(cell);
            }
        }

        /**
         * Draws a rectangle on the screen representing the stratum.
         *
         * @param g2d The graphics device
         * @param color The color of the stratum
         * @param cellIndex The cell index (The cell being rendered)
         * @param cellWidth The width of the cell in pixels
         * @param startDrawHeight The top position of the stratum
         * @param windowHeight The height of the window
         * @param layerThickness The thickness of the stratum
         * @param cellThicknessRatio A scaling value for the thickness of the
         * stratum on the screen
         */
        private void drawLayer(Graphics2D g2d, Color color, int cellIndex, int cellWidth,
                double startDrawHeight, int windowHeight, double layerThickness, double cellThicknessRatio) {

            g2d.setColor(color);

            int x = cellIndex * cellWidth;
            int y = (int) (startDrawHeight - (windowHeight * (1f / 3f)));
            int height = (int) (layerThickness + 1);

            g2d.fillRect(x, y, cellWidth, height);
        }
    }

    private class Frame extends JPanel {

        private List<BufferedImage> images;

        public Frame(int w, int h) {
            super();
            setSize(w, h);
            setBackground(Color.WHITE);
            images = new ArrayList<>();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            try {
                Graphics2D g2d = (Graphics2D) graphics;
                setRasterOfEachImage();
                renderEachImage(g2d);
                g2d.dispose();
            } catch (Exception e) {
                Logger.getLogger(SurfaceThread.class.getName()).log(Level.SEVERE, null, e);
            }
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
        public void setRasterOfEachImage() {

            WritableRaster raster = null;
            List<Integer[]> dataSets;

            PlanetSurface surface = testWorld.getSurface();
            int bounds = surface.getGridWidth();

            for (int x = 0; x < bounds; x++) {
                for (int y = 0; y < bounds; y++) {

                    dataSets = surface.getCellData(x, y);

                    firstTimeInit(dataSets, bounds);

                    for (int dataSetIndex = 0; dataSetIndex < dataSets.size(); dataSetIndex++) {

                        BufferedImage image = images.get(dataSetIndex);
                        Integer[] color = dataSets.get(dataSetIndex);

                        int rgba[] = {color[0], color[1], color[2], color[3]};

                        raster = image.getRaster();
                        raster.setPixel(x, y, rgba);
                    }

                }
            }
        }

        private void firstTimeInit(List<Integer[]> dataSets, int bounds) {
            if (images.size() < dataSets.size()) {
                images.clear();
                dataSets.forEach(dataSet -> {
                    BufferedImage image = new BufferedImage(bounds, bounds, BufferedImage.TYPE_INT_ARGB);
                    images.add(image);
                });
            }
        }

    }

    /* **************************** Main ******************************/
    public static void main(String[] args) {
        new BasicPlanet();

    }

}

class JAdapter extends WindowAdapter {

    @Override
    public void windowClosed(WindowEvent e) {
        System.exit(0);
    }
}
