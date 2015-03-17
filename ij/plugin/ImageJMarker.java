package ij.plugin;

import ij.ImagePlus;
import ij.io.Opener;
import ij.macro.Interpreter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.StringReader;

/**
 * Created by Alexandr Shirokov on 11/03/2015.
 * Defines a utility for performing marking the macro script
 */
public class ImageJMarker implements PlugIn
{
    //----------------------------------------------
    // Private Constants
    //----------------------------------------------
    private static final int INITIAL = 0;
    private static final int COMMENT_START = 1;
    private static final int COMMENT_ONE = 2;
    private static final int COMMENT_TWO = 3;
    private static final int COMMENT_TWO_END = 4;

    public void run(String arg) {

    }

    /**
     * Run the script for the given question
     * @param macro The script to run
     * @param sourceImagePath The source image for script
     * @return The image that was produced by the script
     * @throws Exception
     */
    public static ImagePlus runScript(String macro, String sourceImagePath) throws Exception
    {
        Opener opener = new Opener();
        ImagePlus sourceImage = opener.openImage(sourceImagePath);

        //Headless/GUI-less version
        Interpreter interpreter = new Interpreter();
        String macroScript = sanitize(macro);

        return interpreter.runBatchMacro(macroScript, sourceImage);
    }

    private static String sanitize(String macro)
    {
        String fileData = macro.replaceAll("print(.*);", ";");
        return fileData.replaceAll("Dialog.*;", ";");
    }

    /**
     * Perform the given image check
     * @param destinationImagePath The path to destination image
     * @param sourceImage The source image
     * @param maxMarks The maximum marks that this question is worth
     * @param level1Mark The mark given for a level 1 miss
     * @param level2Mark The mark given for a level 2 miss
     * @param level1Thresh The threshold for level 1
     * @param level2Thresh The threshold for level 2
     * @param xpad The padding in the x direction
     * @param ypad The padding in the y direction
     */
    public static void performImageCompare(String destinationImagePath, ImagePlus sourceImage, int maxMarks, int level1Mark, int level2Mark, int level1Thresh, int level2Thresh, int xpad, int ypad)
    {
        ImagePlus targetImage = new ImagePlus(destinationImagePath);
        int difference = getMaxDifference(sourceImage, targetImage, xpad, ypad);
        if (difference <= 1) {
            System.out.print(maxMarks);
        } else if (difference <= level1Thresh) {
            System.out.print(level1Mark);
            //String comment = String.format("Score %s marks if the maximum error is between 1 and %s.", level1Mark, level1Thresh);
        } else if (difference <= level2Thresh) {
            System.out.print(level2Mark);
            //String comment = String.format("Score %s marks if the maximum error is between %s and %s.", level2Mark, level1Thresh, level2Thresh);
        } else {
            System.out.print(String.format("Exceeded maximum allowed difference of +/- %s [actual difference found: %s].", level2Thresh, difference));
        }
    }

    /**
     * Perform the given image check
     * @param destinationImagePath The path to destination image
     * @param sourceImage The source image
     * @param maxMarks The maximum marks that this question is worth
     */
    public static void performAverageImageCompare(String destinationImagePath, ImagePlus sourceImage, int maxMarks)
    {
        ImagePlus targetImage = new ImagePlus(destinationImagePath);
        int difference = getAverageDifference(sourceImage, targetImage);
        if (difference <= 1) {
            System.out.print(maxMarks);
        } else if (difference <= 2) {
            System.out.print(maxMarks / 2);
            //mark.setComment("Half mark if the maximum error is between 1 and 2.");
        } else if (difference <= 32) {
            System.out.print(maxMarks / 5);
            //mark.setComment("A fifth mark if the maximum error is between 2 and 32.");
        } else {
            System.out.print(String.format("Exceeded maximum allowed difference of +/- 32 [actual difference found: %s].", difference));
        }
    }


