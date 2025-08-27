package com.tool.utils.rescs;

import java.io.File;
import com.tool.utils.ZipUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.tool.utils.ExecuteUtil;
import com.tool.utils.ResourceUtil;

/**
 *
 * @author Michal
 */
public enum Resources {
    exiftool("exiftool.zip"),
    ;
    private Resources(String name) {
        this.name = name;
    }
    String name;
    
    public byte[] getBytes(){
        return ResourceUtil.getBytes(name, Resources.class);
    }
    
    public static void main(String[] args) throws IOException {
        byte[] bytes = exiftool.getBytes();
        System.out.println(bytes);
        
        Path path = ZipUtil.unzipToTempDir(bytes);
        System.out.println(path);
        ExecuteUtil.openFileLocation(new File(path.toFile(), "exiftool.exe"));
        
//        byte[] bytes = ResourceUtil.getBytes(exiftool.name, Resources.class);
//        System.out.println(bytes);
        
    }
    
}
