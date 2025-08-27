package com.tool.utils.rescs;

import java.io.File;
import com.tool.utils.ZipUtil;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.tool.utils.ExecuteUtil;
import com.tool.utils.ResourceUtil;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author Michal
 */
public enum Resources {
    exiftool("exiftool.zip"),
    ffmpeg  ("""
             ffmpeg.001
             ffmpeg.002
             ffmpeg.003
             ffmpeg.004
             ffmpeg.005
             ffmpeg.crc""".split("\\R")),
    ;
    
    private Resources(String name) {
        this.names = new String[]{name};
    }
    
    private Resources(String... names) {
        this.names = names;
    }
    
    private String[] names;
    
    public byte[] getBytes(){
        return ResourceUtil.getBytes(names[0], Resources.class);
    }
    
    public File getTempSplitFile() throws IOException{
        String[] partFileNames = Arrays.copyOf(names, names.length - 1);
        String    crcFileName  = names[names.length-1];
        LinkedHashMap<String, String> crcMap = Stream.of(ResourceUtil.getText(crcFileName, Resources.class).split("\\R")).collect(
            Collectors.toMap((r)->r.split("=")[0], (r)->r.split("=")[1], (a,b)->a, LinkedHashMap::new));
        String fileName = crcMap.get("filename");
        String fileSize = crcMap.get("size");
        try(InputStream stream = ResourceUtil.getStream(partFileNames, Resources.class)){
            Path tempDir  = Files.createTempDirectory("unzipped-");
            Path tempFile = Paths.get(tempDir.toString(), fileName);
            Files.copy(stream, tempFile, StandardCopyOption.REPLACE_EXISTING);
            long fileSizeL = tempFile.toFile().length();
            if(!(fileSizeL+"").equals(fileSize)){
                System.err.println("Wrong file size of "+fileName+"! Expected:"+fileSize+", but is "+fileSizeL);
            }
            return tempFile.toFile();
        }
    }
    
    public File getTempFile() throws IOException{
        byte[] bytes = ResourceUtil.getBytes(names[0], Resources.class);
        Path tempDir  = Files.createTempDirectory("unzipped-");
        Path tempFile = Paths.get(tempDir.toString(), names[0]);
        Files.copy(new ByteArrayInputStream(bytes), tempFile, StandardCopyOption.REPLACE_EXISTING);
        return tempFile.toFile();
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
