package dev.catman.emotecraftfix.mixin;

import dev.kosmx.playerAnim.core.impl.AnimationProcessor;
import dev.kosmx.playerAnim.core.util.SetableSupplier;
import dev.kosmx.playerAnim.impl.animation.IBendHelper;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.impl.BendableCuboid;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Copies active bend keyframes onto armor geometry before it is drawn. */
@Mixin(targets = "net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer")
public abstract class ArmorModelBendMixin {
    private static final String RENDER_ARMOR_PIECE = "renderArmorPiece(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/entity/EquipmentSlot;ILnet/minecraft/client/model/HumanoidModel;FFFFFF)V";

    @Inject(method = RENDER_ARMOR_PIECE, at = @At("TAIL"), remap = false)
    private void emotecraft$clearArmorBendsAfterRender(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                                        net.minecraft.client.renderer.MultiBufferSource buffer,
                                                        net.minecraft.world.entity.LivingEntity entity,
                                                        net.minecraft.world.entity.EquipmentSlot slot,
                                                        int light,
                                                        net.minecraft.client.model.HumanoidModel<?> armorModel,
                                                        float limbSwing, float limbSwingAmount, float partialTick,
                                                        float ageInTicks, float netHeadYaw, float headPitch,
                                                        CallbackInfo ci) {
        clear(armorModel.body);
        clear(armorModel.rightArm);
        clear(armorModel.leftArm);
        clear(armorModel.rightLeg);
        clear(armorModel.leftLeg);
    }

    @Inject(method = RENDER_ARMOR_PIECE, at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/HumanoidModel;copyPropertiesTo(Lnet/minecraft/client/model/HumanoidModel;)V", shift = At.Shift.AFTER), remap = false)
    private void emotecraft$applyBendsToArmor(com.mojang.blaze3d.vertex.PoseStack poseStack,
                                              net.minecraft.client.renderer.MultiBufferSource buffer,
                                              net.minecraft.world.entity.LivingEntity entity,
                                              net.minecraft.world.entity.EquipmentSlot slot,
                                              int light,
                                              net.minecraft.client.model.HumanoidModel<?> armorModel,
                                              float limbSwing, float limbSwingAmount, float partialTick,
                                              float ageInTicks, float netHeadYaw, float headPitch,
                                              CallbackInfo ci) {
        if (!(armorModel instanceof dev.kosmx.playerAnim.impl.IMutableModel mutableModel)) return;
        SetableSupplier<AnimationProcessor> supplier = mutableModel.getEmoteSupplier();
        AnimationProcessor animation = supplier == null ? null : supplier.get();
        boolean active = animation != null && animation.isActive();

        // Armor model instances are reused. Always write the current bend (including
        // zero when no emote is active), otherwise the last emote pose sticks on armor.
        apply(armorModel.body, active ? combine(animation, "torso", "body") : zeroBend(), Direction.DOWN);
        apply(armorModel.rightArm, active ? animation.getBend("rightArm") : zeroBend(), Direction.UP);
        apply(armorModel.leftArm, active ? animation.getBend("leftArm") : zeroBend(), Direction.UP);
        apply(armorModel.rightLeg, active ? animation.getBend("rightLeg") : zeroBend(), Direction.UP);
        apply(armorModel.leftLeg, active ? animation.getBend("leftLeg") : zeroBend(), Direction.UP);
    }

    private static dev.kosmx.playerAnim.core.util.Pair<Float, Float> zeroBend() {
        return new dev.kosmx.playerAnim.core.util.Pair<>(0F, 0F);
    }

    private static void clear(ModelPart part) {
        IBendHelper.INSTANCE.bend(part, (dev.kosmx.playerAnim.core.util.Pair<Float, Float>) null);
    }

    private static dev.kosmx.playerAnim.core.util.Pair<Float, Float> combine(
            AnimationProcessor animation, String first, String second) {
        var a = animation.getBend(first);
        var b = animation.getBend(second);
        return new dev.kosmx.playerAnim.core.util.Pair<>(a.getLeft() + b.getLeft(), a.getRight() + b.getRight());
    }

    private static void apply(ModelPart part, dev.kosmx.playerAnim.core.util.Pair<Float, Float> bend, Direction direction) {
        ModelPartAccessor.optionalGetCuboid(part, 0).ifPresent(cuboid -> {
            if (!cuboid.hasMutator("bend")) {
                cuboid.registerMutator("bend", data -> new BendableCuboid.Builder().setDirection(direction).build(data));
            }
        });
        IBendHelper.INSTANCE.bend(part, bend);
    }
}
