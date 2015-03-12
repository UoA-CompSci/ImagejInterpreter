/*
 * This class provides methods which are used in headless version.
 */
package ij.macro;

import static ij.macro.MacroConstants.DIALOG;
import static ij.macro.MacroConstants.SET_PIXEL;
import static ij.macro.MacroConstants.PUT_PIXEL;
import static ij.macro.MacroConstants.PRINT;

/**
 *
 * @author hnad002
 */
public class Headless {
        static final int predefinedWhiteList[] = new int [] {SET_PIXEL, PUT_PIXEL, PRINT};
        static final int getStringBlackList[] = new int [] {DIALOG};
        
        static private boolean isInList(int[] list, int item) {
            for (int e: list) {
                if (e == item)
                    return true;
            }
            return false;
        }
               
        static boolean checkPredefined(int type) {
            return isInList(predefinedWhiteList, type);
        }
        
        // returns true, if getString type function is allowed to be called in batch mode.
        static boolean checkGetString(int type) {
            return !isInList(getStringBlackList, type);
        }      
        
        // generates exception for headless version related errors.
        static void error(String msg) {
           throw new RuntimeException("headless version error: " + msg);
        }

}
