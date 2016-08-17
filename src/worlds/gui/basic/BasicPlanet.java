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
import engine.util.BasicTask;
import engine.util.Delay;
import engine.util.Task;
import engine.util.Tools;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JPanel;
import worlds.planet.cells.PlanetCell;
import worlds.planet.cells.geology.GeoCell;
import worlds.planet.cells.geology.Stratum;
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
        PlanetSurface surface = (PlanetSurface) testWorld.getSurface();
        for (int i = 0; i < 4; i++){
            surface.addToSurface(Layer.BASALT, 100000);
        }
        testWorld.setTimescale(Planet.TimeScale.Geological);
        Geosphere.heatDistributionCount = 100;

        testWorld.play();
    }

    private void setupJFrame() {
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE*2, SIZE);
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
        testWorld = new TestWorld();
        testWorld.getSurface().setDisplay(this);
        renderFrame = new Frame(SIZE, SIZE);
        renderFrame.registerMap(testWorld.getSurface());
    }

    @Override
    public void update() {
        renderFrame.repaint();
        crossSection.repaint();
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


    /* **************************** Keyboard ******************************/
    private class KeyController extends KeyAdapter {


        @Override
        public void keyPressed(KeyEvent e) {
            checkKeys(e);
        }
        
        private void checkKeys(KeyEvent e){
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    int curY = crossSection.viewY;
                    if (--curY < 0){
                        curY = testWorld.getSurface().getGridWidth() - 1;
                    }
                    crossSection.viewY = curY;
                    break;
                case KeyEvent.VK_DOWN:
                    curY = crossSection.viewY;
                    if (++curY >= testWorld.getSurface().getGridWidth()){
                        curY = 0;
                    }
                    crossSection.viewY = curY;
                    break;
                case KeyEvent.VK_LEFT:
                    int curX = crossSection.viewX;
                    if (--curX < 0){
                        curX = testWorld.getSurface().getGridWidth() - 1;
                    }
                    crossSection.viewX = curX;
                    break;
                case KeyEvent.VK_RIGHT:
                    curX = crossSection.viewX;
                    if (++curX < 0){
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
                    if (crossSection.LAYER_THICKNESS < 1){
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
            draw((Graphics2D) g);
            setBackground(Color.BLACK);
        }
        
        public void draw(Graphics2D g2d) {

            if (Planet.instance() == null) {
                return;
            }

            PlanetSurface surface = (PlanetSurface) Planet.instance().getSurface();

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
                    layerThickness = cell.getOceanHeight() * cellThicknessRatio;

                    drawLayer(g2d, Color.BLUE, cellIndex, cellWidth,
                            startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);

                    startDrawHeight += layerThickness;

                    // Draw Sediments
                    GeoCell.SedimentBuffer sedimentBuffer = cell.getSedimentBuffer();
                    Layer sedimentType = sedimentBuffer.getSedimentType();
                    if (sedimentType != null){
                        float sedDepth = Tools.calcHeight(sedimentBuffer.getSediments(), Planet.instance().getCellArea(), sedimentType.getDensity());
                        layerThickness = sedDepth * cellThicknessRatio;
                        drawLayer(g2d, sedimentType.getColor(), cellIndex, cellWidth,
                                startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);

                        startDrawHeight += layerThickness;
                    }
                    
                    nextStratum = cell.peekTopStratum();

                    while (nextStratum != null) {
                        layerThickness = nextStratum.getHeight() * cellThicknessRatio;
                        color = nextStratum.getLayer().getColor();
                        drawLayer(g2d, color, cellIndex, cellWidth,
                                startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);
                        startDrawHeight += layerThickness;
                        nextStratum = nextStratum.next();
                    }
                }
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
            g2d.setColor(Color.BLACK);
            int vx = crossSection.viewX;
            int vy = crossSection.viewY;
            
            int x = (int)(vx * (512f/(128f)));
            int y = (int)(vy * (512f/(128f)));
            g2d.drawLine(x, y, x+35, y);
            g2d.drawLine(x, y+1, x+35, y+1);
            
            StringBuilder sb = new StringBuilder();
            sb.append("[").append(vx).append(", ").append(vy).append("]");
            
            g2d.drawString(sb.toString(), x, y);
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
