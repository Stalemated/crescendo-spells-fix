package com.stalemated.crescendospellsfix.mixin;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import static com.stalemated.crescendospellsfix.CrescendoSpellsFix.LOGGER;

@Mixin(value = CrossbowItem.class, priority = 2000)
public class CrescendoSpellCompatMixin {

	@Unique
	private static final ThreadLocal<String> attached_spell = new ThreadLocal<>();

	@Inject(method = "use", at = @At("HEAD"))
	private void captureSpellOnUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		ItemStack stack = user.getStackInHand(hand);

		// If the item contains the tag, capture the spell and store it
		if (stack.hasNbt() && stack.getNbt() != null && stack.getNbt().contains("se_spell")) {
			attached_spell.set(stack.getNbt().getString("se_spell"));
			LOGGER.info("Captured spell before use: {}", attached_spell.get());
		} else {
			attached_spell.remove();
		}
	}

	@Inject(method = "use", at = @At("RETURN"))
	private void restoreSpellOnUse(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
		ItemStack stack = user.getStackInHand(hand);
		String spell = attached_spell.get();

		if (spell != null && stack.hasNbt() && stack.getNbt() != null) {
			NbtCompound nbt = stack.getNbt();

			// If Zenith's tag is still active, restore the spell
			if (nbt.contains("shots")) {
				nbt.putString("se_spell", spell);
				LOGGER.info("Restored spell {} due to Crescendo active. NBT: {}", spell, nbt);
			} else {
				LOGGER.info("No shots tag found. Letting spell clear naturally.");
			}
		}

		attached_spell.remove();
	}
}