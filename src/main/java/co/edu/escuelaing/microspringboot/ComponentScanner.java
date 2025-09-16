package co.edu.escuelaing.microspringboot;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.stream.Collectors;

public class ComponentScanner {

    public static List<Class<?>> scanForControllers(String basePackage) throws IOException, ClassNotFoundException {
        List<Class<?>> classes = loadClasses(basePackage);

        return classes.stream()
                .filter(c -> c.isAnnotationPresent(RestController.class))
                .toList();
    }

    private static List<Class<?>> loadClasses(String basePackage) throws IOException, ClassNotFoundException {
        URL root = ComponentScanner.class.getClassLoader().getResource("");
        if (root != null) {
            try {
                Path baseDir = Paths.get(root.toURI());
                return findClassesInDirectory(baseDir, "", basePackage);
            } catch (URISyntaxException e) {
                throw new IOException("Error resolving base directory", e);
            }
        } else {
            return findClassesInJar(basePackage);
        }
    }

    private static List<Class<?>> findClassesInDirectory(Path dir, String currentPackage, String basePackage) throws IOException {
        if (!Files.exists(dir)) return Collections.emptyList();

        try (var stream = Files.walk(dir)) {
            return stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".class"))
                    .map(path -> toClassName(dir, path, currentPackage))
                    .filter(name -> name.startsWith(basePackage))
                    .map(ComponentScanner::safeLoadClass)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }

    private static String toClassName(Path rootDir, Path classFile, String currentPackage) {
        Path relative = rootDir.relativize(classFile);
        String joined = relative.toString().replace(File.separatorChar, '.');
        return (currentPackage.isEmpty() ? "" : currentPackage + ".") + joined.replace(".class", "");
    }

    private static List<Class<?>> findClassesInJar(String basePackage) throws IOException, ClassNotFoundException {
        CodeSource codeSource = ComponentScanner.class.getProtectionDomain().getCodeSource();
        if (codeSource == null) return Collections.emptyList();

        String jarPath = codeSource.getLocation().getPath();
        List<Class<?>> classes = new ArrayList<>();

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (name.endsWith(".class")) {
                    String className = name.replace('/', '.').replace(".class", "");
                    if (className.startsWith(basePackage)) {
                        classes.add(Class.forName(className));
                    }
                }
            }
        }
        return classes;
    }

    private static Class<?> safeLoadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            System.err.println("Could not load class: " + className);
            return null;
        }
    }
}
