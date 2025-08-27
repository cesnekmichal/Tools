/*    */ package com.tool.retimebyname;
/*    */ 
/*    */ import java.io.IOException;
/*    */ import java.io.InputStream;
/*    */ import java.io.OutputStream;
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ public class Tools
/*    */ {
/*    */   public static void exec(String command) {
/*    */     try {
/* 31 */       Process exec = Runtime.getRuntime().exec(command);
/* 32 */       exec.waitFor();
/* 33 */     } catch (Exception ex) {
/* 34 */       ex.printStackTrace();
/*    */     } 
/*    */   }
/*    */   
/*    */   public static boolean readAndWrite(InputStream is, OutputStream os, int sizeOfBuffer) {
/* 39 */     if (is == null || os == null) return false; 
/* 40 */     byte[] buffer = new byte[sizeOfBuffer];
/*    */     try {
/*    */       int count;
/*    */       do {
/* 44 */         count = is.read(buffer);
/* 45 */         if (count <= 0)
/* 46 */           continue;  os.write(buffer, 0, count);
/*    */       }
/* 48 */       while (count > 0);
/* 49 */     } catch (IOException ex) {
/* 50 */       return false;
/*    */     } 
/* 52 */     return true;
/*    */   }
/*    */ }


/* Location:              C:\CHAPPIE\Fotky\_aplikace\RetimeByName\RetimeByName.zip!\retimebyname\Tools.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */