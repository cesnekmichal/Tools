package com.tool.utils;

import com.tool.utils.rescs.Resources;
import jakarta.annotation.Nonnull;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;


/**
 *
 * @author Michal
 */
public class ExifToolsUtil {
    
    private static String json_out = "-j";
    
    /** Vrátí cestu k souboru: exiftool.exe */
    public synchronized static File getExIfToolExe(){
        if(ExIfToolExe!=null) return ExIfToolExe;
        try {
            Path tmpDir = ZipUtil.unzipToTempDir(Resources.exiftool.getBytes());
            ExIfToolExe = new File(tmpDir.toFile(),exiftool_exe);
            return ExIfToolExe;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private static File ExIfToolExe = null;
    private static final String exiftool_exe = "exiftool.exe";
    
    /** Vrátí cestu k souboru: ffmpeg.exe */
    public synchronized static File getFFmpegExe(){
        if(FFmpegExe!=null) return FFmpegExe;
        try {
            FFmpegExe = Resources.ffmpeg.getTempSplitFile();
            return FFmpegExe;
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    private static File FFmpegExe = null;
    
    public enum FileType{
        /** File Type : JPEG, MIME Type : image/jpeg. */
        JPG("JPEG","image/jpeg"),
        /** FileType : PNG, MIMEType : image/png. */
        PNG("PNG","image/png"),
        /** FileType : MP4, MIMEType : video/mp4, Major Brand : MP4 v2 [ISO 14496-14]. */
        MP4("MP4","video/mp4"),
        /** FileType : MOV, MIMEType : video/quicktime, Major Brand : Apple QuickTime (.MOV/QT). */
        MOV("MOV","video/quicktime"),
        /** FileType : MP3, MIMEType : audio/mpeg. */
        MP3("MP3","audio/mpeg"),
        
        /** File Type : Win64 EXE, MIME Type : application/octet-stream. */
        EXE64("Win64 EXE","application/octet-stream"),
        /** File Type : Win32 EXE, MIME Type : application/octet-stream. */
        EXE32("Win32 EXE","application/octet-stream"),
        /** File Type : ZIP, MIME Type : application/zip. */
        ZIP("ZIP","application/zip"),
        /** UNKNOWN. */
        UNKNOWN("","");
        ;
        public final String fileType;
        public final String MIMEType;
        private FileType(String fileType, String MIMEType) {
            this.fileType = fileType;
            this.MIMEType = MIMEType;
        }
        public boolean isMedia(){
            return switch (this) {
                case JPG, PNG, MP4, MOV, MP3 -> true;
                default -> false;
            };
        }
        public static @Nonnull FileType getFileType(File mediaFile){
            if(mediaFile.isDirectory()) return UNKNOWN;
            /*
                [{
                  "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample.png",
                  "FileType": "PNG",
                  "MIMEType": "image/png"
                }]            
            */
            
            String cmd_out = ExecuteUtil.exec(getExIfToolExe().getAbsolutePath(),"-FileType","-MIMEType","-MajorBrand",json_out,mediaFile.getAbsolutePath());
            JSONArray array = new JSONArray(cmd_out);
            JSONObject obj = array.getJSONObject(0);
            String fileType = obj.getString("FileType");
            for (FileType value : values()) {
                if(value.fileType.equals(fileType)) return value;
            }
            System.err.println("Unknown FileType:\n"+cmd_out);
            return UNKNOWN;
        };
    }
    
    public static Date setExIfDateTime(File mediaFile, Date date){
        FileType fileType = FileType.getFileType(mediaFile);
        if(!fileType.isMedia()) return null;
        switch (fileType) {
            case JPG: return setExIfDateTime_JPG(mediaFile,date);
            case PNG: return setExIfDateTime_PNG(mediaFile,date);
            case MP4: 
            case MOV: return setExIfDateTime_MP4_MOV(mediaFile,date);
            case MP3: return setFFmpegDateTime_MP3(mediaFile,date);
        }
        return null;
    }
    
    public static Date getExIfDateTime(File mediaFile){
        FileType fileType = FileType.getFileType(mediaFile);
        if(!fileType.isMedia()) return null;
        switch (fileType) {
            case JPG: return getExIfDateTime_JPG(mediaFile);
            case PNG: return getExIfDateTime_PNG(mediaFile);
            case MP4: 
            case MOV: return getExIfDateTime_MP4_MOV(mediaFile);
            case MP3: return getExIfDateTime_MP3(mediaFile);
        }
        return null;
    }
    
    public static Date getExIfDateTime_JPG(File mediaFile){
        //<editor-fold defaultstate="collapsed" desc="Samples">
        /*
        [{
            "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample2.jpg",
            "FileModifyDate"  : "2015:08:16 07:47:00+02:00",              --Nastavujeme
            "FileAccessDate"  : "2025:08:26 22:11:21+02:00",              --Nastavujeme
            "FileCreateDate"  : "2025:08:26 22:11:21+02:00",              --Nastavujeme
            "ModifyDate"      : "2015:08:16 07:47:00",        --Hledáme   --Nastavujeme
            "DateTimeOriginal": "2015:08:16 07:47:00",        --Hledáme   --Nastavujeme
            "CreateDate"      : "2015:08:16 07:47:00"         --Hledáme   --Nastavujeme
        }]
        [{
            "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample.jpg",
            "FileModifyDate": "2025:08:22 15:16:39+02:00",                --Nastavujeme
            "FileAccessDate": "2025:08:22 15:21:01+02:00",                --Nastavujeme
            "FileCreateDate": "2025:08:22 15:21:01+02:00",                --Nastavujeme
            "ModifyDate": "2025:08:22 15:16:38",              --Hledáme   --Nastavujeme
            "DateTimeOriginal": "2025:08:22 15:16:38",        --Hledáme   --Nastavujeme
            "CreateDate": "2025:08:22 15:16:38",              --Hledáme   --Nastavujeme
            "OffsetTime": "+02:00",                                       --Mažeme
            "OffsetTimeOriginal": "+02:00",                               --Mažeme
            "SubSecTime": 157,                                            --Mažeme
            "SubSecTimeOriginal": 157,                                    --Mažeme
            "SubSecTimeDigitized": 157,                                   --Mažeme
            "TimeStamp": "2025:08:22 15:16:38.157+02:00",                 --Mažeme !!????
            "SubSecCreateDate": "2025:08:22 15:16:38.157",                --Mažeme
            "SubSecDateTimeOriginal": "2025:08:22 15:16:38.157+02:00",    --Mažeme
            "SubSecModifyDate": "2025:08:22 15:16:38.157+02:00"           --Mažeme
        }]
        */
        //</editor-fold>
        String std_out = ExecuteUtil.exec(getExIfToolExe().getAbsolutePath(),"-time:all",json_out,mediaFile.getAbsolutePath());
        LinkedHashMap<String, String> map = getJSONValues(std_out, "DateTimeOriginal","CreateDate","ModifyDate","FileModifyDate");
        Date minDate = map.values().stream().map(value -> parseDate(value)).filter(Objects::nonNull).min((Date o1, Date o2) -> o1.compareTo(o2)).orElse(null);
        return minDate;
    }
    public static Date setExIfDateTime_JPG(File mediaFile, Date date){
        if(date==null) return null;
        String dateValueMsUTC = new SimpleDateFormat(dateFormatMsUTC).format(date);
        String dateValueUTC   = new SimpleDateFormat(dateFormatUTC).format(date);
        String dateValue      = new SimpleDateFormat(dateFormat)   .format(date);
        List<String> commands = new ArrayList<>();
        commands.add(getExIfToolExe().getAbsolutePath());
        commands.add("-overwrite_original");
        commands.add("-FileModifyDate="+dateValueUTC);
      //commands.add("-FileAccessDate="+dateValueUTC);//Warning: Sorry, FileAccessDate is not writable
        commands.add("-FileCreateDate="+dateValueUTC);
        commands.add("-CreateDate="+dateValue);
        commands.add("-ModifyDate="+dateValue);
        commands.add("-DateTimeOriginal="+dateValueUTC);
        commands.add("-OffsetTime=");
        commands.add("-OffsetTimeOriginal=");
        commands.add("-SubSecTime=");
        commands.add("-SubSecTimeOriginal=");
        commands.add("-SubSecTimeDigitized=");
        commands.add("-TimeStamp=");
      //commands.add("-TimeStamp="+date.getTime()/1000);
      //commands.add("-TrackCreateDate="+dateValueUTC);
      //commands.add("-TrackModifyDate="+dateValueUTC);
      //commands.add("-MediaCreateDate="+dateValueUTC);
      //commands.add("-MediaModifyDate="+dateValueUTC);
        commands.add(mediaFile.getAbsolutePath());
        String out = ExecuteUtil.exec(commands.toArray(String[]::new));
        if(out!=null && out.contains("Warning")){
            System.err.println(mediaFile+"\n"+out);
        }
        return out!=null && out.contains("1 image files updated") ? date : null;
    }
    
    public static Date getExIfDateTime_PNG(File mediaFile){
        //<editor-fold defaultstate="collapsed" desc="Samples">
        /*
            [{
              "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample.png",
              "FileModifyDate": "2024:02:14 09:51:09+01:00",                --Nastavujeme
              "FileAccessDate": "2025:08:26 21:40:53+02:00",                --Nastavujeme
              "FileCreateDate": "2025:08:26 21:40:53+02:00",                --Nastavujeme
              "ModifyDate": "2008:02:01 22:15:55"               --Hledáme   --Nastavujeme
            }]    
        */
        //</editor-fold>
        String std_out = ExecuteUtil.exec(getExIfToolExe().getAbsolutePath(),"-time:all",json_out,mediaFile.getAbsolutePath());
        Date ModifyDate = parseDate(getJSONValue(std_out, "ModifyDate"));
        return ModifyDate;
    }
    public static Date setExIfDateTime_PNG(File mediaFile, Date date){
        if(date==null) return null;
        String dateValueUTC = new SimpleDateFormat(dateFormatUTC).format(date);
        String dateValue    = new SimpleDateFormat(dateFormat)   .format(date);
        List<String> commands = new ArrayList<>();
        commands.add(getExIfToolExe().getAbsolutePath());
        commands.add("-overwrite_original");
        commands.add("-FileModifyDate="+dateValueUTC);
      //commands.add("-FileAccessDate="+dateValueUTC);//Warning: Sorry, FileAccessDate is not writable
        commands.add("-FileCreateDate="+dateValueUTC);
        commands.add("-CreateDate="+dateValue);
        commands.add("-ModifyDate="+dateValue);
        commands.add(mediaFile.getAbsolutePath());
        String out = ExecuteUtil.exec(commands.toArray(String[]::new));
        if(out!=null && out.contains("Warning")){
            System.err.println(mediaFile+"\n"+out);
        }
        return out!=null && out.contains("1 image files updated") ? date : null;
    }
    
    public static Date getExIfDateTime_MP4_MOV(File mediaFile){
        //<editor-fold defaultstate="collapsed" desc="Samples">
        /*
            [{
              "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample.mp4",
              "FileModifyDate" : "2025:08:22 15:16:54+02:00",    --Hledáme   --Nastavujeme
              "FileAccessDate" : "2025:08:22 15:21:02+02:00",                --Nastavujeme
              "FileCreateDate" : "2025:08:22 15:21:01+02:00",    --Hledáme   --Nastavujeme
              "CreateDate"     : "2025:08:22 13:16:54",          --Hledáme   --Nastavujeme
              "ModifyDate"     : "2025:08:22 13:16:54",          --Hledáme   --Nastavujeme
              "TrackCreateDate": "2025:08:22 13:16:54",                      --Nastavujeme
              "TrackModifyDate": "2025:08:22 13:16:54",                      --Nastavujeme
              "MediaCreateDate": "2025:08:22 13:16:54",                      --Nastavujeme
              "MediaModifyDate": "2025:08:22 13:16:54"                       --Nastavujeme
            }]    
            [{
              "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/video.mp4",
              "FileModifyDate" : "2025:08:16 11:26:08+02:00",               --Nastavujeme
              "FileAccessDate" : "2025:08:16 11:26:08+02:00",               --Nastavujeme
              "FileCreateDate" : "2025:08:16 11:26:08+02:00",               --Nastavujeme
              "TrackCreateDate": "2025:08:16 09:26:08",                     --Nastavujeme
              "TrackModifyDate": "2025:08:16 09:26:08",                     --Nastavujeme
              "MediaCreateDate": "2025:08:16 09:26:08",         --Hledáme   --Nastavujeme
              "MediaModifyDate": "2025:08:16 09:26:08",         --Hledáme   --Nastavujeme
              "CreateDate"     : "2025:08:16 11:26:08",         --Hledáme   --Nastavujeme
              "ModifyDate"     : "2025:08:16 11:26:08"          --Hledáme   --Nastavujeme
            }]    
            [{
              "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample.mov",
              "FileModifyDate" : "2025:08:26 23:29:13+02:00",   --Hledáme   --Nastavujeme
              "FileAccessDate" : "2025:08:26 23:29:21+02:00",               --Nastavujeme
              "FileCreateDate" : "2025:08:26 23:29:21+02:00",   --Hledáme   --Nastavujeme
              "CreateDate"     : "0000:00:00 00:00:00",                     --Nastavujeme
              "ModifyDate"     : "0000:00:00 00:00:00",                     --Nastavujeme
              "TrackCreateDate": "0000:00:00 00:00:00",                     --Nastavujeme
              "TrackModifyDate": "0000:00:00 00:00:00",                     --Nastavujeme
              "MediaCreateDate": "0000:00:00 00:00:00",                     --Nastavujeme
              "MediaModifyDate": "0000:00:00 00:00:00"                      --Nastavujeme
            }]        
        */
        //</editor-fold>
        String std_out = ExecuteUtil.exec(getExIfToolExe().getAbsolutePath(),"-api","QuickTimeUTC","-time:all",json_out,mediaFile.getAbsolutePath());
        LinkedHashMap<String, String> map = getJSONValues(std_out, "DateTimeOriginal","MediaCreateDate","CreateDate","MediaModifyDate","ModifyDate","FileCreateDate","FileModifyDate");
        Date minDate = map.values().stream().map(value -> parseDate(value)).filter(Objects::nonNull).min((Date o1, Date o2) -> o1.compareTo(o2)).orElse(null);
        return minDate;
    }
    public static Date setExIfDateTime_MP4_MOV(File mediaFile, Date date){
        if(date==null) return null;
        String dateValueMs  = new SimpleDateFormat(dateFormatMs).format(date);
        String dateValueUTC = new SimpleDateFormat(dateFormatUTC).format(date);
        String dateValue    = new SimpleDateFormat(dateFormat)   .format(date);
        List<String> commands = new ArrayList<>();
        commands.add(getExIfToolExe().getAbsolutePath());
        commands.add("-overwrite_original");
        commands.add("-api");
        commands.add("QuickTimeUTC");
        commands.add("-FileModifyDate="+dateValueUTC);
      //commands.add("-FileAccessDate="+dateValueUTC);//Warning: Sorry, FileAccessDate is not writable
        commands.add("-FileCreateDate="+dateValueUTC);
        commands.add("-DateTimeOriginal="+dateValueUTC);
        commands.add("-TimeZone=");
      //commands.add("-TimeZoneCity=");//Warning: Can't delete Permanent tag Canon:TimeZoneCity
      //commands.add("-DaylightSavings=");//Warning: Can't delete Permanent tag Nikon:DaylightSavings
        commands.add("-CreateDate="+dateValue);
        commands.add("-ModifyDate="+dateValue);
        commands.add("-TrackCreateDate="+dateValueUTC);
        commands.add("-TrackModifyDate="+dateValueUTC);
        commands.add("-MediaCreateDate="+dateValueUTC);
        commands.add("-MediaModifyDate="+dateValueUTC);
        commands.add("-SubSecCreateDate="+dateValueMs);
        commands.add("-SubSecDateTimeOriginal="+dateValueMs);
        commands.add("-SubSecModifyDate="+dateValueMs);
        commands.add("-SubSecTime=");
        commands.add("-SubSecTimeOriginal=");
        commands.add("-SubSecTimeDigitized=");
        commands.add(mediaFile.getAbsolutePath());
        String out = ExecuteUtil.exec(commands.toArray(String[]::new));
        if(out!=null && out.contains("Warning")){
            System.err.println(mediaFile+"\n"+out);
        }
        return out!=null && out.contains("1 image files updated") ? date : null;
    }
    
    public static Date getExIfDateTime_MP3(File mediaFile){
        //<editor-fold defaultstate="collapsed" desc="Samples">
        /*
            [{
              "SourceFile": "C:/Users/Michal/Documents/NetBeansProjects/Tool.NBp/run/./copy",
              "FileModifyDate"  : "2025:08:27 08:26:51+02:00",
              "FileAccessDate"  : "2025:08:27 08:27:49+02:00",
              "FileCreateDate"  : "2025:08:27 08:27:49+02:00",
              "RecordingTime"   : "2015:11:27 11:50:38",
              "Date"            : "2015-11-27 11:50:38",
              "DateTimeOriginal": "2015:11:27 11:50:38"
            }] 
        */
        //</editor-fold>
        String std_out = ExecuteUtil.exec(getExIfToolExe().getAbsolutePath(),"-time:all",json_out,mediaFile.getAbsolutePath());
        LinkedHashMap<String, String> map = getJSONValues(std_out, "DateTimeOriginal","RecordingTime","FileModifyDate","MediaModifyDate","FileCreateDate","Date");
        Date minDate = map.values().stream().map(value -> parseDate(value)).filter(Objects::nonNull).min((Date o1, Date o2) -> o1.compareTo(o2)).orElse(null);
        return minDate;
    }
    
    public static Date setFFmpegDateTime_MP3(File mediaFile, Date date){
        if(date==null) return null;
        //ffmpeg -i pisnicka.mp3 -metadata "date=2025-08-27T10:00:00" -metadata "TDAT=2025-08-27T10:00:00" -codec copy pisnicka.tmp.mp3 && move /Y pisnicka.tmp.mp3 pisnicka.mp3
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String dateT = formatter.format(date.toInstant().atZone(ZoneId.systemDefault()));
        List<String> commands = new ArrayList<>();
        commands.add(getFFmpegExe().getAbsolutePath());
        commands.add("-i");
        commands.add(mediaFile.getAbsolutePath());
        commands.add("-metadata");
        commands.add("date="+dateT);
        commands.add("-metadata");
        commands.add("TDAT="+dateT);
        commands.add("-codec");
        commands.add("copy");
        String tempFile = FileUtil.changeExtension(mediaFile.getAbsolutePath(), "tmp.mp3");
        new File(tempFile).deleteOnExit();
        commands.add(tempFile);
//        commands.add("&&");
//        commands.add("move");
//        commands.add("/Y");
//        commands.add(FileUtil.changeExtension(mediaFile.getAbsolutePath(), "tmp"));
//        commands.add(mediaFile.getAbsolutePath());
        String out = ExecuteUtil.exec(commands.toArray(String[]::new));
        if(out!=null && out.contains("Error")){
            System.err.println(mediaFile+"\n"+out);
        }
        boolean success = out!=null && out.contains("Output #0, mp3, to '"+tempFile+"':");
        if(success){
            try {
                Files.move(new File(tempFile).toPath(), mediaFile.toPath(), StandardCopyOption.ATOMIC_MOVE);
            } catch (IOException ex) {
                ex.printStackTrace();
                success = false;
            }
        }
        return success ? date : null;
    }
    @Deprecated
    /** exiftools.exe - Writing of MP3 files is not yet supported  */
    public static Date setExIfDateTime_MP3(File mediaFile, Date date){
        if(date==null) return null;
        String dateValueUTC = new SimpleDateFormat(dateFormatUTC).format(date);
        String dateValue    = new SimpleDateFormat(dateFormat)   .format(date);
        List<String> commands = new ArrayList<>();
        commands.add(getExIfToolExe().getAbsolutePath());
        commands.add("-overwrite_original");
        commands.add("-FileModifyDate="+dateValueUTC);
      //commands.add("-FileAccessDate="+dateValueUTC);//Warning: Sorry, FileAccessDate is not writable
        commands.add("-FileCreateDate="+dateValueUTC);
      //commands.add("-RecordingTime="+dateValue);//Warning: Sorry, RecordingTime is not writable
        commands.add("-Date="+dateValue);
        commands.add("-DateTimeOriginal="+dateValue);
        commands.add(mediaFile.getAbsolutePath());
        String out = ExecuteUtil.exec(commands.toArray(String[]::new));
        if(out!=null && out.contains("Warning")){
            System.err.println(mediaFile+"\n"+out);
        }
        return out!=null && out.contains("1 image files updated") ? date : null;
    }
    
    private static final String dateFormatMsUTC = "yyyy:MM:dd HH:mm:ss.SSSXXX";
    private static final String dateFormatMs    = "yyyy:MM:dd HH:mm:ss.SS";
    private static final String dateFormatUTC   = "yyyy:MM:dd HH:mm:ssXXX";
    private static final String dateFormat      = "yyyy:MM:dd HH:mm:ss";
    
    public static String detectFormat(String input) {
        if (input.matches("\\d{4}:\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\+\\d{2}:\\d{2}")) {
            return "yyyy:MM:dd HH:mm:ss.SSSXXX"; // s milisekundami a časovou zónou
        } else 
        if (input.matches("\\d{4}:\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}\\+\\d{2}:\\d{2}")) {
            return "yyyy:MM:dd HH:mm:ssXXX"; // bez milisekund, s časovou zónou
        } else 
        if(input.matches("\\d{4}:\\d{2}:\\d{2} \\d{2}:\\d{2}:\\d{2}")) {
            return "yyyy:MM:dd HH:mm:ss"; // bez časové zóny
        } else {
            return null; // neznámý formát
        }
    }    
    
    public static Date parseDate(String value){
        try {
            if(value==null || value.isBlank() || value.equals("0000:00:00 00:00:00")) return null;
            String format = detectFormat(value);
            if(format==null) return null;
            return new SimpleDateFormat(format).parse(value);
        } catch (ParseException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    private static LinkedHashMap<String,String> getJSONValues(String exiftool_out_json, String... keys){
        /*
        [{
          "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample.png",
          "FileType": "PNG",
          "MIMEType": "image/png"
        }]     
         */
        JSONArray array = new JSONArray(exiftool_out_json);
        JSONObject obj = array.getJSONObject(0);
        LinkedHashMap<String,String> map = new LinkedHashMap<>();
        for (String key : keys) {
            if(obj.has(key)){
                map.put(key, obj.get(key).toString());
            }
        }
        return map;
    }
    
    private static String getJSONValue(String exiftool_out_json, String key){
        /*
        [{
          "SourceFile": "c:/Users/Michal/Downloads/exiftool-13.34_64/sample.png",
          "FileType": "PNG",
          "MIMEType": "image/png"
        }]     
         */
        JSONArray array = new JSONArray(exiftool_out_json);
        JSONObject obj = array.getJSONObject(0);
        if(obj.has(key)){
            return obj.getString(key);
        } else {
            return null;
        }
    }
        
    public static void main(String[] args) throws IOException {
        //exiftool.exe -time:all "fotka.jpg"
        File file = new File("sample_media/sample.mp3");
        File copy = new File("copy."+FileUtil.getExtension(file.getPath()));
        
        String out = ExecuteUtil.exec(getExIfToolExe().getAbsolutePath(),"-api","QuickTimeUTC","-time:all","-j",file.getAbsolutePath());
        System.out.println(out);

        //2025,08,16-14,21,28.JPG
        
//        FileType fileType = FileType.getFileType(file.getAbsoluteFile());
//        System.out.println(fileType);
//
//        Date date = getExIfDateTime(file);
//        System.out.println(date);
        
        Files.copy(file.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);

        setExIfDateTime(copy, new Date());
        out = ExecuteUtil.exec(getExIfToolExe().getAbsolutePath(),"-api","QuickTimeUTC","-time:all","-j",copy.getAbsolutePath());
        System.out.println(out);
    }

    
}
