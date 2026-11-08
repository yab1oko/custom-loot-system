package cts.yblk.cts.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import cts.yblk.cts.Cts;
import cts.yblk.cts.entity.AirdropEntity;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, Cts.MODID);

    public static final RegistryObject<EntityType<AirdropEntity>> AIRDROP = ENTITIES.register("airdrop",
            () -> EntityType.Builder.<AirdropEntity>of(AirdropEntity::new, MobCategory.MISC)
                    .sized(2.2F, 3.8F)
                    .clientTrackingRange(10)
                    .updateInterval(1)
                    .fireImmune()
                    .build(new ResourceLocation(Cts.MODID, "airdrop").toString()));

    private ModEntities() {
    }
}
