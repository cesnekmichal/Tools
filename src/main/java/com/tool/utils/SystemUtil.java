package com.tool.utils;

import java.time.Duration;

/**
 *
 * @author Michal
 */
/** 
 * Utility třída pro práci se systémovými operacemi, poskytuje helper metody pro práci s časem a vlákny.
 */
public class SystemUtil {
    
    /** 
     * Přeruší provádění vlákna na určitou dobu.
     * 
     * @param d doba, po kterou se má vlákno uspat
     */
    private void sleep(Duration d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
    
}
