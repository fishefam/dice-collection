package com.dice.collection;

import java.util.Random;

/**
 * Blueprint for a die.
 * Objects created from this class has access to:
 * - its sides
 * - current side facing up
 *
 * @author Manh Truong Nguyen
 */
public class Die {
    /* Total sides of the die object */
    private final int sides;
    /* Current side facing up */
    private int upSide;

    /**
     * Constructs a die based on a given number of sides
     *
     * @param sides The sides the die has
     */
    public Die(int sides) {
        this.sides = sides;
        upSide = new Random().nextInt(1, sides); // Initiate with a random side facing up
    }

    /**
     * Give access to the sides of die
     *
     * @return The sides of die
     */
    public int getSides() {
        return sides;
    }

    /**
     * Give access to the side of die facing up
     *
     * @return The value of the side facing up
     */
    public int getUpSide() {
        return upSide;
    }

    /**
     * Roll the die once
     */
    public void roll() {
        upSide = new Random().nextInt(1, sides + 1);
    }

    /**
     * Give information of how many sides the die has and its current side facing up
     *
     * @return The information of die
     */
    @Override
    public String toString() {
        return "has " + sides + " sides - Current up side: " + upSide;
    }
}
