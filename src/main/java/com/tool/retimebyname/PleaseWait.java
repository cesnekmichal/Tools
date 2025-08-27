/*    */ package com.tool.retimebyname;
/*    */ 
/*    */ import javax.swing.GroupLayout;
/*    */ import javax.swing.JFrame;
/*    */ import javax.swing.JLabel;
/*    */ import javax.swing.JProgressBar;
/*    */ import javax.swing.LayoutStyle;
/*    */ 
/*    */ 
/*    */ public class PleaseWait
/*    */   extends JFrame
/*    */ {
/*    */   private JLabel jLabel1;
/*    */   private JProgressBar jProgressBar1;
/*    */   
/*    */   public PleaseWait(Integer steps) {
/* 17 */     initComponents();
/* 18 */     this.jProgressBar1.setMinimum(0);
/* 19 */     this.jProgressBar1.setMaximum(steps.intValue());
/*    */   }
/*    */   
/*    */   public void addStep() {
/* 23 */     this.jProgressBar1.setValue(this.jProgressBar1.getValue() + 1);
/*    */   }
/*    */   
/*    */   public void setStep(Integer step) {
/* 27 */     this.jProgressBar1.setValue(step.intValue());
/*    */   }
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */   
/*    */   private void initComponents() {
/* 39 */     this.jProgressBar1 = new JProgressBar();
/* 40 */     this.jLabel1 = new JLabel();
/*    */     
/* 42 */     setDefaultCloseOperation(3);
/*    */     
/* 44 */     this.jLabel1.setText("Please wait !!!");
/*    */     
/* 46 */     GroupLayout layout = new GroupLayout(getContentPane());
/* 47 */     getContentPane().setLayout(layout);
/* 48 */     layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addContainerGap().addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(this.jProgressBar1, -1, 380, 32767).addComponent(this.jLabel1, -1, -1, 32767)).addContainerGap()));
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 57 */     layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING).addGroup(layout.createSequentialGroup().addGap(18, 18, 18).addComponent(this.jLabel1).addPreferredGap(LayoutStyle.ComponentPlacement.RELATED).addComponent(this.jProgressBar1, -2, 48, -2).addContainerGap(-1, 32767)));
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */ 
/*    */     
/* 67 */     pack();
/*    */   }
/*    */ }


/* Location:              C:\CHAPPIE\Fotky\_aplikace\RetimeByName\RetimeByName.zip!\retimebyname\PleaseWait.class
 * Java compiler version: 5 (49.0)
 * JD-Core Version:       1.1.3
 */