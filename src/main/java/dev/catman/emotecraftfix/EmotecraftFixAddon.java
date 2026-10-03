package dev.catman.emotecraftfix;

import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.api.distmarker.Dist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.function.Function;

@Mod(value = EmotecraftFixAddon.MOD_ID, dist = Dist.CLIENT)
public final class EmotecraftFixAddon {
    public static final String MOD_ID = "emotecraft_fix_animate_texture";
    private static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    private static final String EMF_API = "traben.entity_model_features.EMFAnimationApi";
    private static final String EMOTER_INTERFACE = "io.github.kosmx.emotes.main.mixinFunctions.IPlayerEntity";
    private static Method isPlayingEmote;

    public EmotecraftFixAddon() {
        registerEmfHooks();
    }

    private static void registerEmfHooks() {
        if (!ModList.get().isLoaded("entity_model_features")) {
            LOGGER.info("EMF not found; player bend compatibility remains active without EMF hooks");
            return;
        }
        try {
            Class<?> emoteInterface = Class.forName(EMOTER_INTERFACE, false, EmotecraftFixAddon.class.getClassLoader());
            isPlayingEmote = emoteInterface.getMethod("isPlayingEmote");

            Class<?> emfApi = Class.forName(EMF_API, false, EmotecraftFixAddon.class.getClassLoader());
            Method pause = emfApi.getMethod("registerPauseCondition", Function.class);
            Method vanillaModel = emfApi.getMethod("registerVanillaModelCondition", Function.class);
            Function<Object, Boolean> condition = EmotecraftFixAddon::isEmotingPlayer;
            boolean pauseRegistered = Boolean.TRUE.equals(pause.invoke(null, condition));
            boolean vanillaModelRegistered = Boolean.TRUE.equals(vanillaModel.invoke(null, condition));
            LOGGER.info("Registered EMF/Emotecraft fix hooks (pause={}, vanillaModel={})", pauseRegistered, vanillaModelRegistered);
        } catch (ReflectiveOperationException | LinkageError exception) {
            LOGGER.error("Could not register EMF/Emotecraft compatibility hooks", exception);
        }
    }

    private static boolean isEmotingPlayer(Object entity) {
        if (!(entity instanceof Player) || isPlayingEmote == null) return false;
        try {
            return Boolean.TRUE.equals(isPlayingEmote.invoke(entity));
        } catch (ReflectiveOperationException exception) {
            return false;
        }
    }
}
