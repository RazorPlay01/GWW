package com.github.razorplay01.cam.starup;

import com.github.razorplay01.GWW;
import com.github.razorplay01.cam.api.CameraPlugin;
import com.github.razorplay01.cam.api.ICameraPlugin;
import com.github.razorplay01.cam.api.ModifierPriority;
import com.github.razorplay01.cam.core.ModifierRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.ModContainerImpl;
import net.minecraft.resources.ResourceLocation;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Triplet;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Stream;

public final class PluginFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger("FreeCameraAPI/PluginFinder");
    private static final String CAMERA_PLUGIN_DESC = "L" + CameraPlugin.class.getName().replace('.', '/') + ";";

    // CAMBIAR ESTO AL ID DE TU MOD
    private static final String TARGET_MOD = GWW.MOD_ID;

    public static void loadPlugin() {
        LOGGER.info("=== Iniciando búsqueda de plugins de cámara ===");
        LOGGER.info("Descriptor de anotación buscado: {}", CAMERA_PLUGIN_DESC);
        LOGGER.info("Modo DEBUG activado solo para mod: {}", TARGET_MOD);

        List<Triplet<ResourceLocation, ICameraPlugin, ModifierPriority>> plugins = find();

        LOGGER.info("Total de plugins encontrados: {}", plugins.size());

        for (Triplet<ResourceLocation, ICameraPlugin, ModifierPriority> triplet : plugins) {
            LOGGER.info("Registrando plugin: {} con prioridad: {}",
                    triplet.getA(), triplet.getC());
            try {
                ModifierRegistry.INSTANCE.register(triplet.getA(), triplet.getB(), triplet.getC());
                LOGGER.info("✓ Plugin {} registrado exitosamente", triplet.getA());
            } catch (Exception e) {
                LOGGER.error("✗ Error al registrar plugin {}", triplet.getA(), e);
            }
        }

        // ==================== FREEZE AUTOMÁTICO ====================
        if (!plugins.isEmpty()) {
            List<String> order = new ArrayList<>();
            List<String> removed = new ArrayList<>();

            // Orden por prioridad (puedes personalizarlo después)
            for (Triplet<ResourceLocation, ICameraPlugin, ModifierPriority> p : plugins) {
                order.add(p.getA().toString());
            }

            ModifierRegistry.INSTANCE.freeze(order, removed);
            LOGGER.info("✓ Freeze automático completado con {} plugins", plugins.size());
        } else {
            LOGGER.warn("⚠ No se encontraron plugins, freeze no ejecutado");
        }
        // ===========================================================

        LOGGER.info("=== Carga de plugins completada ===");
    }

    private static List<Triplet<ResourceLocation, ICameraPlugin, ModifierPriority>> find() {
        ArrayList<Triplet<ResourceLocation, ICameraPlugin, ModifierPriority>> plugins = new ArrayList<>();
        Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
        boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();

        LOGGER.info("Escaneando {} mod(s) en modo: {}", mods.size(), dev ? "DESARROLLO" : "PRODUCCIÓN");

        for (ModContainer modContainer : mods) {
            String modId = modContainer.getMetadata().getId();

            // SOLO logear para el mod objetivo
            boolean isTarget = modId.equals(TARGET_MOD);

            if (isTarget) {
                LOGGER.info(">>> ENCONTRADO MOD OBJETIVO: {} <<<", modId);
            }

            try {
                List<PluginData> foundPlugins = scanModForPlugins(modContainer, dev, isTarget);

                if (!foundPlugins.isEmpty()) {
                    LOGGER.info("✓ Mod '{}' contiene {} plugin(s)", modId, foundPlugins.size());
                }

                for (PluginData pluginData : foundPlugins) {
                    try {
                        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modId, pluginData.value);

                        LOGGER.info("Instanciando plugin: {} desde clase {}",
                                id, pluginData.className);

                        ICameraPlugin plugin = instantiatePlugin(pluginData.className);

                        plugins.add(new Triplet<>(id, plugin, pluginData.priority));

                        LOGGER.info("✓ Plugin cargado: {} [Prioridad: {}] desde {}",
                                id, pluginData.priority, pluginData.className);

                    } catch (Exception e) {
                        LOGGER.error("✗ Error al instanciar plugin {} desde clase {}",
                                pluginData.value, pluginData.className, e);
                    }
                }

            } catch (Exception e) {
                if (isTarget) {
                    LOGGER.error("ERROR CRÍTICO escaneando mod '{}': {}", modId, e.getMessage(), e);
                }
            }
        }

        LOGGER.info("Escaneo completado. Plugins válidos encontrados: {}", plugins.size());
        return plugins;
    }

    private static List<PluginData> scanModForPlugins(ModContainer modContainer, boolean dev, boolean verbose) {
        List<PluginData> plugins = new ArrayList<>();
        String modId = modContainer.getMetadata().getId();

        try {
            List<Path> roots = getModRoots(modContainer, verbose);

            if (verbose) {
                LOGGER.info("  Mod '{}' tiene {} ruta(s) de búsqueda", modId, roots.size());
                if (roots.isEmpty()) {
                    LOGGER.warn("  ⚠ Mod '{}' no tiene rutas para escanear!", modId);
                }
            }

            for (Path root : roots) {
                if (verbose) {
                    LOGGER.info("  Escaneando ruta: {}", root);
                }
                scanPath(root, plugins, dev, modId, verbose);
            }

        } catch (Exception e) {
            if (verbose) {
                LOGGER.error("  Error obteniendo rutas del mod '{}': {}", modId, e.getMessage(), e);
            }
        }

        return plugins;
    }

    private static List<Path> getModRoots(ModContainer modContainer, boolean verbose) {
        List<Path> paths = new ArrayList<>();
        String modId = modContainer.getMetadata().getId();

        if (verbose) {
            LOGGER.info("  Intentando obtener rutas para mod '{}'", modId);
        }

        try {
            if (modContainer instanceof ModContainerImpl) {
                ModContainerImpl impl = (ModContainerImpl) modContainer;
                List<Path> originPaths = impl.getOrigin().getPaths();

                if (verbose) {
                    LOGGER.info("  ✓ Obtenidas {} rutas desde ModContainerImpl", originPaths.size());
                    for (int i = 0; i < originPaths.size(); i++) {
                        LOGGER.info("    Ruta original {}: {}", i + 1, originPaths.get(i));
                    }
                }

                // En desarrollo, las rutas de ModContainerImpl apuntan a resources
                // Necesitamos agregar también la ruta de classes
                for (Path originalPath : originPaths) {
                    paths.add(originalPath); // Agregar la ruta original (resources)

                    // Intentar encontrar la carpeta de classes
                    String pathStr = originalPath.toString();

                    // Si la ruta contiene "build/resources/main", cambiarla a "build/classes/java/main"
                    if (pathStr.contains("build" + java.io.File.separator + "resources" + java.io.File.separator + "main")) {
                        Path classesPath = Paths.get(pathStr.replace(
                                "build" + java.io.File.separator + "resources" + java.io.File.separator + "main",
                                "build" + java.io.File.separator + "classes" + java.io.File.separator + "java" + java.io.File.separator + "main"
                        ));

                        if (Files.exists(classesPath)) {
                            paths.add(classesPath);
                            if (verbose) {
                                LOGGER.info("    ✓ Ruta de classes agregada: {}", classesPath);
                            }
                        } else {
                            if (verbose) {
                                LOGGER.warn("    ⚠ Ruta de classes no existe: {}", classesPath);
                            }
                        }
                    }

                    // También intentar con bin/main (para Eclipse)
                    if (pathStr.contains("bin" + java.io.File.separator + "main")) {
                        // Ya está en bin/main, no hacer nada
                    } else {
                        // Intentar construir ruta bin/main
                        Path projectRoot = originalPath.getParent().getParent().getParent();
                        Path binMain = projectRoot.resolve("bin").resolve("main");
                        if (Files.exists(binMain) && !paths.contains(binMain)) {
                            paths.add(binMain);
                            if (verbose) {
                                LOGGER.info("    ✓ Ruta bin/main agregada: {}", binMain);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            if (verbose) {
                LOGGER.warn("  ⚠ Error obteniendo rutas desde ModContainerImpl: {}", e.getMessage());
            }
        }

        // Método alternativo si no se encontraron rutas
        if (paths.isEmpty() && verbose) {
            LOGGER.info("  Intentando método alternativo de búsqueda de rutas...");
            try {
                Path workingDir = Paths.get("").toAbsolutePath();
                LOGGER.info("  Directorio de trabajo: {}", workingDir);

                Path buildClasses = workingDir.resolve("build").resolve("classes").resolve("java").resolve("main");
                if (Files.exists(buildClasses)) {
                    paths.add(buildClasses);
                    LOGGER.info("  ✓ Agregada ruta de build/classes: {}", buildClasses);
                }

                Path binMain = workingDir.resolve("bin").resolve("main");
                if (Files.exists(binMain)) {
                    paths.add(binMain);
                    LOGGER.info("  ✓ Agregada ruta de bin/main: {}", binMain);
                }

            } catch (Exception e) {
                LOGGER.warn("  ⚠ Método alternativo falló: {}", e.getMessage());
            }
        }

        if (paths.isEmpty() && verbose) {
            LOGGER.error("  ✗ NO SE ENCONTRARON RUTAS VÁLIDAS PARA MOD '{}'", modId);
        }

        return paths;
    }

    private static void scanPath(Path root, List<PluginData> plugins, boolean dev, String modId, boolean verbose) {
        try {
            if (verbose) {
                LOGGER.info("    Ruta existe: {}", Files.exists(root));
                LOGGER.info("    Es directorio: {}", Files.isDirectory(root));
                LOGGER.info("    Ruta completa: {}", root.toAbsolutePath());
            }

            if (!Files.exists(root)) {
                if (verbose) LOGGER.warn("    ⚠ Ruta no existe!");
                return;
            }

            if (Files.isDirectory(root)) {
                scanDirectory(root, plugins, dev, modId, verbose);
            } else if (root.toString().endsWith(".jar")) {
                scanJar(root, plugins, dev, modId, verbose);
            } else {
                if (verbose) LOGGER.warn("    ⚠ Ruta ignorada (no es directorio ni JAR)");
            }
        } catch (Exception e) {
            if (verbose) {
                LOGGER.error("    ✗ Error escaneando ruta: {}", e.getMessage(), e);
            }
        }
    }

    private static void scanDirectory(Path root, List<PluginData> plugins, boolean dev, String modId, boolean verbose) throws IOException {
        int[] classCount = {0};
        int[] totalFiles = {0};

        try (Stream<Path> stream = Files.walk(root)) {
            stream.forEach(path -> {
                totalFiles[0]++;
                if (path.toString().endsWith(".class")) {
                    classCount[0]++;

                    if (verbose && path.toString().contains("DevPlugin")) {
                        LOGGER.info("      ¡ENCONTRADO DevPlugin.class en: {}!", path);
                    }

                    try {
                        try (InputStream is = Files.newInputStream(path)) {
                            String relativePath = root.relativize(path).toString();
                            analyzeClass(is, plugins, dev, modId, relativePath, verbose);
                        }
                    } catch (Exception e) {
                        if (verbose) {
                            LOGGER.warn("      Error analizando {}: {}", path.getFileName(), e.getMessage());
                        }
                    }
                }
            });
        }

        if (verbose) {
            LOGGER.info("    Escaneados {} archivos totales, {} clases", totalFiles[0], classCount[0]);
        }
    }

    private static void scanJar(Path jarPath, List<PluginData> plugins, boolean dev, String modId, boolean verbose) throws IOException {
        int[] classCount = {0};

        try (FileSystem fs = FileSystems.newFileSystem(jarPath, (ClassLoader) null)) {
            for (Path root : fs.getRootDirectories()) {
                try (Stream<Path> stream = Files.walk(root)) {
                    stream.filter(path -> path.toString().endsWith(".class"))
                            .forEach(classFile -> {
                                classCount[0]++;
                                try {
                                    try (InputStream is = Files.newInputStream(classFile)) {
                                        analyzeClass(is, plugins, dev, modId, classFile.toString(), verbose);
                                    }
                                } catch (Exception e) {
                                    if (verbose) {
                                        LOGGER.warn("      Error analizando clase en JAR: {}", e.getMessage());
                                    }
                                }
                            });
                }
            }
        }

        if (verbose) {
            LOGGER.info("    Escaneadas {} clases en JAR", classCount[0]);
        }
    }

    private static void analyzeClass(InputStream classStream, List<PluginData> plugins, boolean dev, String modId, String classPath, boolean verbose) {
        try {
            ClassReader reader = new ClassReader(classStream);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            boolean isDevPlugin = classNode.name.contains("DevPlugin");

            if (verbose && isDevPlugin) {
                LOGGER.info("      >>> Analizando DevPlugin: {} <<<", classNode.name);
                LOGGER.info("      Tiene anotaciones: {}", classNode.visibleAnnotations != null);
                if (classNode.visibleAnnotations != null) {
                    LOGGER.info("      Número de anotaciones: {}", classNode.visibleAnnotations.size());
                }
            }

            if (classNode.visibleAnnotations != null) {
                for (AnnotationNode annotation : classNode.visibleAnnotations) {
                    if (verbose && isDevPlugin) {
                        LOGGER.info("        Anotación: {}", annotation.desc);
                        LOGGER.info("        Comparando con: {}", CAMERA_PLUGIN_DESC);
                        LOGGER.info("        Son iguales: {}", CAMERA_PLUGIN_DESC.equals(annotation.desc));
                    }

                    if (CAMERA_PLUGIN_DESC.equals(annotation.desc)) {
                        LOGGER.info("      ✓✓✓ ¡PLUGIN ENCONTRADO en {}! ✓✓✓", classNode.name);

                        String value = extractAnnotationValue(annotation, "value", "default");
                        String priorityStr = extractAnnotationValue(annotation, "priority", "NORMAL");

                        LOGGER.info("        value='{}', priority='{}'", value, priorityStr);

                        if (!dev && "dev".equals(value)) {
                            LOGGER.info("        Plugin omitido (dev en producción)");
                            continue;
                        }

                        ModifierPriority priority;
                        try {
                            priority = ModifierPriority.valueOf(priorityStr);
                        } catch (IllegalArgumentException e) {
                            priority = ModifierPriority.NORMAL;
                        }

                        String className = classNode.name.replace('/', '.');

                        PluginData pluginData = new PluginData(className, value, priority);
                        plugins.add(pluginData);

                        LOGGER.info("        ✓ Plugin añadido a lista");
                    }
                }
            }

        } catch (Exception e) {
            if (verbose) {
                LOGGER.error("      Error analizando clase: {}", e.getMessage());
            }
        }
    }

    private static String extractAnnotationValue(AnnotationNode annotation, String key, String defaultValue) {
        if (annotation.values == null) {
            return defaultValue;
        }

        for (int i = 0; i < annotation.values.size(); i += 2) {
            String currentKey = (String) annotation.values.get(i);
            Object value = annotation.values.get(i + 1);

            if (key.equals(currentKey)) {
                if (value instanceof String[]) {
                    String[] enumValue = (String[]) value;
                    return enumValue[1];
                }
                return value.toString();
            }
        }

        return defaultValue;
    }

    private static ICameraPlugin instantiatePlugin(String className) throws CameraPluginInitializeException {
        LOGGER.info("Instanciando plugin desde clase: {}", className);

        try {
            Class<?> clazz = Class.forName(className);

            ICameraPlugin plugin = clazz
                    .asSubclass(ICameraPlugin.class)
                    .getDeclaredConstructor()
                    .newInstance();

            LOGGER.info("✓ Instancia creada exitosamente");
            return plugin;

        } catch (ClassNotFoundException e) {
            throw CameraPluginInitializeException.classNotFound(className);
        } catch (NoSuchMethodException e) {
            throw CameraPluginInitializeException.noSuchMethod(className);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
            throw CameraPluginInitializeException.invocationTarget(className);
        }
    }

    private static class PluginData {
        final String className;
        final String value;
        final ModifierPriority priority;

        PluginData(String className, String value, ModifierPriority priority) {
            this.className = className;
            this.value = value;
            this.priority = priority;
        }
    }
}