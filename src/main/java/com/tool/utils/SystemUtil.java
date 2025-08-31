package com.tool.utils;

import java.time.Duration;

/**
 *
 * @author Michal
 */
public class SystemUtil {
    
    private void sleep(Duration d){
        try {
            Thread.sleep(d);
        } catch (InterruptedException ex) {}
    }
    
}
