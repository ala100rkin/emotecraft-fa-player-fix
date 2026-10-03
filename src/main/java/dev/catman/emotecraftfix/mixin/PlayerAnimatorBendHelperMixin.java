package dev.catman.emotecraftfix.mixin;

import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.client.model.geom.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Avoids a render crash if a third-party model still has no BendyLib mutator. */
@Pseudo
@Mixin(targets = "dev.kosmx.playerAnim.impl.animation.BendHelper", remap = false)
public abstract class PlayerAnimatorBendHelperMixin {
    @Inject(method = "bend(Lnet/minecraft/client/model/geom/ModelPart;FF)V", at = @At("HEAD"), cancellable = true, remap = false)
    private void emotecraft$skipMissingBend(ModelPart part, float axis, float rotation, CallbackInfo ci) {
        if (Math.abs(rotation) < 0.0001F) return;
        try {
            boolean unavailable = ModelPartAccessor.optionalGetCuboid(part, 0)
                    .map(cuboid -> !cuboid.hasMutator("bend")
                            || !(cuboid.getAndActivateMutator("bend") instanceof BendableCuboid))
                    .orElse(false);
            if (unavailable) ci.cancel();
        } catch (RuntimeException exception) {
            ci.cancel();
        }
    }
}
