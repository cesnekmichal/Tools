package com.tool.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
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
    
    public static boolean rename(File from, File to, CopyOption... options){
        try {
            if(from.equals(to)) return true;
            Files.move(from.toPath(), to.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException ex) {
            ex.printStackTrace();
            return false;
        }
    }
    
    /** Vrátí unikátní neexistující 'File'. Přidává posfix: '(n)' */
    public static File getUnique(File parent, String child){
        File f = new File(parent, child);
        int i = 2;
        while(f.exists()){
            String filePath = removeExtension(new File(parent, child).getAbsolutePath());
            String fileExtension = getExtension(child);
            f = new File(filePath+"("+(i++)+")."+fileExtension);
        }
        return f;
    }
    
    public static void main(String[] args) {
        
    }
    
}
