package dev.catman.emotecraftfix.mixin;

import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

/** EMF can replace player cuboids after BendyLib initially attaches its bend mutator. */
@Pseudo
@Mixin(targets = "dev.kosmx.playerAnim.impl.animation.AnimationApplier", remap = false)
public abstract class PlayerAnimatorAnimationApplierMixin {
    @Inject(method = "updatePart", at = @At("HEAD"), remap = false)
    private void emotecraft$restoreBendMutator(String partName, ModelPart part, CallbackInfo ci) {
        if (part == null) return;
        Direction direction = switch (partName == null ? "" : partName.replace("_", "").toLowerCase(Locale.ROOT)) {
            case "torso", "body" -> Direction.DOWN;
            case "leftarm", "rightarm", "leftleg", "rightleg" -> Direction.UP;
            default -> null;
        };
        if (direction == null) return;
        try {
            ModelPartAccessor.optionalGetCuboid(part, 0).ifPresent(cuboid -> {
                if (!cuboid.hasMutator("bend")) {
                    cuboid.registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(direction).build(data));
                }
            });
        } catch (RuntimeException ignored) {
            // Leave unsupported custom cuboids untouched; BendHelper below skips them safely.
        }
    }
}
