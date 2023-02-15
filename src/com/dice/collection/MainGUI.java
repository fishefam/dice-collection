package com.dice.collection;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.OptionalInt;

/**
 * This class is the view of Dice Collection GUI application.
 * It contains:
 * - 2 primary methods:
 * + main
 * + start
 * - 6 node creator methods:
 * + createTitle
 * + createButtons
 * + createHistogramSection
 * + createInfoSection
 * + createInputSection
 * + createInputField
 * - 3 utility methods:
 * + isInteger
 * + isDigit
 * + parseInt
 * Overall vertical layout: Title -> Buttons -> Main area.
 * All parts of overall vertical layout are contained and positioned inside a BorderPane.
 * <p>
 * Main area contains 3 sections horizontally positioned from left to right and created by:
 * + createInputSection
 * + createInfoSection
 * + createHistogramSection
 * <p>
 * LIMITATIONS: Due to the size limit of the canvas to draw the histogram, number of dice is
 * limited to below 7 and number of sizes of each die is limited to below 10
 *
 * @author Manh Truong Nguyen
 */
public class MainGUI extends Application {
    /* Indicate whether it is the first time use clicks on Roll Thousands button */
    private boolean isFirstThousandRoll = true;
    /* Stores the collection of dice */
    private DiceCollection diceCollection;
    /* Stores the array of counts returned from calling histogram method of diceCollection */
    private int[] tracker;

    /**
     * Entry point of Dice Collection GUI App
     *
     * @param args Passed to the launch method of JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }

    /**
     * Entry point of JavaFX
     *
     * @param stage the primary stage for this application, onto which
     *              the application scene can be set.
     *              Applications may create other stages, if needed, but they will not be
     *              primary stages.
     */
    @Override
    public void start(Stage stage) {
        Text title = createTitle("Dice Collection"); // Main header

        Buttons buttons = createButtons(); // Buttons for user control

        /* Create 3 sections from creator methods and put in the main BorderPane in order from left to right */
        VBox inputSection = createInputSection(buttons.rollOne(), buttons.rollThousands());
        VBox infoSection = createInfoSection();
        VBox histogramSection = createHistogramSection();
        BorderPane main = new BorderPane(infoSection, null, histogramSection, null, inputSection);

        /* Container of title, buttons, and main area */
        VBox root = new VBox();
        root.setPadding(new Insets(50, 50, 50, 50));
        root.setSpacing(50);
        root.setAlignment(Pos.TOP_CENTER);
        root.getChildren().addAll(title, buttons.container(), main);

        Scene scene = new Scene(root); // Main scene

        // Config primary stage then show
        stage.setScene(scene);
        stage.setWidth(1920);
        stage.setHeight(1080);
        stage.setMaximized(true);
        stage.setResizable(false);
        stage.setTitle("Dice Collection");
        stage.show();
    }

    /**
     * Creates the title header
     *
     * @param title A string to put as the title
     * @return a Text node with the string argument as title
     */
    public Text createTitle(String title) {
        Text text = new Text(title);
        text.setFont(Font.font("Verdana", FontWeight.BOLD, 28));
        text.setFill(Color.RED);
        text.setTextAlignment(TextAlignment.CENTER);
        return text;
    }

    /**
     * Creates 2 buttons: Roll Once, Roll 100000 times
     * Create 1 container for 2 buttons
     * The return type of this method is a custom record at the end of this file
     *
     * @return An object of the button container and the buttons themselves
     */
    public Buttons createButtons() {
        // Creates 2 buttons with labels
        Button rollOnce = new Button("Roll Once");
        Button rollThousands = new Button("Roll 100,000 Times");

        // Buttons are disabled by default
        rollOnce.setDisable(true);
        rollThousands.setDisable(true);

        // Clicking on Roll Once button clears the tracker and rolls all dice of the collection
        rollOnce.setOnAction((event) -> {
            tracker = null;
            diceCollection.rollAll();
        });
        // Clicking on Roll 100000 Times set the return value of histogram method to tracker
        // From here it is no longer the first time user clicks on this button
        rollThousands.setOnAction((event) -> {
            tracker = diceCollection.histogram(100_000);
            isFirstThousandRoll = false;
        });

        // Container of 2 buttons
        HBox container = new HBox(rollOnce, rollThousands);
        container.setSpacing(15);
        container.setAlignment(Pos.CENTER);

        return new Buttons(container, rollOnce, rollThousands);
    }

