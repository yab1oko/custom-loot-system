package cts.yblk.cts.registry;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import cts.yblk.cts.Cts;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Cts.MODID);

    public static final RegistryObject<SoundEvent> WOOD_SOUND = register("wood_sound");
    public static final RegistryObject<SoundEvent> ZIP = register("zip");
    public static final RegistryObject<SoundEvent> CANSTASH = register("canstash");
    public static final RegistryObject<SoundEvent> BOXES = register("boxes");
    public static final RegistryObject<SoundEvent> CLOTH = register("cloth");
    public static final RegistryObject<SoundEvent> TOOLBOX = register("toolbox");
    public static final RegistryObject<SoundEvent> WASTE_DRUM = register("waste_drum");
    public static final RegistryObject<SoundEvent> SAFE = register("safe");
    public static final RegistryObject<SoundEvent> METAL_SCRAP = register("metal_scrap");
    public static final RegistryObject<SoundEvent> PLASTIC = register("plastic");
    public static final RegistryObject<SoundEvent> PAPER = register("paper");
    public static final RegistryObject<SoundEvent> DRESSER = register("dresser");
    public static final RegistryObject<SoundEvent> DRAWER = register("drawer");
    public static final RegistryObject<SoundEvent> CACHE = register("cache");
    public static final RegistryObject<SoundEvent> NEST = register("nest");
    public static final RegistryObject<SoundEvent> SHELVING = register("shelving");
    public static final RegistryObject<SoundEvent> CASH_REGISTER = register("cash_register");
    public static final RegistryObject<SoundEvent> TOILET = register("toilet");
    public static final RegistryObject<SoundEvent> PLASTIC_LID = register("plastic_lid");
    public static final RegistryObject<SoundEvent> MICROWAVE = register("microwave");
    public static final RegistryObject<SoundEvent> OPEN = register("open");
    public static final RegistryObject<SoundEvent> SEARCHING = register("searching");
    /** Heard up to 200 blocks when an airdrop entity appears. */
    public static final RegistryObject<SoundEvent> AIRDROP = SOUND_EVENTS.register("airdrop",
            () -> SoundEvent.createFixedRangeEvent(new ResourceLocation(Cts.MODID, "airdrop"), 200.0F));
    public static final RegistryObject<SoundEvent> SCREWDRIVER = register("screwdriver");
    public static final RegistryObject<SoundEvent> VENT_OPEN = register("vent_open");

    private ModSounds() {
    }

    private static RegistryObject<SoundEvent> register(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Cts.MODID, name)));
    }
}
