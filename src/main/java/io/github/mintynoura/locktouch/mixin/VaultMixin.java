package io.github.mintynoura.locktouch.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import io.github.mintynoura.locktouch.Locktouch;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.VaultBlock;
import net.minecraft.world.level.block.entity.vault.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(VaultBlockEntity.Server.class)
public abstract class VaultMixin {
    @Shadow
    private static boolean canEjectReward(VaultConfig vaultConfig, VaultState vaultState) {
        return false;
    }

    @Shadow
    private static List<ItemStack> resolveItemsToEject(ServerLevel serverLevel, VaultConfig vaultConfig, BlockPos blockPos, Player player, ItemStack itemStack) {
        return null;
    }

    @Shadow
    private static void unlock(ServerLevel serverLevel, BlockState blockState, BlockPos blockPos, VaultConfig vaultConfig, VaultServerData vaultServerData, VaultSharedData vaultSharedData, List<ItemStack> list) {
    }

    @WrapOperation(method = "tryInsertKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;consume(ILnet/minecraft/world/entity/LivingEntity;)V"))
    private static void locktouch$recycleKey(ItemStack instance, int i, LivingEntity livingEntity, Operation<Void> original) {
        if (livingEntity.getAttributeValue(Locktouch.RECYCLE_KEY_CHANCE) > 0) {
            if (livingEntity.getOffhandItem().is(Locktouch.LOCKPICK_CHARM)) livingEntity.getOffhandItem().hurtAndBreak(1, livingEntity, EquipmentSlot.OFFHAND);
            if (livingEntity.level().random.nextFloat() > livingEntity.getAttributeValue(Locktouch.RECYCLE_KEY_CHANCE)) {
                original.call(instance, i, livingEntity);
            } else if (livingEntity.level() instanceof ServerLevel serverLevel) {
                serverLevel.playSound(null, livingEntity.blockPosition(), Locktouch.KEY_RECYCLED_SOUND, SoundSource.PLAYERS);
                Vec3 look = livingEntity.getLookAngle();
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, livingEntity.getX() + look.x, livingEntity.getY() + 1, livingEntity.getZ() + look.z, 1, 0, 0, 0, 0);
            }
        } else original.call(instance, i, livingEntity);
    }

    @Inject(method = "tryInsertKey", at = @At("HEAD"))
    private static void locktouch$lockpick(
            ServerLevel serverLevel,
            BlockPos blockPos,
            BlockState blockState,
            VaultConfig vaultConfig,
            VaultServerData vaultServerData,
            VaultSharedData vaultSharedData,
            Player player,
            ItemStack itemStack,
            CallbackInfo ci) {
        if (canEjectReward(vaultConfig, blockState.getValue(VaultBlock.STATE))) {
            if ((itemStack.is(Locktouch.LOCKPICK) && blockState.getValue(BlockStateProperties.OMINOUS)) || (itemStack.is(Locktouch.DIAMOND_LOCKPICK) && !blockState.getValue(BlockStateProperties.OMINOUS))) {
                serverLevel.playSound(null, blockPos, SoundEvents.VAULT_INSERT_ITEM_FAIL, SoundSource.BLOCKS);
            }
            if ((itemStack.is(Locktouch.LOCKPICK) && !blockState.getValue(BlockStateProperties.OMINOUS)) || (itemStack.is(Locktouch.DIAMOND_LOCKPICK) && blockState.getValue(BlockStateProperties.OMINOUS))) {
                List<ItemStack> list = resolveItemsToEject(serverLevel, vaultConfig, blockPos, player, itemStack);
                if (!list.isEmpty()) {
                    Vec3 look = player.getLookAngle();
                    player.awardStat(Stats.ITEM_USED.get(itemStack.getItem()));
                    if (player.getOffhandItem().is(Locktouch.LOCKPICK_CHARM))
                        player.getOffhandItem().hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
                    if (player.level().random.nextFloat() > player.getAttributeValue(Locktouch.RECYCLE_LOCKPICK_CHANCE)) {
                        itemStack.consume(1, player);
                        serverLevel.playSound(null, player.blockPosition(), SoundEvents.ITEM_BREAK.value(), SoundSource.PLAYERS);
                        serverLevel.sendParticles(new ItemParticleOption(ParticleTypes.ITEM, itemStack), player.getX() + look.x, player.getY() + 1, player.getZ() + look.z, 4, 0, 0, 0, 0);
                    } else {
                        serverLevel.playSound(null, player.blockPosition(), Locktouch.LOCKPICK_RECYCLED_SOUND, SoundSource.PLAYERS);
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX() + look.x, player.getY() + 1, player.getZ() + look.z, 1, 0, 0, 0, 0);
                    }
                    if (player.level().random.nextFloat() < player.getAttributeValue(Locktouch.LOCKPICK_SUCCESS_CHANCE)) {
                        unlock(serverLevel, blockState, blockPos, vaultConfig, vaultServerData, vaultSharedData, list);
                        vaultServerData.addToRewardedPlayers(player);
                    }
                }
            }
        }
    }

    @ModifyReturnValue(method = "isValidToInsert", at = @At("RETURN"))
    private static boolean locktouch$allowLockpick(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original || itemStack.is(Locktouch.LOCKPICK) || itemStack.is(Locktouch.DIAMOND_LOCKPICK);
    }

    @ModifyExpressionValue(method = "tryInsertKey", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/vault/VaultBlockEntity$Server;canEjectReward(Lnet/minecraft/world/level/block/entity/vault/VaultConfig;Lnet/minecraft/world/level/block/entity/vault/VaultState;)Z"))
    private static boolean locktouch$removeLockpickCondition(boolean original, @Local(argsOnly = true) ItemStack itemStack) {
        return original && !itemStack.is(Locktouch.LOCKPICK) && !itemStack.is(Locktouch.DIAMOND_LOCKPICK);
    }
}