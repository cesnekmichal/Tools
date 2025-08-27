package com.tool.utils;

import java.awt.Image;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.swing.ImageIcon;


/**
 * 
 * @author Michal
 */
public class ResourceUtil {
    
    /** Vraci konvertovanou ikonu v sérii rozlišení: 16x16, 32x32, 48x48, 64x64, 128x128 */
    public static List<Image> getScaledImages4Frame(String fileName, Class classInSamePackageAsFile){
        return getScaledImages(fileName,classInSamePackageAsFile,16,32,48,64,128);
    }
    
    /**
     * Vraci konvertovanou Ikonu. Pokud nebyla nalezena, pak vraci prazdnou ikonu;
     * @param sizes Velikosti X i Y.
     * @return Seznam ikon o zadaných velikostech.
     */
    public static List<Image> getScaledImages(String fileName, Class classInSamePackageAsFile, int... sizes){
        ArrayList<Image> icons = new ArrayList<>();
        for (int size : sizes) {
            icons.add(getScaledImageIcon(fileName, classInSamePackageAsFile, size).getImage());
        }
        return icons;
    }
    
    /**
     * Najde image soubor ve zdrojovem kodu
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * @param size velikost (šířka a výška)
     * @return Ikona o zadané velikosti.
     */
    public static ImageIcon getScaledImageIcon(String fileName,Class classInSamePackageAsFile, int size) {
        ImageIcon imageIcon = getImageIcon(fileName, classInSamePackageAsFile);
        if(imageIcon==null) return null;
        Image image = imageIcon.getImage();
        if(image==null) return null;
        Image scaledInstance = image.getScaledInstance(size, size, Image.SCALE_AREA_AVERAGING);
        if(scaledInstance==null) return null;
        return new ImageIcon(scaledInstance);
    }

    /**
     * Najde image soubor ve zdrojovem kodu a upraví jej dle zadaných parametrů.
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * @param width šířka
     * @param height výška
     * @return Ikona o zadaných velikostech
     */
    public static ImageIcon getScaledImageIcon(String fileName,Class classInSamePackageAsFile, int width, int height) {
        ImageIcon imageIcon = getImageIcon(fileName, classInSamePackageAsFile);
        if(imageIcon==null) return null;
        Image image = imageIcon.getImage();
        if(image==null) return null;
        Image scaledInstance = image.getScaledInstance(width, height, Image.SCALE_AREA_AVERAGING);
        if(scaledInstance==null) return null;
        return new ImageIcon(scaledInstance);
    }
    
    /**
     * Najde image soubor ve zdrojovem kodu a upraví jej dle zadaných parametrů.
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * @param height výška
     * @return Ikona o zadaných velikostech
     */
    public static ImageIcon getScaledProportionallyHeight(String fileName,Class classInSamePackageAsFile,int height){
        ImageIcon imageIcon = getImageIcon(fileName,classInSamePackageAsFile);
        int w = imageIcon.getIconWidth();
        int h = imageIcon.getIconHeight();
        //int proportionalWidth = Double.valueOf(((double)height/(double)h)*(double)w).intValue();
        int proportionalWidth = (int) ( ((1.*height)/h)*w);
        imageIcon = getScaledImageIcon(fileName,classInSamePackageAsFile,proportionalWidth,height);
        return imageIcon;
    }
    
    /**
     * Najde image soubor ve zdrojovem kodu a upraví jej dle zadaných parametrů.
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * @param scale násobič změny velikosti
     * @return Ikona o zadaných velikostech
     */
    public static ImageIcon getScaledProportionally(String fileName,Class classInSamePackageAsFile,float scale){
        ImageIcon imageIcon = getImageIcon(fileName,classInSamePackageAsFile);
        int sW = (int) (imageIcon.getIconWidth() *scale);
        int sH = (int) (imageIcon.getIconHeight()*scale);
        imageIcon = getScaledImageIcon(fileName,classInSamePackageAsFile,sW,sH);
        return imageIcon;
    }
    