    /**
     * Creates the histogram section with 1 header, 1 canvas to draw the bar graph
     * The bar graph is updated whenever the user clicks Roll 100000 Times or
     * whenever the user changes any input values after the user has clicked on Roll 100000 Times once
     * Updates are done with JavaFX Timeline and KeyFrame. All elements inside the canvas
     * is redrawn every 17 milliseconds (60fps)
     *
     * @return The container of all child nodes
     */
    public VBox createHistogramSection() {
        /* Container of child nodes */
        VBox container = new VBox();
        container.setSpacing(15);
        container.setAlignment(Pos.TOP_CENTER);

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.017), (event) -> {
            // Constants that define the sizes of canvas and child elements
            final int CHART_CONTAINER_WIDTH = 600;
            final int CHART_CONTAINER_HEIGHT = 600;
            final int CHART_CONTAINER_PADDING = 60;
            final int CHART_WIDTH = CHART_CONTAINER_WIDTH - CHART_CONTAINER_PADDING * 2;
            final int CHART_HEIGHT = CHART_CONTAINER_HEIGHT - CHART_CONTAINER_PADDING * 2;
            final int CHART_Y_AXIS_TICKS = 10;
            final int CHART_Y_AXIS_TICK_GAP = CHART_HEIGHT / CHART_Y_AXIS_TICKS;
            final int TICK_LENGTH = 5;

            // Clear everything on each frame
            container.getChildren().clear();

            // Header of section
            Text title = new Text("Histogram");
            title.setFont(Font.font("Verdana", FontWeight.BOLD, 17));
            title.setTextAlignment(TextAlignment.CENTER);
            title.setFill(Color.GREEN);

            // Canvas to draw the bar graph
            Canvas chartContainer = new Canvas(CHART_CONTAINER_WIDTH, CHART_CONTAINER_HEIGHT);
            GraphicsContext gc = chartContainer.getGraphicsContext2D();
            gc.setFill(Color.DARKGRAY);
            gc.clearRect(0, 0, CHART_CONTAINER_WIDTH, CHART_CONTAINER_HEIGHT);
            gc.fillRect(0, 0, CHART_CONTAINER_WIDTH, CHART_CONTAINER_HEIGHT);

            // Only draw bar graph if tracker has the return value from histogram method
            if (tracker != null) {
                final double CHART_X_AXIS_TICK_GAP = CHART_WIDTH / (double) tracker.length;

                /* The highest value of tracker count is extracted by using max method of a Stream created from the tracker array */
                OptionalInt highestTrackerVal = Arrays.stream(tracker).max();
                int highestTrack = highestTrackerVal.isPresent() ? highestTrackerVal.getAsInt() : 0;


                // Config attributes of bar graph
                gc.setFill(Color.LIGHTGRAY);
                gc.setStroke(Color.LIGHTGRAY);
                gc.setLineWidth(0.4);

                // Below lines contain redundant variables for understandability of code
                // DO NOT remove these variables based on IntelliJ's warnings
                // Draw x-axis
                double yAxisTopX = CHART_CONTAINER_PADDING;
                double yAxisTopY = CHART_CONTAINER_PADDING;
                double yAxisBottomX = CHART_CONTAINER_PADDING;
                double yAxisBottomY = CHART_CONTAINER_PADDING + CHART_HEIGHT;
                gc.strokeLine(yAxisTopX, yAxisTopY, yAxisBottomX, yAxisBottomY);

                // Draw y-axis
                double xAxisLeftX = CHART_CONTAINER_PADDING;
                double xAxisLeftY = CHART_CONTAINER_PADDING + CHART_HEIGHT;
                double xAxisRightX = CHART_CONTAINER_PADDING + CHART_WIDTH;
                double xAxisRightY = CHART_CONTAINER_PADDING + CHART_HEIGHT;
                gc.strokeLine(xAxisLeftX, xAxisLeftY, xAxisRightX, xAxisRightY);

                // Draw ticks on y-axis
                for (int i = 0; i <= CHART_Y_AXIS_TICKS; i++) {
                    double yAxisTickLeftX = CHART_CONTAINER_PADDING - TICK_LENGTH;
                    double yAxisTickLeftY = yAxisBottomY - i * CHART_Y_AXIS_TICK_GAP;
                    double yAxisTickRightX = CHART_CONTAINER_PADDING;
                    double yAxisTickRightY = yAxisBottomY - i * CHART_Y_AXIS_TICK_GAP;

                    double yAxisDashLineLeftX = CHART_CONTAINER_PADDING;
                    double yAxisDashLineLeftY = yAxisBottomY - i * CHART_Y_AXIS_TICK_GAP;
                    double yAxisDashLineRightX = CHART_CONTAINER_PADDING + CHART_WIDTH;
                    double yAxisDashLineRightY = yAxisBottomY - i * CHART_Y_AXIS_TICK_GAP;

                    gc.strokeLine(yAxisTickLeftX, yAxisTickLeftY, yAxisTickRightX, yAxisTickRightY);
                    gc.setLineDashes(2);
                    gc.strokeLine(yAxisDashLineLeftX, yAxisDashLineLeftY, yAxisDashLineRightX, yAxisDashLineRightY);
                    gc.setTextAlign(TextAlignment.RIGHT);
                    gc.fillText(String.valueOf(i * highestTrack / CHART_Y_AXIS_TICKS), yAxisTickLeftX - 10, yAxisTickLeftY + 3);
                }

                // For every value in tracker array that has 0 count, shift all bars to the left 1
                int shiftBarsLeft = 0;

                // Draw bars on the chart
                for (int i = 0; i < tracker.length; i++) {
                    // If a value has 0 count, add 1 shifting
                    if (tracker[i] == 0) shiftBarsLeft += 1;
                    // Only draw a bar and the sum if its count is more than 0
                    if (tracker[i] > 0) {
                        double barWidth = (CHART_WIDTH / (double) tracker.length) - 10;
                        double barHeight = CHART_HEIGHT * (double) tracker[i] / highestTrack;

                        // The point to start drawing a bar
                        double barTopX = xAxisLeftX + (i - shiftBarsLeft) * CHART_X_AXIS_TICK_GAP;
                        double barTopY = xAxisLeftY;

                        // The point to start drawing a sum value of dice sides facing up
                        double valueX = (xAxisLeftX + (i - shiftBarsLeft) * CHART_X_AXIS_TICK_GAP) + barWidth / 2;
                        double valueY = xAxisLeftY + TICK_LENGTH + 10;

                        gc.setFill(Color.CYAN);

                        // Draw bar
                        gc.setFill(Color.LIGHTBLUE);
                        gc.fillRect(barTopX, barTopY - barHeight, barWidth, barHeight);

                        // Draw the sum value
                        gc.setFill(Color.LIGHTGRAY);
                        gc.setTextAlign(TextAlignment.CENTER);
                        gc.fillText(String.valueOf(i + 1), valueX, valueY);
                    }
                }

                gc.fillText("Possible Values from Adding Dice Sides", CHART_CONTAINER_PADDING + CHART_CONTAINER_WIDTH / 3, CHART_CONTAINER_HEIGHT - CHART_CONTAINER_PADDING / 3);
            }

            // Add header and bar chart to container
            container.getChildren().addAll(title, chartContainer);
        });

        // Timeline to update bar chart by continuously redrawing
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        return container;
    }

    /**
     * Creates the section to display info of dice collection
     * This method uses JavaFX Timeline and Keyframe API to update dice collection
     * info constantly every 17 milliseconds (60fps)
     *
     * @return the container of info section
     */
    public VBox createInfoSection() {
        // Section container
        VBox container = new VBox();
        container.setSpacing(15);
        container.setAlignment(Pos.TOP_CENTER);

        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.017), (event) -> {
            // Clear content on every frame
            container.getChildren().clear();

            // Header of section
            Text title = new Text("Info");
            title.setFont(Font.font("Verdana", FontWeight.BOLD, 17));
            title.setFill(Color.GREEN);

            // Push main contents of dice collection information to a text box
            Text info = new Text();
            info.setText("");
            info.setFont(Font.font(15));
            if (diceCollection != null) info.setText(diceCollection.toString());

            // Add everything to their container
            container.getChildren().addAll(title, info);
        });

        // Run timeline to update screen
        Timeline timeline = new Timeline(keyFrame);
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        return container;
    }

    /**
     * Creates section for user inputs.
     * There are listeners for every input field
     *
     * @param rollOnce      The Roll Once button
     * @param rollThousands The Roll 100000 Times button
     * @return the container of input section
     */
    public VBox createInputSection(Button rollOnce, Button rollThousands) {
        // Text box to display any error
        Text error = new Text();
        error.setFill(Color.RED);

        // Header of section
        Text title = new Text("User Inputs");
        title.setFont(Font.font("Verdana", FontWeight.BOLD, 17));
        title.setFill(Color.GREEN);

        // The field for user to enter the number of dice
        InputField diceNumber = createInputField("Number of dice");

        // The container of all fields to input the sides of dice
        VBox diceSidesContainer = new VBox();
        diceSidesContainer.setSpacing(15);

        // When user enter a valid number of dice, add the input fields of
        // dice sides to container. The number of input fields added is the number
        // of dice the user has entered
        diceNumber.textField().textProperty().addListener((obs, oldInput, currentInput) -> {
            // Reset error text box and clear dice side input fields
            error.setText("");
            diceSidesContainer.getChildren().clear();

            // Push error when encountering these conditions
            if (!isInteger(currentInput)) error.setText("Please enter a positive integer");
            if (isInteger(currentInput) && parseInt(currentInput) >= 7)
                error.setText("Please enter a positive integer smaller than 7");

            // Push input fields according to the number of dice user has entered
            if (isInteger(currentInput) && parseInt(currentInput) < 7) {
                int[] diceSides = new int[parseInt(currentInput)];
                for (int i = 0; i < parseInt(currentInput); i++) {
                    // Since we will be using index in an event listener, we need to make it a constant
                    // to prevent any unexpected results
                    final int currentIndex = i;

                    // Create an input field with label for a die
                    InputField dieSides = createInputField("Sides of die " + (i + 1));

                    // When user has entered all fields with valid values, enable the buttons
                    // and initiate the dice collection
                    dieSides.textField().textProperty().addListener((sidesObs, oldSideInput, currentSideInput) -> {
                        error.setText(""); // Reset error box

                        boolean enableButtons = true;

                        // Reset the value in the current index, display error and disable buttons if user inputs invalid values
                        if (!isInteger(currentSideInput)) {
                            diceSides[currentIndex] = 0;
                            rollOnce.setDisable(true);
                            rollThousands.setDisable(true);
                            error.setText("Please enter a positive integer as \nthe sides of die number " + (currentIndex + 1));
                        }
                        if (isInteger(currentSideInput) && parseInt(currentSideInput) < 2) {
                            diceSides[currentIndex] = 0;
                            rollOnce.setDisable(true);
                            rollThousands.setDisable(true);
                            error.setText("Die number " + (currentIndex + 1) + " needs at least 2 sides");
                        }
                        if (isInteger(currentSideInput) && parseInt(currentSideInput) >= 10) {
                            diceSides[currentIndex] = 0;
                            rollOnce.setDisable(true);
                            rollThousands.setDisable(true);
                            error.setText("Die number " + (currentIndex + 1) + " should only have\nless than 10 sides");
                        }

                        // If user has entered all fields with valid values, enable buttons and initiate dice collection
                        if (isInteger(currentSideInput) && parseInt(currentSideInput) >= 2 && parseInt(currentSideInput) < 15) {
                            diceSides[currentIndex] = parseInt(currentSideInput);
                            for (int sides : diceSides)
                                if (sides <= 0) {
                                    enableButtons = false;
                                    break;
                                }
                            if (enableButtons) {
                                rollOnce.setDisable(false);
                                rollThousands.setDisable(false);
                                diceCollection = new DiceCollection(diceSides);
                                // If user has already clicked Roll 100000 Times button once,
                                // everytime user changes value in any field, this button will also be clicked
                                if (!isFirstThousandRoll) rollThousands.fire();
                            }
                        }
                    });
                    diceSidesContainer.getChildren().add(dieSides.container());
                }
            }
        });

        // A note for user
        Text note = new Text("Due to the limit in the size of the bar graph,\nthe number of dice should be\nsmaller than 7 and the sides of\neach die should be smaller than 10");

        // The container of this section
        VBox container = new VBox(title, diceNumber.container(), diceSidesContainer, note, error);
        container.setSpacing(15);
        container.setAlignment(Pos.TOP_CENTER);

        return container;
    }

    /**
     * Creates a field with a label for user input
     * This method uses a record declared at the end of this file
     *
     * @param label The label of the field
     * @return An object contains the container and the field element
     */
    public InputField createInputField(String label) {
        Label lb = new Label(label);
        TextField field = new TextField();
        // Use BorderPane instead of other APIs to position the label to the left and the field to the right
        BorderPane container = new BorderPane(null, null, field, null, lb);
        BorderPane.setMargin(lb, new Insets(0, 10, 0, 0));
        BorderPane.setAlignment(lb, Pos.CENTER);
        return new InputField(container, field);
    }

    /**
     * This is a utility method that identifies if a given string is an integer
     *
     * @param string The string to validate
     * @return true if string is integer or otherwise
     */
    public boolean isInteger(String string) {
        boolean result = true;
        for (String character : string.split(""))
            if (!isDigit(character)) {
                result = false;
                break;
            }
        return result;
    }

    /**
     * This is a utility method that identifies if a given character is a digit
     *
     * @param character The character to identify
     * @return true if the character is a digit or otherwise
     */
    public boolean isDigit(String character) {
        return Arrays.asList(new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9"}).contains(character);
    }

    /**
     * This is a utility method to parse a string into an integer
     * Just a wrapper method to cure laziness
     *
     * @param number the integer number in type String
     * @return the integer number in type int
     */
    public int parseInt(String number) {
        return Integer.parseInt(number);
    }
}

/**
 * IntelliJ's suggestion to substitute writing a class with only purpose of constructing (Shorter version of a full class)
 * This record is used to construct the return value of input field creator method in Main class
 *
 * @param container
 * @param textField
 */
record InputField(BorderPane container, TextField textField) {
}

/**
 * IntelliJ's suggestion to substitute writing a class with only purpose of constructing (Shorter version of a full class)
 * This record is used to construct the return value of button creator method in Main class
 *
 * @param container
 * @param rollOne
 * @param rollThousands
 */
record Buttons(HBox container, Button rollOne, Button rollThousands) {
}