package com.github.razorplay01.cam.starup;

import com.github.razorplay01.cam.api.CameraModifier;
import com.github.razorplay01.cam.api.CameraPlugin;
import com.github.razorplay01.cam.api.ModifierPriority;
import com.github.razorplay01.cam.api.Plugin;
import com.github.razorplay01.cam.core.Modifier;
import com.github.razorplay01.cam.core.ModifierRegistry;
import net.minecraft.resources.ResourceLocation;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.ModContainerImpl;
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

public final class AnnotationFinder {
    private static final Logger LOGGER = LoggerFactory.getLogger("FreeCameraAPI/AnnotationFinder");
    private static final String PLUGIN_DESC = "L" + Plugin.class.getName().replace('.', '/') + ";";

    public static void clientLoading() {
        LOGGER.debug("=== Iniciando carga de plugins de FreeCameraAPI ===");
        loadPlugin();
        LOGGER.debug("=== Carga de plugins completada ===");
    }

    private static void loadPlugin() {
        LOGGER.debug("Descriptor de anotación @Plugin: {}", PLUGIN_DESC);

        List<Triplet<CameraModifier, CameraPlugin, ModifierPriority>> plugins = findPlugin();

        LOGGER.debug("Total de plugins encontrados: {}", plugins.size());

        List<ResourceLocation> order = new ArrayList<>();

        for (Triplet<CameraModifier, CameraPlugin, ModifierPriority> triplet : plugins) {
            CameraModifier modifier = triplet.getA();
            CameraPlugin plugin = triplet.getB();
            ModifierPriority priority = triplet.getC();

            LOGGER.debug("Registrando plugin: {} con prioridad: {}",
                    modifier.getId(), priority);

            try {
                ModifierRegistry.INSTANCE.register(plugin, priority, modifier);
                order.add(modifier.getId());
                LOGGER.debug("✓ Plugin {} registrado exitosamente", modifier.getId());
            } catch (Exception e) {
                LOGGER.error("✗ Error al registrar plugin {}", modifier.getId(), e);
            }
        }

        // ==================== FREEZE AUTOMÁTICO ====================
        if (!plugins.isEmpty()) {
            LOGGER.debug("Ejecutando freeze automático del ModifierRegistry...");

            List<String> orderStrings = new ArrayList<>();
            for (ResourceLocation id : order) {
                orderStrings.add(id.toString());
            }

            List<String> removed = new ArrayList<>();

            try {
                ModifierRegistry.INSTANCE.freeze(orderStrings, removed);
                LOGGER.debug("✓ Freeze completado exitosamente con {} plugins", plugins.size());

                if (!removed.isEmpty()) {
                    LOGGER.warn("Plugins removidos durante freeze: {}", removed);
                }
            } catch (Exception e) {
                LOGGER.error("✗ Error durante el freeze del ModifierRegistry", e);
            }
        } else {
            LOGGER.warn("⚠ No se encontraron plugins, freeze no ejecutado");
        }
        // ===========================================================
    }