    /**
     * Najde image soubor ve zdrojovem kodu a upraví jej dle zadaných parametrů.
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku
     * @param width šířka
     * @return Ikona o zadaných velikostech
     */
    public static ImageIcon getScaledProportionallyWidth(String fileName,Class classInSamePackageAsFile,int width){
        ImageIcon imageIcon = getImageIcon(fileName,classInSamePackageAsFile);
        int w = imageIcon.getIconWidth();
        int h = imageIcon.getIconHeight();
//        int proportionalHeight = Double.valueOf(((double)width/(double)w)*(double)h).intValue();
        int proportionalHeight = (int) ( ((1.*width)/w)*h );
        imageIcon = getScaledImageIcon(fileName,classInSamePackageAsFile,width, proportionalHeight);
        return imageIcon;
    }

    /**
     * Najde image soubor ve zdrojovem kodu
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * jako "soubor.png".
     * @return URL k image souboru
     */
    public static ImageIcon getImageIcon(String fileName,Class classInSamePackageAsFile) {
        URL url = getUrl(fileName, classInSamePackageAsFile);
        if(url==null) return null;
        return new ImageIcon(url);
    }
    
    /**
     * Najde TXT soubor ve zdrojovem kodu
     * @param fileName Název souboru s příponou (např. "soubor.txt")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * jako "soubor.txt".
     * @return Text souboru
     */
    public static String getText(String fileName,Class classInSamePackageAsFile) {
        InputStream is = getStream(fileName, classInSamePackageAsFile);
        if(is==null) return null;
        String text = null;
        try {
            text = new String(is.readAllBytes(),StandardCharsets.UTF_8);
        } catch (IOException ex) { ex.printStackTrace(); }
        return text;
    }    
    
    /**
     * Najde soubor ve zdrojovem kodu
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * jako "soubor.png".
     * @return URL k souboru
     */
    public static URL getUrl(String fileName,Class classInSamePackageAsFile) {
        String path = getPATH(fileName, classInSamePackageAsFile);
        URL url = classInSamePackageAsFile.getResource(path);
        return url;
    }
    
    /**
     * Najde soubor ve zdrojovem kodu
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * jako "soubor.png".
     * @return data souboru
     */
    public static byte[] getBytes(String fileName,Class classInSamePackageAsFile) {
        InputStream inputStream = getStream(fileName, classInSamePackageAsFile);
        try {
            byte[] bytes = inputStream.readAllBytes();
            return bytes;
        } catch (IOException ex) {
            return null;
        }
    }    
    
    /**
     * Najde soubor ve zdrojovem kodu
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * jako "soubor.png".
     * @return InputStream k souboru
     */
    public static InputStream getStream(String fileName,Class classInSamePackageAsFile) {
        String path = getPATH(fileName, classInSamePackageAsFile);
        InputStream inputStream = ResourceUtil.class.getResourceAsStream(path);
        return inputStream;
    }
    
