package com.tool.utils;

import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Česnek Michal, UNIDATAZ s.r.o.
 */
public class FileUtil {
    
    public static String getExtension(String fileName){
        return FilenameUtils.getExtension(fileName);
    }
    
    public static String removeExtension(String fileName){
        return FilenameUtils.removeExtension(fileName);
    }
    
    public static String changeExtension(String fileName,String newExtension){
        if(newExtension==null || newExtension.isBlank()) return fileName;
        return FilenameUtils.removeExtension(fileName)+"."+newExtension;
    }
    
    public static void main(String[] args) {
        
    }
    
}