    /**
     * Perform a comparison for binary images
     * @param destinationImagePath The path to destination image
     * @param sourceImage The source image
     * @param maxMarks The maximum marks for the question
     * @param xpad The x padding
     * @param ypad The y padding
     */
    public static void performBinaryImageCompare(String destinationImagePath, ImagePlus sourceImage, int maxMarks, int xpad, int ypad)
    {
        ImagePlus targetImage = new ImagePlus(destinationImagePath);
        int difference = getDifferenceCount(sourceImage, targetImage, xpad, ypad);
        if (difference <= 10) {
            System.out.print(maxMarks);
        } else if (difference <= 50) {
            System.out.print(maxMarks / 2);
            //mark.setComment("Half mark if the difference is between 10 and 50 mismatches.");
        } else {
            System.out.print(String.format("Exceeded maximum allowed difference of 50 mismatches [actual difference found: %s].", difference));
        }
    }

    /**
     * Allocates marks for border checking
     * @param destinationImagePath The path to destination image
     * @param sourceImage The source image that we are dealing with
     * @param maxMarks The maximum marks assigned to this question
     * @param xSize The size of the border in the x direction
     * @param ySize The size of the border in the y direction
     */
    public static void performBorderCheck(String destinationImagePath, ImagePlus sourceImage, int maxMarks, int xSize, int ySize)
    {
        ImagePlus targetImage = new ImagePlus(destinationImagePath);
        boolean sameBorders = doBordersMatch(sourceImage, targetImage, xSize, ySize);
        if (sameBorders) {
            System.out.print(maxMarks);
        } else {
            System.out.print("The border should not change, but changes were detected!");
        }
    }

    /**
     * Assign marks to the amount of comments found
     * @param macro The script to check
     * @param maxMarks The amount of marks assigned to this question
     */
    public static void performCommentCheck(String macro, int maxMarks)
    {
        int count = countComments(macro);
        if (count > 5) {
            System.out.print(maxMarks);
        } else {
            System.out.print("Not enough comments detected in the code!");
        }
    }


    //----------------------------------------------
    // Helper Methods
    //----------------------------------------------

