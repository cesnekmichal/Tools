package com.tool.gui;

import com.tool.gui.cs.Actions;
import static com.tool.gui.cs.Actions.Retime_By_Name;
import com.tool.gui.cs.ActionsConsumer;
import com.tool.utils.ExecuteUtil;
import com.tool.utils.ExifToolsUtil;
import com.tool.utils.ExifToolsUtil.FileAndDate;
import com.tool.utils.ExifToolsUtil.FileAndType;
import com.tool.utils.ExifToolsUtil.FileFromTo;
import com.tool.utils.ExifToolsUtil.FileType;
import com.tool.utils.FileUtil;
import com.tool.utils.rescs.Resources;
import java.awt.Dimension;
import java.io.File;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.SwingUtilities;
import static com.tool.gui.cs.Actions.Rename_By_ExIfDateTime;
import com.tool.utils.StringUtil;
import java.util.stream.IntStream;
import javax.swing.UIManager;
import static com.tool.gui.cs.Actions.Encode_MOV_to_MP4;
import static com.tool.gui.cs.Actions.ReEncode_MP4_1080p;
import com.tool.utils.ExifToolsUtil.FileTypeDate;

/** 
 * Třída MenuDialog představuje dialogové okno pro provádění různých akcí na mediálních souborech.
 * Obsahuje komponenty pro výběr akce, zobrazení vstupních a výstupních dat, a ovládací prvky pro spuštění akcí.
 * Třída podporuje operace jako přejmenování souborů podle Exif dat, změnu času souborů podle názvu,
 * konverzi MOV souborů na MP4 a další.
 * 
 * @author Česnek Michal, UNIDATAZ s.r.o.
 */
public class MediaToolDialog extends javax.swing.JFrame {
    
    private interface AutoCloseableX extends AutoCloseable{
        @Override
        void close();
    }
    
    private class AutoCloseableResource{
        Runnable onOpen;
        Runnable onClose;
        public AutoCloseableResource(Runnable onOpen,Runnable onClose) {
            this.onOpen = onOpen;
            this.onClose = onClose;
        }
        public AutoCloseableX open(){
            onOpen.run();
            return onClose::run;
        }
    }
    
    AutoCloseableResource awtLocker;
    
    /**
     * Creates new form Menu
     */
    public MediaToolDialog() {
        initComponents();
        setTitle("MediaTool "+" ("+new File("").getAbsolutePath()+")");
        setSize(new Dimension(635, 450));
        setLocationRelativeTo(null);
        
        setIconImage(Resources.MediaTool.getImageIcon().getImage());
        
        field_Actions.addActionsConsumer(new ActionsConsumer() {
            @Override
            public void accept(Actions action) {
                viewAction(action);
            }
        });
        
        // 1. Propojení vertikálních posuvníků
        jScrollPane2.getVerticalScrollBar().setModel(jScrollPane1.getVerticalScrollBar().getModel());

        btn_Refresh.setText("");
        btn_Refresh.setIcon(Resources.refresh.getImageIcon(16));
        btn_Play.setText("");
        btn_Play.setIcon(Resources.play.getImageIcon(16));
        
        label_Left.setText("");
        label_Right.setText("");
        field_FileNamePattern.setEnabled(false);
        
        awtLocker = new AutoCloseableResource(() -> {
            //On Open
            field_Actions.setEnabled(false);
            btn_Refresh.setEnabled(false);
            btn_Play.setEnabled(false);
            field_Left.setEnabled(false);
            field_Right.setEnabled(false);
            field_FileNamePattern.setEnabled(false);
        }, () -> {
            //On Close
            field_Actions.setEnabled(true);
            btn_Refresh.setEnabled(true);
            btn_Play.setEnabled(true);
            field_Left.setEnabled(true);
            field_Right.setEnabled(true);
            field_FileNamePattern.setEnabled(field_Actions.getSelectedItem()==Actions.Rename);
        });
    }

