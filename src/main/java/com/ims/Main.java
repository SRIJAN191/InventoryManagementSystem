package com.ims;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Main {
    private static final String APP_PACKAGE_PREFIX = "com.ims.";
    private static final String APP_CLASS_NAME = "com.ims.App";
    private static final String SOURCE_ROOT = "src/main/java";
    private static final String RESOURCE_ROOT = "src/main/resources";
    private static final String LIB_ROOT = "lib";
    private static final String OUTPUT_ROOT = "bin";

    private Main() {
    }

    public static void main(String[] args) {
        try {
            Path projectRoot = locateProjectRoot();
            Path outputRoot = projectRoot.resolve(OUTPUT_ROOT);

            if (needsCompilation(projectRoot, outputRoot)) {
                compileProject(projectRoot, outputRoot);
            }

            launchApplication(projectRoot, outputRoot, args);
        } catch (Exception exception) {
            throw new IllegalStateException(buildFailureMessage(exception), exception);
        }
    }

    private static Path locateProjectRoot() {
        Path current = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        while (current != null) {
            Path appSource = current.resolve(SOURCE_ROOT).resolve("com/ims/App.java");
            if (Files.exists(appSource)) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("Could not locate the project root from the current working directory.");
    }

    private static boolean needsCompilation(Path projectRoot, Path outputRoot) throws IOException {
        Path appClass = outputRoot.resolve("com/ims/App.class");
        if (!Files.exists(appClass)) {
            return true;
        }

        try (Stream<Path> sourceStream = Files.walk(projectRoot.resolve(SOURCE_ROOT))) {
            return sourceStream
                .filter(path -> path.toString().endsWith(".java"))
                .anyMatch(path -> isNewer(path, appClass));
        }
    }

    private static boolean isNewer(Path source, Path compiledClass) {
        try {
            return Files.getLastModifiedTime(source).toMillis() > Files.getLastModifiedTime(compiledClass).toMillis();
        } catch (IOException exception) {
            return true;
        }
    }

    private static void compileProject(Path projectRoot, Path outputRoot) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("A full JDK is required to compile the project.");
        }

        Files.createDirectories(outputRoot);

        List<Path> sourceFiles;
        try (Stream<Path> sourceStream = Files.walk(projectRoot.resolve(SOURCE_ROOT))) {
            sourceFiles = sourceStream
                .filter(path -> path.toString().endsWith(".java"))
                .sorted(Comparator.naturalOrder())
                .toList();
        }

        List<Path> libraryJars = resolveLibraryJars(projectRoot);
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);

        try {
            Iterable<? extends JavaFileObject> compilationUnits =
                fileManager.getJavaFileObjectsFromPaths(sourceFiles);

            List<String> options = new ArrayList<>();
            options.add("-encoding");
            options.add("UTF-8");
            options.add("-d");
            options.add(outputRoot.toString());
            if (!libraryJars.isEmpty()) {
                options.add("-cp");
                options.add(joinPaths(libraryJars));
            }

            boolean success = Boolean.TRUE.equals(
                compiler.getTask(null, fileManager, diagnostics, options, null, compilationUnits).call()
            );

            if (!success && !Files.exists(outputRoot.resolve("com/ims/App.class"))) {
                throw new IllegalStateException(formatDiagnostics(diagnostics));
            }
        } catch (RuntimeException exception) {
            if (!Files.exists(outputRoot.resolve("com/ims/App.class"))) {
                throw exception;
            }
        } finally {
            try {
                fileManager.close();
            } catch (Exception exception) {
                if (!Files.exists(outputRoot.resolve("com/ims/App.class"))) {
                    throw new IllegalStateException("Compilation could not be completed.", exception);
                }
            }
        }
    }

    private static List<Path> resolveLibraryJars(Path projectRoot) throws IOException {
        Path libRoot = projectRoot.resolve(LIB_ROOT);
        if (!Files.isDirectory(libRoot)) {
            throw new IllegalStateException("Required library folder is missing: " + libRoot);
        }

        try (Stream<Path> jarStream = Files.list(libRoot)) {
            List<Path> jars = jarStream
                .filter(path -> path.toString().endsWith(".jar"))
                .sorted(Comparator.naturalOrder())
                .toList();
            if (jars.isEmpty()) {
                throw new IllegalStateException("No dependency jars were found in: " + libRoot);
            }
            return jars;
        }
    }

    private static void launchApplication(Path projectRoot, Path outputRoot, String[] args) throws Exception {
        List<URL> urls = new ArrayList<>();
        urls.add(outputRoot.toUri().toURL());
        Path resources = projectRoot.resolve(RESOURCE_ROOT);
        if (Files.isDirectory(resources)) {
            urls.add(resources.toUri().toURL());
        }
        for (Path jar : resolveLibraryJars(projectRoot)) {
            urls.add(jar.toUri().toURL());
        }

        try (URLClassLoader classLoader = new ChildFirstAppClassLoader(urls.toArray(URL[]::new), Main.class.getClassLoader())) {
            Thread.currentThread().setContextClassLoader(classLoader);
            Class<?> appClass = Class.forName(APP_CLASS_NAME, true, classLoader);
            Method mainMethod = appClass.getMethod("main", String[].class);
            mainMethod.invoke(null, (Object) args);
        }
    }

    private static final class ChildFirstAppClassLoader extends URLClassLoader {
        private ChildFirstAppClassLoader(URL[] urls, ClassLoader parent) {
            super(urls, parent);
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            synchronized (getClassLoadingLock(name)) {
                Class<?> loadedClass = findLoadedClass(name);
                if (loadedClass == null && name.startsWith(APP_PACKAGE_PREFIX)) {
                    try {
                        loadedClass = findClass(name);
                    } catch (ClassNotFoundException ignored) {
                        loadedClass = super.loadClass(name, false);
                    }
                }
                if (loadedClass == null) {
                    loadedClass = super.loadClass(name, false);
                }
                if (resolve) {
                    resolveClass(loadedClass);
                }
                return loadedClass;
            }
        }
    }

    private static String joinPaths(List<Path> paths) {
        return paths.stream()
            .map(Path::toString)
            .collect(Collectors.joining(System.getProperty("path.separator")));
    }

    private static String formatDiagnostics(DiagnosticCollector<JavaFileObject> diagnostics) {
        StringBuilder builder = new StringBuilder("Project compilation failed.");
        for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
            builder.append(System.lineSeparator())
                .append(diagnostic.getKind())
                .append(": ");
            if (diagnostic.getSource() != null) {
                builder.append(diagnostic.getSource().getName())
                    .append(':')
                    .append(diagnostic.getLineNumber())
                    .append(" - ");
            }
            builder.append(diagnostic.getMessage(Locale.getDefault()));
        }
        return builder.toString();
    }

    private static String buildFailureMessage(Exception exception) {
        return """
            Unable to launch Inventory Management System.

            Make sure the project is opened at its root and the local dependency jars exist in the 'lib' folder.
            If the problem continues in VS Code, run:
            1. Java: Clean Java Language Server Workspace
            2. Developer: Reload Window
            """.trim();
    }
}
