package com.tool.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ZipUtil {

    /**
     * Rozbalí ZIP archiv z bajtového pole do nově vytvořeného dočasného adresáře.
     *
     * @param zipBytes Bajtové pole obsahující ZIP data.
     * @return Cesta (Path) k dočasnému adresáři, kam byl obsah rozbalen.
     * @throws IOException Pokud dojde k chybě při I/O operacích.
     */
    public static Path unzipToTempDir(byte[] zipBytes) throws IOException {
        // 1. Vytvoření unikátního dočasného adresáře pro rozbalený obsah
        Path tempDir = Files.createTempDirectory("unzipped-");
        //System.out.println("Obsah bude rozbalen do: " + tempDir.toAbsolutePath());

        // Použití try-with-resources pro automatické uzavření streamů
        try (ZipInputStream zipInputStream = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry entry;
            // 2. Procházení všech položek (souborů a adresářů) v ZIP archivu
            while ((entry = zipInputStream.getNextEntry()) != null) {
                // Sestavení cílové cesty pro položku uvnitř našeho dočasného adresáře
                Path entryPath = tempDir.resolve(entry.getName()).normalize();
                
                // 3. BEZPEČNOSTNÍ KONTROLA: Zabraňuje "Path Traversal" útoku
                // Ověříme, že se cílová cesta skutečně nachází uvnitř našeho temp adresáře.
                // Pokud by cesta začínala např. "../", mohlo by dojít k zápisu mimo cílový adresář.
                if (!entryPath.startsWith(tempDir)) {
                    throw new IOException("Nebezpečná položka v ZIP souboru: " + entry.getName());
                }

                if (entry.isDirectory()) {
                    // 4. Pokud je položka adresář, vytvoříme ho
                    Files.createDirectories(entryPath);
                } else {
                    // 5. Pokud je položka soubor:
                    // Nejprve zajistíme, že existuje nadřazený adresář
                    Files.createDirectories(entryPath.getParent());
                    // Zkopírujeme data ze streamu do cílového souboru
                    Files.copy(zipInputStream, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
                zipInputStream.closeEntry();
            }
        }
        
        //System.out.println("Rozbalení dokončeno úspěšně.");
        return tempDir;
    }

    public static void main(String[] args) {
        try {
            // Zde byste měli vaše reálné bajtové pole ze souboru nebo sítě
            // Pro účely demonstrace ho načteme ze souboru (předpokládáme, že existuje)
            // V reálné aplikaci tento blok nepotřebujete, pokud již máte `byte[]`
            Path zipFilePath = Path.of("archive.zip"); // UJISTĚTE SE, ŽE TENTO SOUBOR EXISTUJE
            if (!Files.exists(zipFilePath)) {
                 System.err.println("Pro spuštění dema vytvořte soubor 'archive.zip' s adresáři a soubory.");
                 return;
            }
            byte[] zipData = Files.readAllBytes(zipFilePath);

            // Zavolání naší metody pro rozbalení
            Path destinationDir = unzipToTempDir(zipData);

            // Nyní můžete s rozbalenými soubory pracovat...
            System.out.println("Soubory byly rozbaleny a jsou k dispozici v: " + destinationDir);
            
            // POZNÁMKA: Dočasný adresář a jeho obsah se automaticky nesmažou.
            // Pokud je po dokončení práce potřebujete smazat, musíte to udělat ručně.

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}