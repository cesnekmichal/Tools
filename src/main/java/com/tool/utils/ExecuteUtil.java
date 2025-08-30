package com.tool.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 *
 * @author Michal
 */
public class ExecuteUtil {

    public static void runInThread(Runnable runnable){
        new Thread(runnable).start();
    }
    
    public static <U> List<U> runsAsync(List<Supplier<U>> suppliers,BiConsumer<U,Throwable> whenPartialComplete){
        return runsAsync(suppliers.toArray(Supplier[]::new),whenPartialComplete);
    }
    
    public static <U> List<U> runsAsync(Supplier<U>[] suppliers,BiConsumer<U,Throwable> whenPartialComplete){
        class run{
            static <U> U get(CompletableFuture<U> cf){
                try {
                    return cf.get();
                } catch (InterruptedException | ExecutionException ex) {
                    ex.printStackTrace();
                    return null;
                }
            }
        }
        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            List<CompletableFuture> futuresList = new ArrayList<>();
            for (Supplier<U> supplier : suppliers) {
                futuresList.add(CompletableFuture.supplyAsync(supplier,executor).whenComplete(whenPartialComplete));
            }
            CompletableFuture<Void> futures = CompletableFuture.allOf(futuresList.toArray(CompletableFuture[]::new));
            futures.join();
            return futuresList.stream().map((cf) -> (U) run.get(cf)).collect(Collectors.toList());
        }
    }
    
    public static String exec(String... command) {
        StringBuilder result = new StringBuilder();
            try {
                ProcessBuilder builder = new ProcessBuilder(command);
                builder.redirectErrorStream(true);
                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line).append(System.lineSeparator());
                    }
                }
                int exitCode = process.waitFor();
                if (exitCode != 0) {
                    System.err.println("exitCode: "+exitCode);
                }
            } catch (Exception e) {
                result.append("Chyba: ").append(e.getMessage());
            }
        return result.toString();
    }
    
    public static CompletableFuture<String> execAsync(String... command) {
        return CompletableFuture.supplyAsync(()->exec(command));
    }
    
    /** Otevře umístění souboru. */
    public static boolean openFileLocation(File f){
        if(isWindows()){
            try {
                String path = f.getCanonicalPath();
                //Cesta k souboru nesmí obsahovat mezery jinak to přestane fungovat!
                //V místě kde je mezera musí začít nový parametr.
                List<String> commands = new ArrayList<>();
                commands.add("explorer.exe");
                commands.add("/select,");
                commands.addAll(Arrays.asList(path.split(" ")));
                Process p = new ProcessBuilder(commands).inheritIO().start();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }
    
    public static boolean isWindows(){
        return System.getProperty("os.name").toUpperCase().contains("WINDOWS");
    }
    
}
