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
import planet.gui.DisplayAdapter;
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

    private static final int THREAD_COUNT = 2;
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
        surface.addToSurface(Layer.BASALT, 1000);
        surface.addLavaToSurface(10000);
        Planet.self().setTimescale(Planet.TimeScale.Geological);
        Geosphere.heatDistributionCount = 180;

        testWorld.play();
    }

    private void setupJFrame() {
        addWindowListener(new JAdapter());
        addKeyListener(new KeyController());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(SIZE, SIZE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void constructWorld() {
        testWorld = new TestWorld(THREAD_COUNT);
        testWorld.getSurface().setDisplay(this);
        renderFrame = new Frame(SIZE, SIZE);
        renderFrame.registerMap(testWorld.getSurface());
        add(renderFrame);
    }

    @Override
    public void update() {
        renderFrame.repaint();

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

    public static void main(String[] args) {
        new BasicPlanet();
    }

}

class KeyController extends KeyAdapter {

    @Override
    public void keyReleased(KeyEvent e) {
        Planet p = Planet.self();
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
