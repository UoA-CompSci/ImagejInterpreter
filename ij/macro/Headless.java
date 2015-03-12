/*
 * This class provides methods which are used in headless version.
 */
package ij.macro;

import static ij.macro.MacroConstants.DIALOG;
import static ij.macro.MacroConstants.GET_PIXEL;
import static ij.macro.MacroConstants.PRINT;
import static ij.macro.MacroConstants.PUT_PIXEL;
import static ij.macro.MacroConstants.SET_PIXEL;

/**
 *
 * @author hnad002
 */
public class Headless {
        // returns true, if the predefined function is allowed to be called in batch mode.
        static boolean checkPredefined(int type) {
            switch(type) {
                case GET_PIXEL:
                case SET_PIXEL:
                case PUT_PIXEL:
                case PRINT:
                    return true;
                default:
                    return false;
            }
        }
        
        // returns true, if getString type function is allowed to be called in batch mode.
        static boolean checkGetString(int type) {
            switch(type) {
                case DIALOG:
                    return false;
                default:
                    return true;
            }            
        }      
        
        // generates exception for headless version related errors.
        static void error(String msg) {
           throw new RuntimeException("headless version error: " + msg);
        }

}
