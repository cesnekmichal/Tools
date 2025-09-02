package com.tool.gui.cs;

import com.tool.utils.FileUtil;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.LineBorder;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Michal
 */
public class FileNamePatternEditor extends JPanel{
    
    /** 
     * Vrací formátovaný název souboru s ohledem na původní název, datum a číslo souboru.
     * 
     * @param fileName původní název souboru
     * @param fileDate datum vytvoření souboru
     * @param fileNum číslo souboru
     * @return formátovaný název souboru
     */
    public String getFormattedFileName(String fileName, Date fileDate, Integer fileNum, Integer fileCount) {
        String fileNamePattern = getText();
        //Prázdá šablonba -> původní název
        if(fileNamePattern.isBlank()) return fileName;
        String fileExt = FileUtil.getExtension(fileName);
        for (P p : P.values()) {
            if(p.type==PType.DATE){
                fileNamePattern = fileNamePattern.replace(p.name, new SimpleDateFormat(p.mask).format(fileDate));
            } else 
            if(p.type==PType.NUM){
                fileNamePattern = fileNamePattern.replace(p.name, String.format(p.mask, fileNum));
            } else 
            if(p.type==PType.NUMX){
                fileNamePattern = fileNamePattern.replace(p.name, String.format("%0"+fileCount.toString().length()+"d", fileNum));//%01d ... %09d
            } else 
            if(p.type==PType.EXT){
                if(p==P.ext){
                    fileNamePattern = fileNamePattern.replace(p.name, "."+fileExt.toLowerCase());
                } else 
                if(p==P.EXT){
                    fileNamePattern = fileNamePattern.replace(p.name, "."+fileExt.toUpperCase());
                }
            }
        }
        //Zbývající nepodporované tagy odstraníme
        fileNamePattern = fileNamePattern.replaceAll("\\*[^*]+\\*", "");
        //Přidáme příponu souboru pokud neexistuje
        if(!fileNamePattern.toLowerCase().endsWith(fileExt.toLowerCase())){
            fileNamePattern += "."+fileExt;
        }
        return fileNamePattern;
    }

    public static enum PType{
        DATE,
        NUM,
        NUMX,
        EXT,
        ;
    }
    
    //yyyy-MM-dd HH:mm:ss
    public static class P{
        static P year   = new P("*year*"  ,"yyyy",PType.DATE,"e.g. 2025");
        static P month  = new P("*month*" ,"MM"  ,PType.DATE,"e.g. 12");
        static P day    = new P("*day*"   ,"dd"  ,PType.DATE,"e.g. 31");
        static P hour   = new P("*hour*"  ,"HH"  ,PType.DATE,"e.g. 23");
        static P minute = new P("*minute*","mm"  ,PType.DATE,"e.g. 59");
        static P second = new P("*second*","ss"  ,PType.DATE,"e.g. 00");
        static P num1   = new P("*n*"     ,"%01d",PType.NUM ,"e.g.    1,    9");
        static P num2   = new P("*nn*"    ,"%02d",PType.NUM ,"e.g.   01,   99");
        static P num3   = new P("*nnn*"   ,"%03d",PType.NUM ,"e.g.  001,  999");
        static P num4   = new P("*nnnn*"  ,"%04d",PType.NUM ,"e.g. 0001, 9999");
        static P numX   = new P("*number*",null  ,PType.NUMX,"e.g. 1, 11, 111");
        static P ext    = new P("*.ext*"  ,null  ,PType.EXT, "e.g. .jpg, .mp4, .png");
        static P EXT    = new P("*.EXT*"  ,null  ,PType.EXT, "e.g. .JPG, .MP4, .PNG");

        String name;
        String mask;
        PType  type;
        String desc;
        private P(String name,String mask, PType type,String desc) {
            this.name = name;
            this.mask = mask;
            this.type = type;
            this.desc = desc;
        }
        public static P get(String name){
            for (P value : values()) {
                if(value.name.equals(name)) return value;
            }
            return null;
        }
        public static List<P> values(){
            return List.of(year,month,day,hour,minute,second,num1,num2,num3,num4,numX,ext,EXT);
        }
        @Override
        public String toString() {
            return name;
        }
    }
    
