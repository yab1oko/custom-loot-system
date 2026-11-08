package cts.yblk.cts.client;

import dev.kosmx.playerAnim.api.firstPerson.FirstPersonConfiguration;
import dev.kosmx.playerAnim.api.firstPerson.FirstPersonMode;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.data.KeyframeAnimation;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import cts.yblk.cts.Cts;

/**
 * Player Animator wiring — only loaded when {@link SearchAnimation#isAvailable()} is true.
 */
final class SearchAnimationHooks {
    private static final ResourceLocation LAYER_ID = new ResourceLocation(Cts.MODID, "action");

    private SearchAnimationHooks() {
    }

    static void registerFactory() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(
                LAYER_ID,
                42,
                player -> new ModifierLayer<>());
    }

    static void start(Player player, ResourceLocation animId, boolean showHeldItem) {
        if (!(player instanceof AbstractClientPlayer clientPlayer)) {
            return;
        }
        ModifierLayer<IAnimation> layer = layerOf(clientPlayer);
        KeyframeAnimation animation = PlayerAnimationRegistry.getAnimation(animId);
        if (layer == null || animation == null) {
            return;
        }
        // Without RFP: draw TP arms in FP so rummage/chop/mine are visible.
        // With Features Real First Person: RFP already renders that body — PA FP
        // overlay would duplicate arms/items. Pose still applies to the RFP pass.
        FirstPersonMode fpMode = FeaturesRfpCompat.isRealFirstPersonActive()
                ? FirstPersonMode.DISABLED
                : FirstPersonMode.THIRD_PERSON_MODEL;
        KeyframeAnimationPlayer playerAnim = new KeyframeAnimationPlayer(animation)
                .setFirstPersonMode(fpMode)
                .setFirstPersonConfiguration(new FirstPersonConfiguration(
                        true, true, showHeldItem, false));
        layer.replaceAnimationWithFade(
                AbstractFadeModifier.standardFadeIn(4, Ease.INOUTSINE),
                playerAnim);
    }

    static void stop(Player player) {
        if (!(player instanceof AbstractClientPlayer clientPlayer)) {
            return;
        }
        ModifierLayer<IAnimation> layer = layerOf(clientPlayer);
        if (layer == null || layer.getAnimation() == null) {
            return;
        }
        layer.replaceAnimationWithFade(
                AbstractFadeModifier.standardFadeIn(4, Ease.INOUTSINE),
                null);
    }

    @SuppressWarnings("unchecked")
    private static ModifierLayer<IAnimation> layerOf(AbstractClientPlayer player) {
        Object data = PlayerAnimationAccess.getPlayerAssociatedData(player).get(LAYER_ID);
        if (data instanceof ModifierLayer<?> layer) {
            return (ModifierLayer<IAnimation>) layer;
        }
        return null;
    }
}
