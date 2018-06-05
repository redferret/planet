
import worlds.planet.Util;
import java.util.Deque;

import org.junit.Test;

import worlds.planet.geosphere.Crust;
import worlds.planet.TestWorld;
import worlds.planet.PlanetCell;

import static org.junit.Assert.*;

/**
 * Does basic unit testing on the Crust class.
 *
 * @author Richard DeSilvey
 */
public class GeoCellTest {


  /**
   * Tests for correct density calculation without oceans.
   */
  @Test
  public void calculateDensityTest() {

  }

  @Test
  public void removeStrataLayersTest() {
    
  }

  /**
   * Tests removal of the strata with the special removal case of removing from
   * the strata where the removal amount exceeds the top or bottom layer being
   * removed from as well as checking to make sure that zero mass layers don't
   * exist.
   */
  @Test
  public void removeAmountFromStrataTest() {

  }

  /**
   * Tests the removal of multiple strata.
   */
  @Test
  public void multipleLayerRemovalTest() {

  }

  /**
   * Helper method for testing ranges.
   *
   * @param rangeDiff The amount of deviation
   * @param testNumber The outcome
   * @param expectedNumber The expected
   * @return True if the outcome is within an accepted range based on the
   * deviation.
   */
  private boolean rangeTest(Float rangeDiff, Float testNumber, Float expectedNumber) {
    Float minimum = expectedNumber - rangeDiff;
    Float maximum = expectedNumber + rangeDiff;

    return testNumber >= minimum && testNumber <= maximum;
  }

  @Test
  public void insertMassTest() {

  }

  /**
   * Tests the functionality of adding similar layer types to the strata. The
   * expected behavior is that adding a similar type of an arbritrary amount
   * won't add additional layers unless the types aren't the same.
   */
  @Test
  public void addingToLayersTest() {

  }

}
