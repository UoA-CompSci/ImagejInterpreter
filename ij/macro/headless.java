/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
public class headless {
        // returns true, if the called function is allowed to be called in batch mode.
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
        
        static boolean checkGetString(int type) {
            switch(type) {
                case DIALOG:
                    return false;
                default:
                    return true;
            }            
        }      
        
        static void error(String msg) {
           throw new RuntimeException("headless version error: " + msg);
        }

}