    private final JTextPane textPane = new JTextPane();
    
    private final JButton btn = new JButton("▼");
    
    public FileNamePatternEditor() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        
        setBackground(Color.WHITE);
        
        // Získání aktuálního fontu
        Font puvodniFont = textPane.getFont();
        // Vytvoření nového fontu se stejným názvem a stylem, ale větší velikostí
        Font novyFont = new Font("Monospaced", Font.BOLD, puvodniFont.getSize() + 2);
        // Nastavení nového fontu
        textPane.setFont(novyFont);

        
        add(textPane);
        // Nastavení výšky na jeden řádek
        int lineHeight = textPane.getFontMetrics(textPane.getFont()).getHeight();
        textPane.setPreferredSize(new Dimension(200, lineHeight + 0));
        
        // Stylování
        StyledDocument doc = textPane.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet redAttr     = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED  );
        AttributeSet blueAttr    = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLUE );
        AttributeSet defaultAttr = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLACK);
        
        // Filtr pro odstranění nových řádků a zvýraznění syntaxe
        ((AbstractDocument) doc).setDocumentFilter(new DocumentFilter() {
            private void highlight() {
                SwingUtilities.invokeLater(() -> {
                    String text = textPane.getText();
                    doc.setCharacterAttributes(0, text.length(), defaultAttr, true); // Reset

                    Matcher matcher = Pattern.compile("\\*[^*]+\\*").matcher(text);
                    while (matcher.find()) {
                        String match = matcher.group();
                        AttributeSet attr = P.get(match)!=null ? blueAttr : redAttr;
                        doc.setCharacterAttributes(matcher.start(), matcher.end() - matcher.start(), attr, false);
                    }
                });
            }
            @Override
            public void insertString(DocumentFilter.FilterBypass fb, int offset, String string, AttributeSet attr) throws BadLocationException {
                string = string.replaceAll("\\R", "");
                super.insertString(fb, offset, string, attr);
                highlight();
            }
            @Override
            public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String string, AttributeSet attrs) throws BadLocationException {
                string = string.replaceAll("\\R", "");
                super.replace(fb, offset, length, string, attrs);
                highlight();
            }
            @Override
            public void remove(DocumentFilter.FilterBypass fb, int offset, int length) throws BadLocationException {
                super.remove(fb, offset, length);
                highlight();
            }
        });
        
        // Vytvoření popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        for (P p : P.values()) {
            JMenuItem item = new JMenuItem(p.name+" - "+p.desc);
            item.addActionListener(e -> {
                int pos = textPane.getCaretPosition();
                String currentText = textPane.getText();
                String newText = currentText.substring(0, pos) + p.name + currentText.substring(pos);
                textPane.setText(newText);
                textPane.requestFocus();
                textPane.setCaretPosition(pos + p.name.length());
            });
            popupMenu.add(item);
        }

        // Zobrazení popup menu při kliknutí na tlačítko
        btn.addActionListener(e -> {
            popupMenu.show(btn, 0, btn.getHeight());
        });        
        btn.setMargin(new Insets(0, 5, 0, 5));
        btn.setFocusable(false);
        add(btn);
        
        //Nastavení okrajů
        setBorder(new LineBorder(Color.BLACK, 1));
    }

    public void setText(String text) {
        this.textPane.setText(text);
    }

    public String getText() {
        return textPane.getText();
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        textPane.setEnabled(enabled);
        btn.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return textPane.isEnabled();// && btn.isEnabled();
    }
    
    public static void main(String[] args) {
        JFrame frame = new JFrame("TextField s předvolbami");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setLayout(new FlowLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        frame.add(panel);
        FileNamePatternEditor editor = new FileNamePatternEditor();
        editor.setText(P.year+","+P.month+","+P.day);
        panel.add(editor);
//        fileNamePatternEditor.setEnabled(false);
        frame.setSize(300, 100);
        SwingUtilities.invokeLater(()->frame.setVisible(true));
    }
    
}
