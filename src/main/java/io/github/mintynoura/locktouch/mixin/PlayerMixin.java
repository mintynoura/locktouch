package io.github.mintynoura.locktouch.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import io.github.mintynoura.locktouch.Locktouch;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public class PlayerMixin {
    @ModifyReturnValue(method = "createAttributes", at = @At("RETURN"))
    private static AttributeSupplier.Builder locktouch$addAttributes(AttributeSupplier.Builder original) {
        return original
                .add(Locktouch.RECYCLE_KEY_CHANCE)
                .add(Locktouch.RECYCLE_LOCKPICK_CHANCE)
                .add(Locktouch.LOCKPICK_SUCCESS_CHANCE);
    }
}