    public static List<Supplier<FileAndType>> getFileTypesAsync(){
        return Stream.of(new File(".").listFiles())
                    .filter(File::isFile)
                    .map((file)->ExifToolsUtil.getFileAndTypeAsync(file))
                .collect(Collectors.toList());
    }
    
    public static List<Supplier<FileTypeDate>> getFileTypeDatesAsync(){
        return Stream.of(new File(".").listFiles())
                    .filter(File::isFile)
                    .map((file)->ExifToolsUtil.getExIfFileTypeDateTimeAsync(file))
                .collect(Collectors.toList());
    }
    
    private static File[] getOperatedFiles(Actions actions){
        switch (actions) {
            case Retime_By_Name -> {
                return Stream.of(new File(".").listFiles())
                    .filter((f)->f.isFile())
                    .filter((f)->ExifToolsUtil.DateFormat.fileName.equals(FileUtil.removeExtension(f.getName())))
                .toArray(File[]::new);
            }
        }
        return new File[0];
    }
    
    private void viewAction(Actions action){
        if(action==null) return;
        switch (action) {
            case Rename -> {
                ExecuteUtil.runInThread(()->viewAction_Rename());
            }
            case Rename_By_ExIfDateTime -> {
                ExecuteUtil.runInThread(()->viewAction_Rename_By_ExIfDateTime());
            }
            case Retime_By_Name -> {
                ExecuteUtil.runInThread(()->viewAction_Retime_By_Name());
            }
            case Encode_MOV_to_MP4 -> {
                ExecuteUtil.runInThread(()->viewAction_Convert_MOV_to_MP4());
            }
            case ReEncode_MP4_1080p -> {
                ExecuteUtil.runInThread(()->viewAction_ReConvert_MP4_1080p());
            }

        }        
    }
    
    private void doAction(Actions action){
        if(action==null) return;
        switch (action) {
            case Rename -> {
                ExecuteUtil.runInThread(()->doAction_Rename());
            }
            case Rename_By_ExIfDateTime -> {
                ExecuteUtil.runInThread(()->doAction_Rename_By_ExIfDateTime());
            }
            case Retime_By_Name -> {
                ExecuteUtil.runInThread(()->doAction_Retime_By_Name());
            }
            case Encode_MOV_to_MP4 -> {
                ExecuteUtil.runInThread(()->doAction_Encode_MOV_to_MP4());
            }
            case ReEncode_MP4_1080p -> {
                ExecuteUtil.runInThread(()->doAction_ReEncode_MP4_1080p());
            }

        }
    }
    
