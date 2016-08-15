

package worlds.gui.basic;

import engine.gui.DisplayAdapter;
import engine.util.Tools;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.concurrent.atomic.AtomicInteger;
import javax.swing.JFrame;
import javax.swing.JPanel;
import worlds.planet.Planet;
import worlds.planet.cells.PlanetCell;
import worlds.planet.cells.geology.Stratum;
import worlds.planet.enums.Layer;
import worlds.planet.surface.PlanetSurface;

public class CrossSection extends JFrame implements DisplayAdapter {

    private static final String TITLE;
    private StrataFrame renderFrame;
    private int scroll;
    
    private AtomicInteger viewX, viewY;
    
    static {
        TITLE = "Strata Display";
    }
    private static int HEIGHT_OFFSET = 0;
    
    public CrossSection(int width, int height)  {
        super(TITLE);
        scroll = 0;
        viewX = new AtomicInteger(0);
        viewY = new AtomicInteger(0);
        renderFrame = new StrataFrame();
        add(renderFrame);
        setSize(width, height);
        setResizable(true);
        setVisible(true);
    }
    
    public void setViewY(int viewY) {
        this.viewY.set(viewY);
    }

    public void setViewX(int viewX) {
        this.viewX.set(viewX);
    }
    
    public int getViewY() {
        return viewY.get();
    }

    public int getViewX() {
        return viewX.get();
    }
    
    private class StrataFrame extends JPanel {
    
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            draw((Graphics2D) g);
            setBackground(Color.BLACK);
        }
        
        public void draw(Graphics2D g2d) {

            if (Planet.instance() == null) return;
            
            PlanetSurface surface = (PlanetSurface) Planet.instance().getSurface();

            if (surface == null) return;
            
            int windowWidth = getWidth(), 
                windowHeight = getHeight() - HEIGHT_OFFSET, cutToolWidth = 50;
            int cellWidth = windowWidth/cutToolWidth, cx = viewX.get(), cy = viewY.get();
            final int MAX_THICKNESS = 10;

            double layerThickness, cellThicknessRatio, startDrawHeight;

            Color color;
            PlanetCell cell;
            Stratum nextStratum;

            StringBuilder sb = new StringBuilder();
            sb.append("[").append(cx).append(", ").append(cy).append("]");
            
            g2d.drawString(sb.toString(), cx, cy);
            
            // Draw the cross section
            for (int cellIndex = 0; cellIndex < cutToolWidth; cellIndex++){

                cx = Tools.checkBounds(cellIndex + cx, surface.getGridWidth());
                cy = Tools.checkBounds(cy, surface.getGridWidth());

                cell = surface.getCellAt(cx, cy);

                if (cell != null){

                    cellThicknessRatio = ((float)windowHeight / MAX_THICKNESS);
                    startDrawHeight = (MAX_THICKNESS - cell.getHeight());
                    startDrawHeight *= cellThicknessRatio;

                    // Draw Ocean
                        layerThickness = cell.getOceanHeight() * cellThicknessRatio;

                        drawLayer(g2d, Color.BLUE, cellIndex, cellWidth, 
                                startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);

                        startDrawHeight += layerThickness;

                    // Draw Sediments
                        float sedDepth = Tools.calcHeight(cell.getSedimentBuffer().getSediments(), Planet.instance().getCellArea(), Layer.SEDIMENT.getDensity());
                        layerThickness = sedDepth * cellThicknessRatio;

                        drawLayer(g2d, Layer.SEDIMENT.getColor(), cellIndex, cellWidth, 
                                startDrawHeight, windowHeight, layerThickness, cellThicknessRatio);

                        startDrawHeight += layerThickness;
                        
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
            int y = (int)(startDrawHeight-(windowHeight*(1f/3f))-(scroll*cellThicknessRatio));
            int height = (int)(layerThickness + 1);

            g2d.fillRect(x, y, cellWidth, height);
        }
    }
    
    public void setScroll(int value){
        scroll = value;
    }

    @Override
    public void update() {
        renderFrame.repaint();
    }
    
}
