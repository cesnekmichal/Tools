package com.tool.gui.cs;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.text.SimpleDateFormat;
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
    
    public static enum PType{
        DATE,
        NUM
    }
    
    //yyyy-MM-dd HH:mm:ss
    public static class P{
        static P year   = new P("*year*"  ,"yyyy",PType.DATE,"e.g. 2025");
        static P month  = new P("*month*" ,"MM"  ,PType.DATE,"e.g. 12");
        static P day    = new P("*day*"   ,"dd"  ,PType.DATE,"e.g. 31");
        static P hour   = new P("*hour*"  ,"HH"  ,PType.DATE,"e.g. 23");
        static P minute = new P("*minute*","mm"  ,PType.DATE,"e.g. 59");
        static P second = new P("*second*","ss"  ,PType.DATE,"e.g. 00");
        static P num1   = new P("*1*"     ,"%01d",PType.NUM ,"e.g.   1");
        static P num2   = new P("*01*"    ,"%02d",PType.NUM ,"e.g.  01");
        static P num3   = new P("*001*"   ,"%03d",PType.NUM ,"e.g. 001");

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
            return List.of(year,month,day,hour,minute,second,num1,num2,num3);
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
        
        // Získání aktuálního fontu
        Font puvodniFont = textPane.getFont();
        // Vytvoření nového fontu se stejným názvem a stylem, ale větší velikostí
        Font novyFont = new Font("Monospaced", Font.BOLD, puvodniFont.getSize() + 2);
        // Nastavení nového fontu
        textPane.setFont(novyFont);

        
        add(textPane);
        // Nastavení výšky na jeden řádek
        int lineHeight = textPane.getFontMetrics(textPane.getFont()).getHeight();
        textPane.setPreferredSize(new Dimension(200, lineHeight + 6));
        
        // Stylování
        StyledDocument doc = textPane.getStyledDocument();
        StyleContext sc = StyleContext.getDefaultStyleContext();
        AttributeSet redAttr = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.RED);
        AttributeSet blueAttr = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, Color.BLUE);
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
        });
        
        textPane.setText(P.year+","+P.month+","+P.day);
        
        // Vytvoření popup menu
        JPopupMenu popupMenu = new JPopupMenu();
        List<String> presets = P.values().stream().map((v)->v.name).toList();

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
        btn.setMargin(new Insets(1, 5, 1, 5));
        btn.setFocusable(false);
        add(btn);
        
        //Nastavení okrajů
        setBorder(new LineBorder(Color.BLACK, 1));
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
        FileNamePatternEditor fileNamePatternEditor = new FileNamePatternEditor();
        panel.add(fileNamePatternEditor);
//        fileNamePatternEditor.setEnabled(false);
        frame.setSize(300, 100);
        SwingUtilities.invokeLater(()->frame.setVisible(true));
    }
    
}