    /**
     * Count the comments that are in the file
     * @param macro The script to count comments
     * @return The amount of comments that are in the file
     */
    //TODO find more efficient way to count comments
    private static int countComments(String macro)
    {
        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new StringReader(macro));
            int value = -1;
            int counter = 0;
            int state = INITIAL;
            while ((value = reader.read()) != -1)
            {
                switch (state)
                {
                    case INITIAL: state = processInitial(value, state); break;
                    case COMMENT_START: state = processCommentStart(value, counter); break;
                    case COMMENT_ONE: state = processCommentOne(value, state); break;
                    case COMMENT_TWO: state = processCommentTwo(value, state); break;
                    case COMMENT_TWO_END: state = processCommentEnd(value, state); break;
                }
            }
            return counter;
        } catch (Exception exception) {
            System.err.println("Error: " + exception.getMessage());
            return 0;
        } finally {
            try {reader.close();} catch (Exception exception) {}
        }
    }

    /**
     * Process the initial state
     * @param value The next value to be read in
     */
    private static int processInitial(int value, int state)
    {
        char character = (char) value;
        if (character == '/') return COMMENT_START;
        return state;
    }

    /**
     * Process the comment start state
     * @param value The next value to be read in
     */
    private static int processCommentStart(int value, int counter)
    {
        char character = (char) value;
        if (character == '/') {
            counter++;
            return COMMENT_ONE;
        } else if (character == '*') {
            counter++;
            return COMMENT_TWO;
        } else return INITIAL;
    }

    /**
     * Process the state of comment one
     * @param value The next value to be read in
     */
    private static int processCommentOne(int value, int state)
    {
        char character = (char) value;
        if (character == '\n') return INITIAL;
        return state;
    }

    /**
     * Process the state of comment two
     * @param value The next value to be read in
     */
    private static int processCommentTwo(int value, int state)
    {
        char character = (char) value;
        if (character == '*') return COMMENT_TWO_END;
        return state;
    }

    /**
     * Process the state of comment end
     * @param value The next value to be read in
     */
    private static int processCommentEnd(int value, int state)
    {
        char character = (char) value;
        if (character == '/') return INITIAL;
        else return COMMENT_TWO;
    }

    /**
     * Calculate the difference between the source image and the target image
     * @param sourceImage The source image that we are comparing
     * @param targetImage The target image that we are comparing
     * @param xpad The amount of the x axis to ignore.
     * @return The comparison
     */
    private static int getMaxDifference(ImagePlus sourceImage, ImagePlus targetImage, int xpad, int ypad)
    {
        if (sourceImage.getWidth() != targetImage.getWidth()) return 100;
        if (sourceImage.getHeight() != targetImage.getHeight()) return 100;

        int maxDifference = 0;

        for (int y=0; y<sourceImage.getWidth(); y++)
        {
            for (int x=0; x<sourceImage.getHeight(); x++)
            {
                if (IsBorder(sourceImage,x,y,xpad,ypad)) continue;
                int sourceIntensity = sourceImage.getPixel(x, y)[0];
                int targetIntensity = targetImage.getPixel(x, y)[0];
                int difference = Math.abs(targetIntensity - sourceIntensity);
                if (difference > maxDifference) maxDifference = difference;
            }
        }
        return maxDifference;
    }

    /**
     * Calculate the difference between the source image and the target image
     * @param sourceImage The source image that we are comparing
     * @param targetImage The target image that we are comparing
     * @return The comparison
     */
    private static int getAverageDifference(ImagePlus sourceImage, ImagePlus targetImage)
    {
        if (sourceImage.getWidth() != targetImage.getWidth()) return 100;
        if (sourceImage.getHeight() != targetImage.getHeight()) return 100;

        int aveDifference = 0, counter = 0;

        for (int y=0; y<sourceImage.getWidth(); y++)
        {
            for (int x=0; x<sourceImage.getHeight(); x++)
            {
                int sourceIntensity = sourceImage.getPixel(x, y)[0];
                int targetIntensity = targetImage.getPixel(x, y)[0];
                int difference = Math.abs(targetIntensity - sourceIntensity);
                aveDifference += difference; counter++;
            }
        }
        return aveDifference / counter;
    }

    /**
     * Perform a difference count on the images
     * @param sourceImage The source image
     * @param targetImage The target image
     * @param xpad The x border to ignore
     * @param ypad The y border to ignore
     * @return The result of the comparison
     */
    private static int getDifferenceCount(ImagePlus sourceImage, ImagePlus targetImage, int xpad, int ypad)
    {
        if (sourceImage.getWidth() != targetImage.getWidth()) return 100;
        if (sourceImage.getHeight() != targetImage.getHeight()) return 100;

        int counter = 0;

        for (int y=0; y<sourceImage.getWidth(); y++)
        {
            for (int x=0; x<sourceImage.getHeight(); x++)
            {
                if (IsBorder(sourceImage,x,y,xpad,ypad)) continue;
                int sourceIntensity = sourceImage.getPixel(x, y)[0];
                int targetIntensity = targetImage.getPixel(x, y)[0];
                if (sourceIntensity != targetIntensity) counter++;
            }
        }

        return counter;
    }

    /**
     * Test if the given borders match
     * @param sourceImage The source image that we are testing
     * @param targetImage The destination image that we are testing
     * @param xSize The size of the border in the x direction
     * @param ySize The size of the border in the y direction
     * @return True if the borders match
     */
    private static boolean doBordersMatch(ImagePlus sourceImage, ImagePlus targetImage, int xSize, int ySize)
    {
        if (sourceImage.getWidth() != targetImage.getWidth()) return false;
        if (sourceImage.getHeight() != targetImage.getHeight()) return false;
        for (int y=0; y<sourceImage.getWidth(); y++)
        {
            for (int x=0; x<sourceImage.getHeight(); x++)
            {
                if (IsBorder(sourceImage, x,y, xSize, ySize))
                {
                    int sourceIntensity = sourceImage.getPixel(x, y)[0];
                    int targetIntensity = targetImage.getPixel(x, y)[0];
                    if (sourceIntensity != targetIntensity) return false;
                }
            }
        }
        return true;
    }

    /**
     * Test to see if our position is on the boarder
     * @param sourceImage The image that we are testing
     * @param x The current x coordinate
     * @param y The current y coordinate
     * @param xSize The size of the x coordinate
     * @param ySize The size of the y coordinate
     * @return True if we are on the border, else false
     */
    private static boolean IsBorder(ImagePlus sourceImage, int x, int y, int xSize, int ySize)
    {
        if (x < xSize) return true;
        if (sourceImage.getWidth() - x <= xSize) return true;
        if (y < ySize) return true;
        if (sourceImage.getHeight() - y <= ySize) return true;
        return false;
    }
}
