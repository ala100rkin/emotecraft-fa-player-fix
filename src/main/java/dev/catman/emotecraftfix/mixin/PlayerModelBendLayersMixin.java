package dev.catman.emotecraftfix.mixin;

import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.impl.IAnimatedPlayer;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Applies Player Animator's bend keyframes to the outer skin layer too. */
@Mixin(value = PlayerModel.class)
public abstract class PlayerModelBendLayersMixin<T extends LivingEntity> {
    @Shadow @Final public ModelPart jacket;
    @Shadow @Final public ModelPart rightSleeve;
    @Shadow @Final public ModelPart leftSleeve;
    @Shadow @Final public ModelPart rightPants;
    @Shadow @Final public ModelPart leftPants;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void emotecraft$applyBendsToOuterSkin(T entity, float limbSwing, float limbSwingAmount,
                                                  float ageInTicks, float netHeadYaw, float headPitch,
                                                  CallbackInfo ci) {
        if (!(entity instanceof Player player) || !(player instanceof IAnimatedPlayer animated)) return;
        AnimationProcessor animation = animated.playerAnimator_getAnimation();
        if (animation == null || !animation.isActive()) return;

        apply(jacket, combine(animation, "torso", "body"), Direction.DOWN);
        apply(rightSleeve, animation.getBend("rightArm"), Direction.UP);
        apply(leftSleeve, animation.getBend("leftArm"), Direction.UP);
        apply(rightPants, animation.getBend("rightLeg"), Direction.UP);
        apply(leftPants, animation.getBend("leftLeg"), Direction.UP);
    }

    private static dev.kosmx.playerAnim.core.util.Pair<Float, Float> combine(
            AnimationProcessor animation, String first, String second) {
        var a = animation.getBend(first);
        var b = animation.getBend(second);
        return new dev.kosmx.playerAnim.core.util.Pair<>(a.getLeft() + b.getLeft(), a.getRight() + b.getRight());
    }

    private static void apply(ModelPart part, dev.kosmx.playerAnim.core.util.Pair<Float, Float> bend, Direction direction) {
        ensureMutator(part, direction);
        IBendHelper.INSTANCE.bend(part, bend);
    }

    private static void ensureMutator(ModelPart part, Direction direction) {
        ModelPartAccessor.optionalGetCuboid(part, 0).ifPresent(cuboid -> {
            if (!cuboid.hasMutator("bend")) {
                cuboid.registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(direction).build(data));
            }
        });
    }
}
