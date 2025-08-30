package com.tool.gui.cs;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Česnek Michal, UNIDATAZ s.r.o.
 */
public class ActionsComboBoxRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        // Zavoláme metodu předka, abychom získali základní komponentu (obvykle JLabel)
        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

        if (value instanceof Actions) {
            Actions status = (Actions) value;
            // Zde nastavíme text, který chceme zobrazit
            setText(status.name().toUpperCase()); // Např. zobrazíme velkými písmeny
        }

        return this;
    }
}
