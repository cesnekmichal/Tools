package com.tool.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Česnek Michal, UNIDATAZ s.r.o.
 */
public class ExifToolsUtilTest {
    
    public ExifToolsUtilTest() {
    }

    @org.junit.jupiter.api.BeforeAll
    public static void setUpClass() throws Exception {
    }

    @org.junit.jupiter.api.AfterAll
    public static void tearDownClass() throws Exception {
    }

    @org.junit.jupiter.api.BeforeEach
    public void setUp() throws Exception {
    }

    @org.junit.jupiter.api.AfterEach
    public void tearDown() throws Exception {
    }

    @org.junit.jupiter.api.Test
    public void test_sample_media() throws IOException {
        //https://file-examples.com/
        for (File sample_file : new File("sample_media").listFiles()) {
            ExifToolsUtil.FileType fileType = ExifToolsUtil.FileType.getFileType(sample_file);
            assertNotEquals(fileType.UNKNOWN, fileType, "UNKNOWN FileType:\n"+sample_file);
            
            Date date = ExifToolsUtil.getExIfDateTime(sample_file);
            assertNotNull(date, "Date not found:\n"+sample_file);
            
            File sample_copy = new File("run/temp/copy."+FileUtil.getExtension(sample_file.getName()));
            sample_copy.mkdirs();
            Files.copy(sample_file.toPath(), sample_copy.toPath(), StandardCopyOption.REPLACE_EXISTING);
            Date date_ = ExifToolsUtil.setExIfDateTime(sample_copy, new Date());
            assertNotNull(date_, "Unable to setExIfDateTime:\n"+sample_file);
        }
    }
    
}
