package com.tool.gui.cs;

import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FileListTable {

    public static void main(String[] args) {
        // Spuštění GUI v odděleném vlákně pro bezpečnost
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Seznam souborů");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Získání souborů z aktuálního adresáře
            File currentDir = new File(".");
            File[] files = currentDir.listFiles();

            // Vytvoření vlastního TableModelu se seznamem souborů
            FileTableModel model = new FileTableModel(files);

            // Vytvoření JTable s naším modelem
            JTable table = new JTable(model);

            // Přidání tabulky do JScrollPane pro možnost rolování
            JScrollPane scrollPane = new JScrollPane(table);

            frame.add(scrollPane);
            frame.pack();
            frame.setLocationRelativeTo(null); // Vycentrování okna
            frame.setVisible(true);
        });
    }
}

/**
 * Vlastní TableModel pro zobrazení seznamu souborů.
 */
class FileTableModel extends AbstractTableModel {

    private final File[] files;
    private final String[] columnNames = {"Název souboru", "Čas poslední úpravy"};
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    public FileTableModel(File[] files) {
        this.files = files != null ? files : new File[0];
    }

    @Override
    public int getRowCount() {
        return files.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        File file = files[rowIndex];
        switch (columnIndex) {
            case 0:
                // První sloupec: název souboru
                return file.getName();
            case 1:
                // Druhý sloupec: formátovaný čas poslední úpravy
                return dateFormat.format(new Date(file.lastModified()));
            default:
                return null;
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        // Umožní správné řazení podle typu dat
        if (columnIndex == 1) {
            return String.class; // I když je to datum, formátujeme ho jako String
        }
        return String.class;
    }
}
