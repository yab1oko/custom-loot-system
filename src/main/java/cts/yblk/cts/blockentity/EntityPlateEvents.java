package cts.yblk.cts.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import cts.yblk.cts.Cts;

@Mod.EventBusSubscriber(modid = Cts.MODID)
public final class EntityPlateEvents {
    private EntityPlateEvents() {
    }

    @SubscribeEvent
    public static void onMobDeath(LivingDeathEvent event) {
        Entity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        CompoundTag data = entity.getPersistentData();
        if (!data.contains(EntityPlateBlockEntity.ENTITY_TAG)) {
            return;
        }
        BlockPos pos = BlockPos.of(data.getLong(EntityPlateBlockEntity.ENTITY_TAG));
        if (entity.level().getBlockEntity(pos) instanceof EntityPlateBlockEntity plate) {
            plate.onSpawnedMobDeath(entity.getUUID());
        }
    }
}
