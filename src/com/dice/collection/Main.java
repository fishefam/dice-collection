package com.dice.collection;

import java.util.Arrays;
import java.util.OptionalInt;
import java.util.Scanner;

/**
 * This class is the view of Dice Collection application. All user inputs and outputs
 * are in the console. All inputs of inappropriate numbers are sanitized and errors
 * of invalid inputs are handled.
 *
 * @author Manh Truong Nguyen
 */
public class Main {

    /**
     * Entry point method of application
     *
     * @param args unused
     */
    public static void main(String[] args) {
        /* Some information for the user before using application */
        System.out.println("\n* Entries of decimal numbers get the decimal places truncated.");
        System.out.println("* Entries of negative numbers get converted into positive numbers.");
        System.out.println("* A die has at least 2 sides.\n");

        /* This scanner object is used throughout the entire app */
        Scanner scanner = new Scanner(System.in);

        /* Store the number of how many dice the user enters.
           This will loop for user input until a valid value is entered. */
        int numberOfDice = loopNumberInput(scanner, 1, "How many dice? ", "Need at least 1 die. Try again.", "Only numbers are allowed. Try again.");

        /* Initiate an array of dice from user input */
        final int[] dice = new int[numberOfDice];

        /* Store the number of sides of each die.
           Inputs for each die's side are looped until a valid value is entered. */
        for (int i = 0; i < numberOfDice; i++)
            dice[i] = loopNumberInput(scanner, 2, "Enter the sides for Die " + (i + 1) + ": ", "Need at least 2 sides. Try again.", "Only numbers are allowed. Try again");

        /* Instantiate a new collection of dice */
        DiceCollection diceCollection = new DiceCollection(dice);
        System.out.println("\n" + diceCollection); // Output the info of all dice

        /* Stores user's selection to quit application */
        String quitSelection = "";

        /* Keep looping until user quits */
        while (!hasString(quitSelection, new String[]{"quit", "q", "exit", "e"})) {
            System.out.println("\nRoll Options: \t 1. Once \t 2. 100,000 times");
            System.out.print("Enter an option number or type in the option to select: ");

            /* Stores user's selection of roll option */
            String selection = scanner.nextLine().trim();

            boolean is1Selected = hasString(selection, new String[]{"1", "once", "one time", "1 time"}); // Check if option 1 is selected
            boolean is2Selected = hasString(selection, new String[]{"2", "100000", "100,000", "100000 times", "100,000 times"}); // Check of if option 2 is elected

            /* If users choose to roll once, show the result by printing the dice they got and the sum */
            if (is1Selected) {
                diceCollection.rollAll();
                int upSideSum = diceCollection.sumUpSides(); // The sum of current sides that are facing up
                System.out.println("\nRolled all dice once.\nThe sum of all up sides is: " + upSideSum);

                /* The array of all dice after getting rolled */
                Die[] rolledDice = diceCollection.getDice();
                for (int i = 0; i < rolledDice.length; i++)
                    System.out.println("Current up side of Die " + (i + 1) + ": " + rolledDice[i].getUpSide());
            }

            /* If users choose 100,000 rolls, output the histogram of the rolls */
            if (is2Selected) {
                final int BASE_UNIT = 200; // Each star in the bar graph represents this amount
                final int ROLLS = 100_000; // How many times to roll all dice
                int[] tracker = diceCollection.histogram(ROLLS); // Get the histogram result after rolling
                StringBuilder output = new StringBuilder();

                /* The highest value of sums is also the length of the histogram result array */
                String highestSum = String.valueOf(tracker.length);
                /* The highest value of tracker count is extracted by using max method of a Stream created from the tracker array */
                OptionalInt highestTrackerVal = Arrays.stream(tracker).max();
                String highestTrackerValue = String.valueOf(highestTrackerVal.isPresent() ? highestTrackerVal.getAsInt() : "");

                /* For each tracking value, add paddings, add stars of bar graph, then concatenate to output */
                for (int i = 0; i < tracker.length; i++) {
                    String sumValue = String.valueOf(i + 1);
                    int sumValueMissingSpaces = getPaddingSpaces(sumValue, highestSum);
                    String paddedSumValue = padLeftString(sumValue, sumValueMissingSpaces);

                    String trackerValue = String.valueOf(tracker[i]);
                    int trackerValueMissingSpaces = getPaddingSpaces(trackerValue, highestTrackerValue);
                    String paddedTrackerValue = padLeftString(trackerValue, trackerValueMissingSpaces);

                    int numberOfStars = Math.round((float) (tracker[i] / BASE_UNIT));
                    /* StringBuilder and repeat and append methods are suggested by IntelliJ for better performance when concatenating string inside of loops */
                    StringBuilder stars = new StringBuilder();
                    stars.append("*".repeat(Math.max(0, numberOfStars)));

                    if (tracker[i] > 0)
                        output.append(paddedSumValue).append(": ").append(paddedTrackerValue).append("\t").append(stars).append("\n");
                }

                System.out.println("\nHistogram of " + ROLLS + " times rolling all dice: ");
                System.out.println(output);
                System.out.println("Each star represents " + BASE_UNIT + ".");
            }

            if (!is1Selected && !is2Selected)
                System.out.println("Invalid selection. Try again."); // Output error of no option being selected

            /* Input and output of quit option */
            if (is1Selected || is2Selected) {
                System.out.print("\nEnter \"[q]uit\" or \"[e]xit\" to close the program or anything else to continue rolling dice: ");
                quitSelection = scanner.nextLine();
                if (hasString(quitSelection, new String[]{"quit", "q", "exit", "e"})) System.out.println("Bye!");
            }
        }
    }

