package com.tool.gui.cs;

import java.awt.Component;
import java.awt.Dimension;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

public class ComboBoxUtils {

    /**
     * Nastaví preferovanou šířku JComboBox na základě nejpřesnějšího výpočtu
     * s použitím jeho rendereru. Toto je nejspolehlivější metoda.
     * @param <E> Typ položek v ComboBoxu
     * @param comboBox Komponenta, jejíž šířka se má nastavit
     */
    public static <E> void setWidthBasedOnRenderer(JComboBox<E> comboBox) {
        int maxWidth = 0;
        
        // 1. Získáme renderer, který JComboBox reálně používá
        ListCellRenderer<? super E> renderer = comboBox.getRenderer();
        
        // Tento trik použijeme, aby JList parametr nebyl null
        JList<? extends E> fakeList = new JList<>(); 

        // 2. Projdeme všechny položky v modelu
        for (int i = 0; i < comboBox.getItemCount(); i++) {
            E item = comboBox.getItemAt(i);
            
            // 3. Získáme komponentu tak, jak by ji renderer vykreslil pro danou položku
            //    Parametry isSelected a cellHasFocus jsou false, protože nás zajímá standardní stav.
            Component comp = renderer.getListCellRendererComponent(fakeList, item, i, false, false);
            
            // 4. Zeptáme se komponenty na její preferovanou šířku
            int componentWidth = comp.getPreferredSize().width;
            if (componentWidth > maxWidth) {
                maxWidth = componentWidth;
            }
        }

        // 5. Nastavíme preferovanou velikost ComboBoxu
        Dimension prefSize = comboBox.getPreferredSize();
        // K šířce rendereru přidáme ještě prostor pro šipku a okraje
        prefSize.width = maxWidth + 25 + 5; 
        comboBox.setPreferredSize(prefSize);
    }
}