    private void viewAction_Rename(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<Supplier<FileTypeDate>> suppliersFiles = getFileTypeDatesAsync();
            progressBarInit(suppliersFiles.size());
            List<FileTypeDate> fileTypeDatesAll = ExecuteUtil.runsAsync(suppliersFiles,progressBarIncrement);
            List<FileTypeDate> fileTypeDates = fileTypeDatesAll.stream()
                .filter((ftd)->ftd.type.isMedia() && ftd.date!=null).toList();
            String left = fileTypeDates.stream().map((ftd)->ftd.file.getName()).collect(Collectors.joining("\n"));
            int fileCount = fileTypeDates.size();
            String right = IntStream.range(0, fileCount).boxed().map((Integer idx)->{
                progressBarIncrement();
                String fileName = fileTypeDates.get(idx).file.getName();
                Date   fileDate = fileTypeDates.get(idx).date;
                Integer fileNum = idx+1;
                return field_FileNamePattern.getFormattedFileName(fileName, fileDate, fileNum, fileCount);
            }).collect(Collectors.joining("\n"));
            
            label_Left.setText("Filename From");
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("Filename To (edit this filenames)");
            field_Right.setText(right);
            field_Right.setEditable(true);
            progressBarReset();
        }
    }
    private void doAction_Rename(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<String> fromFileNames = List.of(field_Left.getText().split("\\R"));
            List<String> toFileNames   = List.of(field_Right.getText().split("\\R"));
            if(fromFileNames.size()!=toFileNames.size()) return;
            List<FileFromTo> filesFromTo = IntStream.range(0, fromFileNames.size()).boxed()
                    .map((Integer idx)->new FileFromTo(new File(fromFileNames.get(idx)),new File(toFileNames.get(idx))))
                .toList();
            List<File> toFiles = filesFromTo.stream().map(new Function<FileFromTo, File> () {
                @Override
                public File apply(FileFromTo fft) {
                    progressBarIncrement();
                    boolean success = FileUtil.rename(fft.from, fft.to, StandardCopyOption.REPLACE_EXISTING);
                    return success ? fft.to : fft.from;
                }
            }).toList();
            String left = toFiles.stream().map((f)->f.getName()).collect(Collectors.joining("\n"));
            label_Left.setText("Filename From");
            field_Left.setText(left);
            field_Left.setEditable(false);
            progressBarReset();
        }
    }
    
    private void viewAction_Rename_By_ExIfDateTime(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<Supplier<FileAndType>> suppliersFiles = getFileTypesAsync();
            progressBarInit(suppliersFiles.size()*2);
            List<FileAndType> fileAndTypes = ExecuteUtil.runsAsync(suppliersFiles,progressBarIncrement);
            List<File> files = fileAndTypes.stream().filter((fat)->fat.type.isMedia()).map((fat)->fat.file).toList();
            String left = files.stream().map((f)->f.getName()).collect(Collectors.joining("\n"));
            List<Supplier<Date>> suppliers = files.stream().map((file)->ExifToolsUtil.getExIfDateTimeAsync(file)).toList();
            List<Date> dates = ExecuteUtil.runsAsync(suppliers,progressBarIncrement);
            String right = dates.stream().map((date)->ExifToolsUtil.DateFormat.exiftool.format(date, "<unknown>")).collect(Collectors.joining("\n"));
            label_Left.setText("Filename");
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("Exif datetime");
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }
    }
    private void doAction_Rename_By_ExIfDateTime(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<Supplier<FileAndType>> suppliersFATs = getFileTypesAsync();
            progressBarInit(suppliersFATs.size()*3);
            List<FileAndType> fileAndTypes = ExecuteUtil.runsAsync(suppliersFATs,progressBarIncrement);
            List<File> files = fileAndTypes.stream().filter((fat)->fat.type.isMedia()).map((fat)->fat.file).toList();
            List<Supplier<FileAndDate>> suppliersFADs = files.stream().map((file)->ExifToolsUtil.getExIfFileAndDateTimeAsync(file)).toList();
            List<FileAndDate> fileAndDates = ExecuteUtil.runsAsync(suppliersFADs,progressBarIncrement);
            List<FileAndDate> filesRenamed = fileAndDates.stream().map((new Function<FileAndDate, FileAndDate>() {
                @Override
                public FileAndDate apply(FileAndDate fad) {
                    progressBarIncrement();
                    if(fad.date==null) return fad;
                    File target = new File(fad.file.getParentFile(),ExifToolsUtil.DateFormat.fileName.format(fad.date)+"."+FileUtil.getExtension(fad.file.getName()));
                    if(fad.file.equals(target)) return fad;
                    if(target.exists()) target = FileUtil.getUnique(target.getParentFile(), target.getName());
                    boolean success = FileUtil.rename(fad.file, target, StandardCopyOption.REPLACE_EXISTING);
                    return success ? new FileAndDate(target,fad.date) : fad;
                }
            })).toList();
            String left = filesRenamed.stream().map((fad)->fad.file.getName()).collect(Collectors.joining("\n"));
            String right = filesRenamed.stream().map((fad)->ExifToolsUtil.DateFormat.exiftool.format(fad.date, "<unknown>")).collect(Collectors.joining("\n"));
            label_Left.setText("Filename");
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("Exif datetime");
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }
    }    
    
    private void viewAction_Retime_By_Name(){
        try(AutoCloseableX lock = awtLocker.open()){
            File[] files = getOperatedFiles(Actions.Retime_By_Name);
            String left = Stream.of(files).map((f)->f.getName()).collect(Collectors.joining("\n"));
            progressBarInit(files.length+1);
            progressBarIncrement();
            List<Supplier<Date>> suppliers = Stream.of(files).map((File file) -> ExifToolsUtil.getExIfDateTimeAsync(file)).collect(Collectors.toList());
            List<Date> dates = ExecuteUtil.runsAsync(suppliers,progressBarIncrement);
            String right = dates.stream().map((date)->ExifToolsUtil.DateFormat.exiftool.format(date, "<unknown>")).collect(Collectors.joining("\n"));
            label_Left.setText("Filename");
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("Exif datetime");
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }
    }
    private void doAction_Retime_By_Name(){
        try(AutoCloseableX lock = awtLocker.open()){
            File[] files = getOperatedFiles(Actions.Retime_By_Name);
            Map<File, Date> filesDates = Stream.of(files).collect(Collectors.toMap((file)->file, (file)->ExifToolsUtil.DateFormat.fileName.parse(FileUtil.removeExtension(file.getName()))));
            //Stream.of(files).map((file)->).collect(Collectors.toList());
            progressBarInit(files.length+1);
            progressBarIncrement();
            List<Supplier<Date>> suppliers = filesDates.entrySet().stream().map((entry)->ExifToolsUtil.setExIfDateTimeAsync(entry.getKey(), entry.getValue())).collect(Collectors.toList());
            List<Date> dates = ExecuteUtil.runsAsync(suppliers,progressBarIncrement);
            String right = dates.stream().map((date)->ExifToolsUtil.DateFormat.exiftool.format(date, "<unknown>")).collect(Collectors.joining("\n"));
            label_Right.setText("Exif datetime");
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }
    }

    private void viewAction_Convert_MOV_to_MP4(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<Supplier<FileAndType>> suppliersFATs = getFileTypesAsync();
            progressBarInit(suppliersFATs.size());
            List<FileAndType> fileAndTypes = ExecuteUtil.runsAsync(suppliersFATs,progressBarIncrement);
            List<File> filesMOV = fileAndTypes.stream().filter((fat)->fat.type==FileType.MOV).map((fat)->fat.file).toList();
            List<File> filesMP4 = fileAndTypes.stream().filter((fat)->fat.type==FileType.MP4).map((fat)->fat.file).toList();
            
            String left = filesMOV.stream().map((f)->f.getName()).collect(Collectors.joining("\n"));
            String right = filesMP4.stream().filter((f)->new File(FileUtil.changeExtension(f.getAbsolutePath(), "mov")).exists())
                                           .map((f)->f.getName()).collect(Collectors.joining("\n"));
            label_Left.setText("MOV files");
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("MP4 files (same filename as MOV)");
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }
    }
    private void doAction_Encode_MOV_to_MP4(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<Supplier<FileAndType>> suppliersFATs = getFileTypesAsync();
            progressBarInit(suppliersFATs.size()*2);
            List<FileAndType> fileAndTypes = ExecuteUtil.runsAsync(suppliersFATs,progressBarIncrement);
            List<Supplier<FileFromTo>> supplersFiles = fileAndTypes.stream().map(new Function<FileAndType, Supplier<FileFromTo>>() {
                @Override
                public Supplier<FileFromTo> apply(FileAndType fat) {
                    if(fat.type!=ExifToolsUtil.FileType.MOV) return ()->new FileFromTo(fat.file,null);
                    File fileMOV = fat.file;
                    File fileMP4 = new File(fat.file.getParentFile(),FileUtil.removeExtension(fat.file.getName())+".mp4");
                    return () -> {
                        Date date = ExifToolsUtil.getExIfDateTime(fileMOV).date;
                        FileFromTo fft = ExifToolsUtil.convertMOVtoMP4(fileMOV, fileMP4);
                        if(date!=null && fft.to!=null) {
                            ExifToolsUtil.setExIfDateTime(fft.to, date);
                        }
                        return fft;
                    };
                }
            }).toList();
            List<FileFromTo> filesFromTo = ExecuteUtil.runsAsync(supplersFiles,progressBarIncrement,2);
            List<File> filesMOV = filesFromTo.stream().filter((fft)->fft.to!=null).map((fft)->fft.from).toList();
            List<File> filesMP4 = filesFromTo.stream().filter((fft)->fft.to!=null).map((fft)->fft.to).toList();
            String left = filesMOV.stream().map((f)->f.getName()).collect(Collectors.joining("\n"));
            String right = filesMP4.stream().filter((f)->new File(FileUtil.changeExtension(f.getAbsolutePath(), "mov")).exists())
                                           .map((f)->f.getName()).collect(Collectors.joining("\n"));
            label_Left.setText("MOV files");
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("MP4 files (same filename as MOV)");
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }        
    }
    
    private void viewAction_ReConvert_MP4_1080p(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<Supplier<FileAndType>> suppliersFATs = getFileTypesAsync();
            progressBarInit(suppliersFATs.size());
            List<FileAndType> fileAndTypes = ExecuteUtil.runsAsync(suppliersFATs,progressBarIncrement);
            List<File> filesMP4 = fileAndTypes.stream().filter((fat)->fat.type==FileType.MP4).map((fat)->fat.file).toList();
            
            String left = filesMP4.stream().map((f)->f.getName()).collect(Collectors.joining("\n"));
            String right = filesMP4.stream().map((f)->StringUtil.formatFileSize(f.length(), 1)).collect(Collectors.joining("\n"));
            
            label_Left.setText("MP4 files: "+filesMP4.size());
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("Size: "+StringUtil.formatFileSize(filesMP4.stream().mapToLong((f)->f.length()).sum(), 1));
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }
    }
    private void doAction_ReEncode_MP4_1080p(){
        try(AutoCloseableX lock = awtLocker.open()){
            List<Supplier<FileAndType>> suppliersFATs = getFileTypesAsync();
            progressBarInit(suppliersFATs.size());
            List<FileAndType> fileAndTypes = ExecuteUtil.runsAsync(suppliersFATs,progressBarIncrement);
            fileAndTypes = fileAndTypes.stream().filter((fat)->fat.type==FileType.MP4).toList();
            List<Supplier<FileFromTo>> supplersFiles = fileAndTypes.stream().map(new Function<FileAndType, Supplier<FileFromTo>>() {
                @Override
                public Supplier<FileFromTo> apply(FileAndType fat) {
                    if(fat.type!=ExifToolsUtil.FileType.MP4) return ()->new FileFromTo(fat.file,null);
                    File fileMP4from = fat.file;
                    File fileMP4to = new File(fat.file.getParentFile(),FileUtil.removeExtension(fat.file.getName())+".tmp.mp4");
                    return () -> {
                        Date date = ExifToolsUtil.getExIfDateTime(fileMP4from).date;
                        FileFromTo fft = ExifToolsUtil.reConvertMP4toMP41080p(fileMP4from, fileMP4to);
                        if(date!=null && fft.to!=null) {
                            ExifToolsUtil.setExIfDateTime(fft.to, date);
                        }
                        if(fft.to!=null){
                            boolean success = FileUtil.rename(fft.to, fft.from, StandardCopyOption.REPLACE_EXISTING);
                            return success ? new FileFromTo(fileMP4from,fileMP4from) : new FileFromTo(fileMP4from,null);
                        }
                        return fft;
                    };
                }
            }).toList();
            progressBarInit(fileAndTypes.size()+1);
            progressBarIncrement();
            List<FileFromTo> filesFromTo = ExecuteUtil.runsAsync(supplersFiles,progressBarIncrement,4);
            List<File> filesMP4 = fileAndTypes.stream().filter((fat)->fat.type==FileType.MP4).map((fat)->fat.file).toList();
            String left = filesMP4.stream().map((f)->f.getName()).collect(Collectors.joining("\n"));
            String right = filesMP4.stream().map((f)->StringUtil.formatFileSize(f.length(), 1)).collect(Collectors.joining("\n"));
            label_Left.setText("MP4 files: "+filesMP4.size());
            field_Left.setText(left);
            field_Left.setEditable(false);
            label_Right.setText("Size: "+StringUtil.formatFileSize(filesMP4.stream().mapToLong((f)->f.length()).sum(), 1));
            field_Right.setText(right);
            field_Right.setEditable(false);
            progressBarReset();
        }        
    }
    
    final Runnable progressBarIncrement = () -> {
        progressBarIncrement();
    };
    
    AtomicInteger progressBarMaxValue = new AtomicInteger(1);
    AtomicInteger progressBarValue    = new AtomicInteger(0);
    
    private void progressBarInit(int maxValue){
        progressBarMaxValue.set(maxValue);
        progressBarValue   .set(0);
        progressBarRepaint_Later();
    }
    private void progressBarIncrement(){
        progressBarValue.incrementAndGet();
        progressBarRepaint_Later();
    }
    private void progressBarReset(){
        progressBarValue.set(0);
        progressBarRepaint_Later();
    }
    
    private void progressBarRepaint_Later(){
        SwingUtilities.invokeLater(()->progressBarRepaint());
    }
    private void progressBarRepaint(){
        progressBar.setMinimum(0);
        progressBar.setMaximum(progressBarMaxValue.get());
        progressBar.setValue  (progressBarValue.get());
        progressBar.repaint();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        field_Actions = new com.tool.gui.cs.ActionsComboBox();
        btn_Refresh = new javax.swing.JButton();
        btn_Play = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        label_Left = new javax.swing.JLabel();
        label_Right = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        field_Left = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        field_Right = new javax.swing.JTextArea();
        progressBar = new javax.swing.JProgressBar();
        field_FileNamePattern = new com.tool.gui.cs.FileNamePatternEditor();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Akce");

        btn_Refresh.setText("Refresh");
        btn_Refresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_RefreshActionPerformed(evt);
            }
        });

        btn_Play.setText("Play");
        btn_Play.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btn_PlayActionPerformed(evt);
            }
        });

        label_Left.setText("label_Left");
        label_Left.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));

        label_Right.setText("label_Right");
        label_Right.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 0));

        field_Left.setColumns(20);
        field_Left.setRows(5);
        jScrollPane1.setViewportView(field_Left);

        field_Right.setColumns(20);
        field_Right.setRows(5);
        jScrollPane2.setViewportView(field_Right);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addContainerGap())
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
                            .addComponent(label_Left))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(label_Right)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE))
                        .addGap(14, 14, 14))))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(label_Left)
                    .addComponent(label_Right))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 687, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(field_Actions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_Refresh)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(btn_Play)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(field_FileNamePattern, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addGap(15, 15, 15))
            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(btn_Refresh)
                    .addComponent(field_Actions, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(btn_Play)
                    .addComponent(field_FileNamePattern, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void btn_RefreshActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_RefreshActionPerformed
        
        field_Actions.fireActionsConsumers();
        
    }//GEN-LAST:event_btn_RefreshActionPerformed

    private void btn_PlayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btn_PlayActionPerformed
        
        Actions action = field_Actions.getSelectedItem();
        doAction(action);
        
    }//GEN-LAST:event_btn_PlayActionPerformed



    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btn_Play;
    private javax.swing.JButton btn_Refresh;
    private com.tool.gui.cs.ActionsComboBox field_Actions;
    private com.tool.gui.cs.FileNamePatternEditor field_FileNamePattern;
    private javax.swing.JTextArea field_Left;
    private javax.swing.JTextArea field_Right;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JLabel label_Left;
    private javax.swing.JLabel label_Right;
    private javax.swing.JProgressBar progressBar;
    // End of variables declaration//GEN-END:variables

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ex) {
            ex.printStackTrace();
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> new MediaToolDialog().setVisible(true));
    }

}
