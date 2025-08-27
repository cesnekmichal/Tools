package com.tool.retimebyname;

///*     */ package retimebyname;
///*     */ 
///*     */ import java.io.File;
///*     */ import java.io.FileOutputStream;
///*     */ import java.io.IOException;
///*     */ import java.io.InputStream;
///*     */ import java.text.ParseException;
///*     */ import java.text.SimpleDateFormat;
///*     */ import java.util.ArrayList;
///*     */ import java.util.Date;
///*     */ import java.util.Iterator;
///*     */ import javax.imageio.ImageIO;
///*     */ import javax.imageio.ImageReader;
///*     */ import javax.imageio.stream.ImageInputStream;
///*     */ 
///*     */ 
///*     */ 
///*     */ 
///*     */ 
///*     */ 
///*     */ 
///*     */ public class Main
///*     */ {
///*     */   public static void main(String[] args) {
///*  25 */     String patternSimple = "yyyy,MM,dd-HH,mm,ss";
///*  26 */     String patternExt = "yyyy,MM,dd-HH,mm,ss-SSS";
///*  27 */     SimpleDateFormat sdfSim = new SimpleDateFormat(patternSimple);
///*  28 */     SimpleDateFormat sdfExt = new SimpleDateFormat(patternExt);
///*  29 */     File currentDir = new File(".");
///*  30 */     File[] listFiles = currentDir.listFiles();
///*  31 */     PleaseWait dlg = new PleaseWait(Integer.valueOf(listFiles.length));
///*  32 */     dlg.setVisible(true);
///*  33 */     for (File file : listFiles) {
///*  34 */       dlg.addStep();
///*  35 */       if (file.isFile() && 
///*  36 */         file.getName().contains(".")) {
///*  37 */         String[] split = file.getName().split("\\.");
///*  38 */         String onlyName = split[0];
///*  39 */         Date date = null;
///*     */         try {
///*  41 */           date = sdfExt.parse(onlyName);
///*  42 */         } catch (ParseException ex) {
///*     */           try {
///*  44 */             date = sdfSim.parse(onlyName);
///*  45 */           } catch (Exception e) {
///*  46 */             e.printStackTrace();
///*     */           } 
///*     */         } 
///*  49 */         if (date != null) {
///*  50 */           renameExIfCreationTime(file, date);
///*  51 */           file.setLastModified(date.getTime());
///*     */         } 
///*     */       } 
///*     */     } 
///*     */     
///*  56 */     dlg.setVisible(false);
///*  57 */     System.exit(0);
///*     */   }
///*     */   
///*     */   private static void renameExIfCreationTime(File f, Date date) {
///*  61 */     if (date.equals(ExIfUtils.getDateTimeOriginal(f)))
///*     */       return;  try {
///*  63 */       File exiftoolFile = getExiftoolexe_File();
///*  64 */       String command = "\"" + exiftoolFile.getAbsolutePath() + "\" \"-AllDates=";
///*  65 */       command = command + (new SimpleDateFormat("yyyy:MM:dd HH:mm:ss")).format(date) + "\" ";
///*  66 */       command = command + "\"" + f.getCanonicalPath() + "\"";
///*  67 */       Tools.exec(command);
///*  68 */       File backupFile = new File(f.getParent(), f.getName() + "_original");
///*  69 */       backupFile.delete();
///*  70 */     } catch (IOException ex) {
///*  71 */       ex.printStackTrace();
///*     */     } 
///*     */   }
///*     */   
///*  75 */   private static File exIfToolFile = null;
///*     */   
///*     */   private static File getExiftoolexe_File() {
///*  78 */     if (exIfToolFile != null) return exIfToolFile; 
///*  79 */     InputStream is = Main.class.getResourceAsStream("exiftool.exe");
///*  80 */     System.out.println(is);
///*     */     try {
///*  82 */       File tmpFile = File.createTempFile("RetimeByName", "exiftool.exe");
///*  83 */       tmpFile.createNewFile();
///*  84 */       FileOutputStream fos = new FileOutputStream(tmpFile);
///*  85 */       Tools.readAndWrite(is, fos, 1024);
///*  86 */       is.close();
///*  87 */       fos.close();
///*  88 */       return tmpFile;
///*  89 */     } catch (IOException ex) {
///*  90 */       ex.printStackTrace();
///*     */       
///*  92 */       return null;
///*     */     } 
///*     */   }
///*     */   public static boolean isImage(File f) {
///*  96 */     String imageFormat = getImageFormat(f);
///*  97 */     return !imageFormat.isEmpty();
///*     */   }
///*     */   
///*     */   public static String getImageFormat(File f) {
///* 101 */     ArrayList<String> formats = new ArrayList<String>();
///*     */     try {
///* 103 */       ImageInputStream iis = ImageIO.createImageInputStream(f);
///* 104 */       Iterator<ImageReader> readers = ImageIO.getImageReaders(iis);
///* 105 */       while (readers.hasNext()) {
///* 106 */         ImageReader read = readers.next();
///* 107 */         formats.add(read.getFormatName());
///*     */       } 
///* 109 */       System.out.println();
///* 110 */       iis.close();
///* 111 */       if (!formats.isEmpty()) {
///* 112 */         return formats.get(0);
///*     */       }
///* 114 */       return "";
///*     */     }
///* 116 */     catch (IOException ex) {
///* 117 */       return "";
///*     */     } 
///*     */   }
///*     */ }
//
//
///* Location:              C:\CHAPPIE\Fotky\_aplikace\RetimeByName\RetimeByName.zip!\retimebyname\Main.class
// * Java compiler version: 5 (49.0)
// * JD-Core Version:       1.1.3
// */