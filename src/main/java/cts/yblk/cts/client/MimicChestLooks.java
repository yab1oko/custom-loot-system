package cts.yblk.cts.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.LidBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves and draws the original chest look for any mod without per-mod hardcoding.
 * Prefers the mimic block's own BER; otherwise asks {@link ChestRenderer} for its material
 * or probes common texture paths.
 */
public final class MimicChestLooks {
    private static final Map<Class<?>, Method> GET_MATERIAL = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Field[]> FIELD_CACHE = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, ResourceLocation> TEXTURE_CACHE = new ConcurrentHashMap<>();
    private static final Map<BlockPos, CachedFake> FAKE_CACHE = new ConcurrentHashMap<>();
    private static final ResourceLocation NO_TEXTURE = new ResourceLocation("cts", "no_texture");

    private MimicChestLooks() {
    }

    public static boolean render(BlockState mimic, BlockPos pos, float partialTick, PoseStack poseStack,
                                 MultiBufferSource buffer, int light, int overlay, float openness,
                                 Direction facing, ChestModelRenderer chestModel) {
        BlockEntity fake = cachedFake(mimic, pos);
        if (fake != null) {
            fake.setLevel(Minecraft.getInstance().level);
            applyLidOpenness(fake, openness);
            BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance()
                    .getBlockEntityRenderDispatcher().getRenderer(fake);
            if (renderer != null) {
                try {
                    renderer.render(fake, partialTick, poseStack, buffer, light, overlay);
                    return true;
                } catch (Throwable ignored) {
                    // Fall through to material / texture paths.
                }
            }

            Material material = materialFromChestRenderer(renderer, fake);
            if (material != null) {
                chestModel.render(poseStack, buffer, light, overlay, facing, openness, material);
                return true;
            }
        }

        Material atlas = materialFromAtlasGuess(mimic);
        if (atlas != null) {
            chestModel.render(poseStack, buffer, light, overlay, facing, openness, atlas);
            return true;
        }

        ResourceLocation texture = directTextureGuess(mimic);
        if (texture != null) {
            chestModel.render(poseStack, buffer, light, overlay, facing, openness, texture);
            return true;
        }

        chestModel.render(poseStack, buffer, light, overlay, facing, openness, Sheets.CHEST_LOCATION);
        return true;
    }

    @Nullable
    private static BlockEntity cachedFake(BlockState mimic, BlockPos pos) {
        CachedFake cached = FAKE_CACHE.get(pos);
        if (cached != null && cached.mimic.equals(mimic)) {
            return cached.entity;
        }
        BlockEntity created = createFake(mimic, pos);
        if (created == null) {
            FAKE_CACHE.remove(pos);
            return null;
        }
        FAKE_CACHE.put(pos.immutable(), new CachedFake(mimic, created));
        if (FAKE_CACHE.size() > 256) {
            FAKE_CACHE.clear();
        }
        return created;
    }

    @Nullable
    private static BlockEntity createFake(BlockState mimic, BlockPos pos) {
        if (!(mimic.getBlock() instanceof EntityBlock entityBlock)) {
            return null;
        }
        try {
            return entityBlock.newBlockEntity(pos, mimic);
        } catch (Throwable ignored) {
            return null;
        }
    }

    private record CachedFake(BlockState mimic, BlockEntity entity) {
    }

