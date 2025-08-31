package com.tool.utils;

/**
 *
 * @author Michal
 */
public class StringUtil {


   /** preformatuje cislo 'size' oznacujici velikost v Bytech na retezec
    * napr. 1.34kB, 129GB
    *
    * @param size
    * @param decimalLength
    * @return
    * @see #formatFileSize(int, int, boolean, boolean, java.lang.String)
    */
    public static String formatFileSize(long size, int decimalLength) {
        return formatFileSize(size, decimalLength, true, true, "");
    }
    
    /** Přeformátuje číslo 'size' označující velikost v Bytech na řetězec. */
    public static String formatFileSize(long size, int decimalLength, boolean useBinaryFormat) {
        return formatFileSize(size, decimalLength, useBinaryFormat, useBinaryFormat, "");
    }
    
    /** preformatuje cislo 'size' oznacujici velikost v Bytech na retezec
     * napr. 1.23kB, 1.23KiB, 1.23 kB, ...
     *
     * <pre>
     * <b>POZOR:</b> useBinaryUnit a useBinary ovlivni formatovany retezec podle nasl. prikladu:
     *  true,  true: 1024 == "1,000 KiB"
     *  true, false: 1024 == "1,024 KiB" (<b>tato kombinace nema smysl!!</b>)
     * false,  true: 1024 == "1,000 kB"
     * false, false: 1024 == "1,024 kB"
     *</pre>
     * @param size velikost k naformatovani
     * @param decimalLength  pocet desetinnych mist (pokud je &le;0 tak se bere def. 2)
     * @param useBinaryUnit zda se ma pouzivat "kB" nebo binarni "KiB"
     * @param useBinary zda se ma pocitat v soustave 1000, nebo 1024
     * @param separator oddelovac mezi hodnotou a jednotkou
     * @return
     */
    public static String formatFileSize(long size, int decimalLength, boolean useBinaryUnit, boolean useBinary, String separator) {
        int p = decimalLength>=0 ? decimalLength : 2;
        String[] xs =
                useBinaryUnit
                ? new String[] {"B", "KiB", "MiB", "GiB", "TiB", "PiB"}
                : new String[] {"B",  "kB", "MB",  "GB",  "TB",  "PB"};
        int mul=useBinary ? 1024 : 1000;
        int x=0;
        long d = 1;
        for (x = 0; x+1 < xs.length; x++) {
            if (size/d <mul)
                break;
            d*=mul;
        }
        if (separator==null)
            separator = "";
        return String.format("%."+p+"f%s"+xs[x], ((double)size)/d, separator);
    }
    
    
}