    private static List<Triplet<CameraModifier, CameraPlugin, ModifierPriority>> findPlugin() {
        ArrayList<Triplet<CameraModifier, CameraPlugin, ModifierPriority>> plugins = new ArrayList<>();
        Collection<ModContainer> allMods = FabricLoader.getInstance().getAllMods();
        boolean dev = FabricLoader.getInstance().isDevelopmentEnvironment();

        LOGGER.debug("Escaneando {} mod(s) en modo: {}", allMods.size(), dev ? "DESARROLLO" : "PRODUCCIÓN");

        for (ModContainer modContainer : allMods) {
            String modId = modContainer.getMetadata().getId();

            try {
                List<PluginData> foundPlugins = scanModForPlugins(modContainer, dev);

                if (!foundPlugins.isEmpty()) {
                    LOGGER.debug("✓ Mod '{}' contiene {} plugin(s)", modId, foundPlugins.size());
                }

                for (PluginData pluginData : foundPlugins) {
                    String name = null;

                    try {
                        // region 读取id
                        String value = pluginData.value;

                        if (!dev && value.equals("dev")) {
                            LOGGER.debug("Plugin '{}' omitido (marcado como 'dev' en producción)", value);
                            continue;
                        }

                        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(modId, value);
                        LOGGER.debug("Procesando plugin: {} desde clase {}", id, pluginData.className);
                        // endregion

                        // region 读取优先级
                        ModifierPriority priority = pluginData.priority;
                        LOGGER.debug("Prioridad del plugin: {}", priority);
                        // endregion

                        // region 读取modifier
                        String modifierClass = pluginData.modifierClass;
                        CameraModifier modifier;

                        if (modifierClass != null && !modifierClass.isEmpty()) {
                            LOGGER.debug("Instanciando modifier personalizado: {}", modifierClass);
                            try {
                                modifier = Class.forName(modifierClass)
                                        .asSubclass(CameraModifier.class)
                                        .getConstructor(ResourceLocation.class)
                                        .newInstance(id);
                                LOGGER.debug("✓ Modifier personalizado creado: {}", modifierClass);
                            } catch (ClassNotFoundException e) {
                                LOGGER.error("✗ Clase de modifier no encontrada: {}", modifierClass);
                                throw CameraPluginInitializeException.modifierClassNotFound(modifierClass);
                            } catch (NoSuchMethodException e) {
                                LOGGER.error("✗ Constructor no encontrado en modifier: {}", modifierClass);
                                throw CameraPluginInitializeException.modifierNoSuchConstructor(modifierClass);
                            } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                                LOGGER.error("✗ Error al instanciar modifier: {}", modifierClass, e);
                                throw CameraPluginInitializeException.modifierInvocationTarget(modifierClass);
                            }
                        } else {
                            LOGGER.debug("Usando Modifier por defecto para {}", id);
                            modifier = new Modifier(id);
                        }
                        // endregion

                        // region 实例化plugin
                        name = pluginData.className;
                        LOGGER.debug("Instanciando plugin desde clase: {}", name);

                        CameraPlugin plugin = Class
                                .forName(name)
                                .asSubclass(CameraPlugin.class)
                                .getConstructor(CameraModifier.class)
                                .newInstance(modifier);

                        plugins.add(new Triplet<>(modifier, plugin, priority));
                        LOGGER.debug("✓ Plugin cargado exitosamente: {} [Prioridad: {}]", id, priority);
                        // endregion

                    } catch (ClassNotFoundException e) {
                        LOGGER.error("✗ Clase de plugin no encontrada: {}", name, e);
                        throw CameraPluginInitializeException.pluginClassNotFound(name);
                    } catch (NoSuchMethodException e) {
                        LOGGER.error("✗ Constructor no encontrado en plugin: {}", name, e);
                        throw CameraPluginInitializeException.pluginNoSuchConstructor(name);
                    } catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
                        LOGGER.error("✗ Error al instanciar plugin: {}", name, e);
                        throw CameraPluginInitializeException.pluginInvocationTarget(name);
                    }
                }

            } catch (Exception e) {
                LOGGER.error("Error escaneando mod '{}': {}", modId, e.getMessage(), e);
            }
        }

        LOGGER.debug("Escaneo completado. Plugins válidos encontrados: {}", plugins.size());
        return plugins;
    }

    private static List<PluginData> scanModForPlugins(ModContainer modContainer, boolean dev) {
        List<PluginData> plugins = new ArrayList<>();
        String modId = modContainer.getMetadata().getId();

        LOGGER.debug("Escaneando mod: {}", modId);

        try {
            List<Path> roots = getModRoots(modContainer);

            LOGGER.debug("Mod '{}' tiene {} ruta(s) de búsqueda", modId, roots.size());

            if (roots.isEmpty()) {
                LOGGER.warn("⚠ Mod '{}' no tiene rutas para escanear!", modId);
            }

            for (Path root : roots) {
                LOGGER.debug("Escaneando ruta: {}", root);
                scanPath(root, plugins, dev, modId);
            }

        } catch (Exception e) {
            LOGGER.error("Error obteniendo rutas del mod '{}': {}", modId, e.getMessage(), e);
        }

        return plugins;
    }

    private static List<Path> getModRoots(ModContainer modContainer) {
        List<Path> paths = new ArrayList<>();
        String modId = modContainer.getMetadata().getId();

        try {
            if (modContainer instanceof ModContainerImpl) {
                ModContainerImpl impl = (ModContainerImpl) modContainer;
                List<Path> originPaths = impl.getOrigin().getPaths();

                LOGGER.debug("Obtenidas {} rutas desde ModContainerImpl para '{}'", originPaths.size(), modId);

                for (Path originalPath : originPaths) {
                    paths.add(originalPath);

                    String pathStr = originalPath.toString();

                    // Buscar carpeta de classes en desarrollo
                    if (pathStr.contains("build" + java.io.File.separator + "resources" + java.io.File.separator + "main")) {
                        Path classesPath = Paths.get(pathStr.replace(
                                "build" + java.io.File.separator + "resources" + java.io.File.separator + "main",
                                "build" + java.io.File.separator + "classes" + java.io.File.separator + "java" + java.io.File.separator + "main"
                        ));

                        if (Files.exists(classesPath)) {
                            paths.add(classesPath);
                            LOGGER.debug("✓ Ruta de classes agregada: {}", classesPath);
                        }
                    }

                    // Intentar con bin/main (Eclipse)
                    Path projectRoot = originalPath.getParent().getParent().getParent();
                    Path binMain = projectRoot.resolve("bin").resolve("main");
                    if (Files.exists(binMain) && !paths.contains(binMain)) {
                        paths.add(binMain);
                        LOGGER.debug("✓ Ruta bin/main agregada: {}", binMain);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.debug("Error obteniendo rutas desde ModContainerImpl: {}", e.getMessage());
        }

        return paths;
    }

    private static void scanPath(Path root, List<PluginData> plugins, boolean dev, String modId) {
        try {
            if (!Files.exists(root)) {
                LOGGER.debug("Ruta no existe: {}", root);
                return;
            }

            if (Files.isDirectory(root)) {
                scanDirectory(root, plugins, dev, modId);
            } else if (root.toString().endsWith(".jar")) {
                scanJar(root, plugins, dev, modId);
            }
        } catch (Exception e) {
            LOGGER.error("Error escaneando ruta: {}", root, e);
        }
    }

    private static void scanDirectory(Path root, List<PluginData> plugins, boolean dev, String modId) throws IOException {
        int[] classCount = {0};

        try (Stream<Path> stream = Files.walk(root)) {
            stream.filter(path -> path.toString().endsWith(".class"))
                    .forEach(path -> {
                        classCount[0]++;
                        try {
                            try (InputStream is = Files.newInputStream(path)) {
                                analyzeClass(is, plugins, dev, modId);
                            }
                        } catch (Exception e) {
                            LOGGER.debug("Error analizando clase {}: {}", path.getFileName(), e.getMessage());
                        }
                    });
        }

        LOGGER.debug("Escaneadas {} clases en directorio de '{}'", classCount[0], modId);
    }

    private static void scanJar(Path jarPath, List<PluginData> plugins, boolean dev, String modId) throws IOException {
        int[] classCount = {0};

        try (FileSystem fs = FileSystems.newFileSystem(jarPath, (ClassLoader) null)) {
            for (Path root : fs.getRootDirectories()) {
                try (Stream<Path> stream = Files.walk(root)) {
                    stream.filter(path -> path.toString().endsWith(".class"))
                            .forEach(classFile -> {
                                classCount[0]++;
                                try {
                                    try (InputStream is = Files.newInputStream(classFile)) {
                                        analyzeClass(is, plugins, dev, modId);
                                    }
                                } catch (Exception e) {
                                    LOGGER.debug("Error analizando clase en JAR: {}", e.getMessage());
                                }
                            });
                }
            }
        }

        LOGGER.debug("Escaneadas {} clases en JAR de '{}'", classCount[0], modId);
    }

    private static void analyzeClass(InputStream classStream, List<PluginData> plugins, boolean dev, String modId) {
        try {
            ClassReader reader = new ClassReader(classStream);
            ClassNode classNode = new ClassNode();
            reader.accept(classNode, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            if (classNode.visibleAnnotations != null) {
                for (AnnotationNode annotation : classNode.visibleAnnotations) {
                    if (PLUGIN_DESC.equals(annotation.desc)) {
                        LOGGER.debug("✓✓✓ ¡PLUGIN ENCONTRADO en {}! ✓✓✓", classNode.name);

                        String value = extractAnnotationValue(annotation, "value", "default");
                        String priorityStr = extractAnnotationValue(annotation, "priority", "NORMAL");
                        String modifierClass = extractAnnotationValue(annotation, "modifier", null);

                        LOGGER.debug("  value='{}', priority='{}', modifier='{}'",
                                value, priorityStr, modifierClass != null ? modifierClass : "default");

                        if (!dev && "dev".equals(value)) {
                            LOGGER.debug("  Plugin omitido (dev en producción)");
                            continue;
                        }

                        ModifierPriority priority;
                        try {
                            priority = ModifierPriority.valueOf(priorityStr);
                        } catch (IllegalArgumentException e) {
                            LOGGER.warn("  Prioridad inválida '{}', usando NORMAL", priorityStr);
                            priority = ModifierPriority.NORMAL;
                        }

                        String className = classNode.name.replace('/', '.');

                        PluginData pluginData = new PluginData(className, value, priority, modifierClass);
                        plugins.add(pluginData);

                        LOGGER.debug("  ✓ Plugin añadido a lista de carga");
                    }
                }
            }

        } catch (Exception e) {
            LOGGER.debug("Error analizando clase: {}", e.getMessage());
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
                if (value instanceof org.objectweb.asm.Type) {
                    return ((org.objectweb.asm.Type) value).getClassName();
                }
                return value != null ? value.toString() : defaultValue;
            }
        }

        return defaultValue;
    }

    private record PluginData(String className, String value, ModifierPriority priority, String modifierClass) {
    }
}