    private static void applyLidOpenness(BlockEntity be, float openness) {
        float clamped = Math.max(0.0F, Math.min(1.0F, openness));
        for (Field field : fieldsOf(be.getClass())) {
            Class<?> type = field.getType();
            String name = type.getName();
            if (!name.contains("LidController") && !name.contains("ChestLid")) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object controller = field.get(be);
                if (controller == null) {
                    continue;
                }
                setFloat(controller, "openness", clamped);
                setFloat(controller, "oOpenness", clamped);
                setBoolean(controller, "shouldBeOpen", clamped > 0.001F);
                return;
            } catch (Throwable ignored) {
            }
        }
        if (be instanceof LidBlockEntity) {
            // Lid is read-only on the interface; controller fields above cover vanilla/Forge chests.
        }
    }

    @Nullable
    private static Material materialFromChestRenderer(@Nullable BlockEntityRenderer<?> renderer, BlockEntity fake) {
        if (!(renderer instanceof ChestRenderer<?>)) {
            // Still try getMaterial if a subclass hierarchy exposes it without instanceof ChestRenderer (unlikely).
            if (renderer == null) {
                return null;
            }
        }
        Object target = renderer;
        if (target == null) {
            return null;
        }
        Method method = GET_MATERIAL.computeIfAbsent(target.getClass(), type -> {
            Class<?> cursor = type;
            while (cursor != null && cursor != Object.class) {
                for (Method candidate : cursor.getDeclaredMethods()) {
                    if (!"getMaterial".equals(candidate.getName())) {
                        continue;
                    }
                    Class<?>[] params = candidate.getParameterTypes();
                    if (params.length == 2 && BlockEntity.class.isAssignableFrom(params[0])
                            && params[1] == ChestType.class
                            && Material.class.isAssignableFrom(candidate.getReturnType())) {
                        candidate.setAccessible(true);
                        return candidate;
                    }
                }
                cursor = cursor.getSuperclass();
            }
            return null;
        });
        if (method == null) {
            return null;
        }
        try {
            Object material = method.invoke(target, fake, ChestType.SINGLE);
            return material instanceof Material mat ? mat : null;
        } catch (Throwable ignored) {
            return null;
        }
    }

    @Nullable
    private static Material materialFromAtlasGuess(BlockState mimic) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(mimic.getBlock());
        String path = id.getPath();
        String wood = path
                .replace("lootr_", "")
                .replace("_trapped_chest", "")
                .replace("trapped_", "")
                .replace("_chest", "")
                .replace("chest_", "");
        boolean trapped = path.contains("trapped");
        String kind = trapped ? "trap" : "normal";

        ResourceLocation[] candidates = new ResourceLocation[]{
                new ResourceLocation(id.getNamespace(), "entity/chest/" + path),
                new ResourceLocation(id.getNamespace(), "entity/chest/" + wood),
                new ResourceLocation(id.getNamespace(), "model/chest/" + wood + "/" + wood),
                new ResourceLocation(id.getNamespace(), "model/chest/" + path),
                new ResourceLocation("quark_variant_chests/" + wood + "/" + kind),
                new ResourceLocation(id.getNamespace() + "_variant_chests/" + wood + "/" + kind)
        };
        for (ResourceLocation sprite : candidates) {
            Material material = new Material(Sheets.CHEST_SHEET, sprite);
            try {
                // Touch sprite; missing atlas entries throw or return missing texture.
                if (material.sprite() != null && !isMissingSprite(material)) {
                    return material;
                }
            } catch (Throwable ignored) {
            }
        }
        return null;
    }

    private static boolean isMissingSprite(Material material) {
        ResourceLocation name = material.sprite().contents().name();
        return name.getPath().contains("missing");
    }

    @Nullable
    private static ResourceLocation directTextureGuess(BlockState mimic) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(mimic.getBlock());
        ResourceLocation cached = TEXTURE_CACHE.get(id);
        if (cached != null) {
            return cached.equals(NO_TEXTURE) ? null : cached;
        }
        ResourceManager resources = Minecraft.getInstance().getResourceManager();
        String path = id.getPath();
        ResourceLocation[] candidates = new ResourceLocation[]{
                new ResourceLocation(id.getNamespace(), "textures/model/" + path + ".png"),
                new ResourceLocation(id.getNamespace(), "textures/entity/chest/" + path + ".png"),
                new ResourceLocation(id.getNamespace(), "textures/entity/tiles/" + path + ".png"),
                new ResourceLocation(id.getNamespace(), "textures/block/chest/" + path + ".png")
        };
        for (ResourceLocation texture : candidates) {
            if (resources.getResource(texture).isPresent()) {
                TEXTURE_CACHE.put(id, texture);
                return texture;
            }
        }
        TEXTURE_CACHE.put(id, NO_TEXTURE);
        return null;
    }

    private static Field[] fieldsOf(Class<?> type) {
        return FIELD_CACHE.computeIfAbsent(type, cls -> {
            java.util.ArrayList<Field> all = new java.util.ArrayList<>();
            Class<?> cursor = cls;
            while (cursor != null && cursor != Object.class) {
                for (Field field : cursor.getDeclaredFields()) {
                    all.add(field);
                }
                cursor = cursor.getSuperclass();
            }
            return all.toArray(Field[]::new);
        });
    }

    private static void setFloat(Object target, String name, float value) throws Exception {
        for (Field field : fieldsOf(target.getClass())) {
            if (field.getName().equals(name) && (field.getType() == float.class || field.getType() == Float.class)) {
                field.setAccessible(true);
                field.setFloat(target, value);
                return;
            }
        }
    }

    private static void setBoolean(Object target, String name, boolean value) throws Exception {
        for (Field field : fieldsOf(target.getClass())) {
            if (field.getName().equals(name) && (field.getType() == boolean.class || field.getType() == Boolean.class)) {
                field.setAccessible(true);
                field.setBoolean(target, value);
                return;
            }
        }
    }
}
