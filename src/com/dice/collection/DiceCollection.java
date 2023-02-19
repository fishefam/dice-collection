package com.dice.collection;

import java.util.Arrays;

/**
 * Blueprint for the collection of dice.
 * Objects created from this class provides access to:
 * - individual dice
 * - information of each dice and all attributes of the collection
 * - sum of current sides facing up
 * - sum when all sides of 1 facing up
 * - sum when all sides of the highest value facing up
 * - ability to roll all dice
 * - ability to roll all dice n times then record those rolls
 *
 * @author Manh Truong Nguyen
 */
public class DiceCollection {
    /* The collection of dice */
    private final Die[] dice;

    /**
     * Constructs the collection using a given array of sides of each die
     *
     * @param sides An array of sides of each die
     */
    public DiceCollection(int[] sides) {
        dice = new Die[sides.length]; // Initiate the array of dice based on the number of sides given. Each element has value of null
        for (int i = 0; i < dice.length; i++)
            dice[i] = new Die(sides[i]); // Instantiate each die the assign to each element of the collection
    }

    /**
     * Get the dice collection
     *
     * @return The dice collection
     */
    public Die[] getDice() {
        return dice;
    }

    /**
     * Sum the values of sides currently facing up
     *
     * @return The sum value
     */
    public int sumUpSides() {
        int sum = 0;
        for (Die upSide : dice) sum += upSide.getUpSide();
        return sum;
    }

    /**
     * Sum all the sides with value of 1
     *
     * @return The sum value
     */
    public int sumMinimum() { int sum = 0;
        for (int i = 0; i < dice.length; i++) sum += 1;
        return sum;
    }

    /**
     * Sum all the sides with the highest value
     *
     * @return The sum value
     */
    public int sumMaximum() {
        int sum = 0;
        for (Die upSide : dice) sum += upSide.getSides();
        return sum;
    }

    /**
     * Roll each die once
     */
    public void rollAll() {
        for (Die die : dice) die.roll();
    }

    /**
     * Give a report of all information of individual dice and the sum values
     *
     * @return The string of collection's information
     */
    @Override
    public String toString() {
        /* Usage of StringBuilder class is suggested by IntelliJ for better performance of string concatenation in loops */
        StringBuilder reportBuilder = new StringBuilder();
        for (int i = 0; i < dice.length; i++)
            reportBuilder.append("Die ").append(i + 1).append(" ").append(dice[i]).append("\n");
        String report = reportBuilder.toString();
        report += "\n" + "Min sum of roll: " + sumMinimum();
        report += "\n" + "Max sum of roll: " + sumMaximum();
        report += "\n" + "Sum of current roll: " + sumUpSides();
        return report;
    }

    /**
     * Rolls the entire collection n times while tracking each roll
     *
     * @param rolls How many times to roll the dice in the collection
     * @return A histogram that tracks the up side sums of rolls
     */
    public int[] histogram(int rolls) {
        /* The difference of min and max sum is the number of possible values that need to be tracked */
        int maxSum = sumMaximum();
        int[] tracker = new int[maxSum];
        for (int i = 0; i < rolls; i++) {
            rollAll();
            int upSideSum = sumUpSides(); // The sum value of sides facing up
            tracker[upSideSum - 1] += 1; // Count that value in the histogram
        }
        return tracker;
    }
}