    /**
     * This method loops user inputs until a valid value
     * is entered otherwise an error message is output in the console.
     *
     * @param scanner            The main scanner object throughout the main method
     * @param min                User input must be greater or equal to this integer
     * @param intro              A string to prompt user input
     * @param lessThanMinError   An error message when user enters a value less than min
     * @param invalidNumberError An error message when user does not enter a valid number
     * @return A valid user input
     */
    public static int loopNumberInput(Scanner scanner, int min, String intro, String lessThanMinError, String invalidNumberError) {
        int input;
        while (true) try {
            System.out.print(intro);
            input = (int) Math.abs(scanner.nextDouble());
            if (input < min) throw new Exception("ValueLowerThanMin");
            break;
        } catch (Exception exception) {
            if (exception.getMessage().equals("ValueLowerThanMin")) System.out.println(lessThanMinError);
            if (exception instanceof java.util.InputMismatchException) System.out.println(invalidNumberError);
            scanner.nextLine(); // Clear buffer of scanner. Remove this call will cause an infinite loop of catch block
        }
        scanner.nextLine(); // Clear buffer of scanner
        return input;
    }

    /**
     * This method checks if an array of strings contains
     * a given string
     *
     * @param string     The string to check
     * @param references The array of reference strings
     * @return A boolean whether the array has the given string
     */
    public static boolean hasString(String string, String[] references) {
        boolean answer = false;
        for (String reference : references)
            if (string.equalsIgnoreCase(reference)) {
                answer = true;
                break;
            }
        return answer;
    }

    /**
     * This method adds padding to the left of a string
     *
     * @param string The string to add paddings
     * @param spaces Number of spaces to pad
     * @return The padded string
     */
    public static String padLeftString(String string, int spaces) {
        return " ".repeat(Math.max(0, spaces)) + string;
    }

    /**
     * This method finds how many spaces a string misses as compared to a reference string
     *
     * @param target    The string to find missing spaces
     * @param reference The string to compare to
     * @return The number of space the target string misses
     */
    public static int getPaddingSpaces(String target, String reference) {
        return reference.length() - target.length();
    }
}