    /**
     * Vraci plnou cestu k souboru v projektu
     * @param fileName Název souboru s příponou (např. "soubor.png")
     * @param classInSamePackageAsFile Třída která se vyskytuje ve stejném balíku 
     * jako "soubor.png".
     * @return Plná cesta k souboru v projektu
     */
    public static String getPATH(String fileName,Class classInSamePackageAsFile){
        String path ="";
        if (classInSamePackageAsFile!=null) {
             path += "/"+classInSamePackageAsFile.getPackage().getName()+"/";
            path = path.replaceAll("\\.", "/");
        }
        path += fileName;
        return path;
    }

//    /** v zadanem baliku packageName (tvaru package1.package2.package3) vyhleda soubory odpovidajici zadanemu pattern.
//     *
//     * @param packageName package1.package2.package3
//     * @param pattern ".*\\.txt
//     * @return nalezene soubory napr. "/package1/package2/package3/a.txt
//     */
//    public static List<String> getResourceNameFromPackage( String packageName, Pattern pattern ) {
//        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
////        configurationBuilder.addUrls(ClasspathHelper.forPackage(packageName));
//        Collection<URL> urls = ClasspathHelper.forPackage(packageName);
////        System.out.println( "urls="+urls );
//        for (URL url : urls.toArray(new URL[0])) {
//            //Při spuštění z NetBeans prohledával tyto cesty, což trvalo dost dlouho
//            //a stejně tam žádné konfigurační UniPOS *.properties soubory nejsou!!!
////            if(url.getPath().endsWith("/build/classes/") || url.getPath().endsWith("/run/data/")){
////                urls.remove(url);
////            }
//        }
//        configurationBuilder.addUrls(urls);
//        //configurationBuilder.setScanners(new ResourcesScanner());
//        configurationBuilder.setScanners(Scanners.Resources);
//        // vyfiltrujeme jen ty, ktere jsou pro náš balík (v 't' je uplna cesta napr. unidataz.uniservice.....a.sql)
//        configurationBuilder.filterInputsBy((String t) ->  t!=null && t.startsWith( packageName));
//        Reflections reflections = new Reflections(configurationBuilder);
//        //vyfiltrujeme jen ty, ktere odpovidaji vzoru
//        Collection<String> files = reflections.getResources(pattern);
//        return files.stream().map( s->"/"+s).collect( Collectors.toList());
//    }

    /**
     * zda se ma preferovat vycitani souboru z jaru a ne podle classpath treba z ext, nebo data
     */
    private boolean primaryInJar=false;
    private String path;

    public ResourceUtil withPath( String fileName, Class classInSamePackageAsFile ) {
        return withPath( getPATH( fileName, classInSamePackageAsFile ));
    }
    public ResourceUtil withPath( String path ) {
        this.path = path;
        return this;
    }

    public ResourceUtil withPrimaryInJar(  ) {
        this.primaryInJar = true;
        return this;
    }

    public InputStream getInputStream() {
        if (primaryInJar) {
            String p = path;
            if (p.startsWith( "/"))
                p = p.replaceFirst( "^/", "");
            try {
                Enumeration<URL> urls = ResourceUtil.class.getClassLoader().getResources( p);
                while (urls.hasMoreElements()) {
                    URL url = urls.nextElement();
                    if (url.toString().contains( ".jar!")) {
                        InputStream is = url.openStream();
                        if (is!=null)
                            return is;
                    }
                }
            } catch (Exception ex) {
            }
        }
        return ResourceUtil.class.getResourceAsStream(path);
    }
    public String getContentAsString() {
        try {
            return new String(getInputStream().readAllBytes());
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    public static ResourceUtil create() {
        ResourceUtil rtv = new ResourceUtil();
        return rtv;
    }
    public static ResourceUtil create(String path) {
        ResourceUtil rtv = create().withPath( path );
        return rtv;
    }

    /** Vylistuje resource cesty. Path - např. /FontsDejaVuCondensed, nalezená cesta: /FontsDejaVuCondensed/DejaVuSansCondensed-Bold.ttf. */
    public static List<Path> listResources(String path,FileVisitOption... options) throws URISyntaxException, IOException {
        URL url = ResourceUtil.class.getResource(path);
        if(url==null) return Collections.EMPTY_LIST;
        URI uri = url.toURI();
        Path myPath;
        if (uri.getScheme().equals("jar")) {
            FileSystem fileSystem;
            try {
                fileSystem = FileSystems.newFileSystem(uri, Collections.<String, Object>emptyMap());
            } catch (java.nio.file.FileSystemAlreadyExistsException ex) {
                fileSystem = FileSystems.getFileSystem(uri);
            }
            myPath = fileSystem.getPath(path);
        } else {
            myPath = Paths.get(uri);
        }
        Stream<Path> walk = Files.walk(myPath, 1, options);
        return walk.collect(Collectors.toList());
    }
    
}
