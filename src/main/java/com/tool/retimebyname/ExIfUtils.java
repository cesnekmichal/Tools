package com.tool.retimebyname;

///*    */ package retimebyname;
///*    */ 
///*    */ import com.drew.imaging.ImageMetadataReader;
///*    */ import com.drew.metadata.Directory;
///*    */ import com.drew.metadata.Metadata;
///*    */ import com.drew.metadata.Tag;
///*    */ import java.io.File;
///*    */ import java.text.SimpleDateFormat;
///*    */ import java.util.Date;
///*    */ 
///*    */ 
///*    */ 
///*    */ 
///*    */ public class ExIfUtils
///*    */ {
///*    */   public static Date getDateTimeOriginal(File mediaFile) {
///*    */     try {
///* 18 */       Metadata metadata = ImageMetadataReader.readMetadata(mediaFile);
///* 19 */       for (Directory directory : metadata.getDirectories()) {
///* 20 */         for (Tag tag : directory.getTags()) {
///* 21 */           if (tag.getTagType() == 36867) {
///*    */             
///* 23 */             String dateTimeOriginalAsText = directory.getString(tag.getTagType());
///* 24 */             if (dateTimeOriginalAsText.equals("0000:00:00 00:00:00")) return null; 
///* 25 */             SimpleDateFormat sdf = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
///* 26 */             Date dateTimeOriginal = sdf.parse(dateTimeOriginalAsText);
///* 27 */             if (dateTimeOriginal != null) {
///* 28 */               return dateTimeOriginal;
///*    */             }
///*    */           } 
///*    */         } 
///*    */       } 
///* 33 */     } catch (Exception ex) {}
///*    */ 
///*    */     
///* 36 */     return null;
///*    */   }
///*    */ }
//
//
///* Location:              C:\CHAPPIE\Fotky\_aplikace\RetimeByName\RetimeByName.zip!\retimebyname\ExIfUtils.class
// * Java compiler version: 5 (49.0)
// * JD-Core Version:       1.1.3
// */