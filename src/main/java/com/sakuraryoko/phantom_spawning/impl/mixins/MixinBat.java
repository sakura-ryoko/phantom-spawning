/*
 * This file is part of the Phantom Spawning project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2026  Sakura Ryoko and contributors
 *
 * Phantom Spawning is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Phantom Spawning is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Phantom Spawning.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.sakuraryoko.phantom_spawning.impl.mixins;

import java.time.LocalDate;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.TestOnly;

//#if MC >= 1.21.11
//$$ import net.minecraft.util.RandomSource;
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
//$$ import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
//$$ import org.spongepowered.asm.mixin.injection.At;
//#endif
import net.minecraft.world.entity.ambient.Bat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

import com.sakuraryoko.corelib.api.util.MathUtils;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptions;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptionsLimits;

@Mixin(value = Bat.class, priority = 1010)
@ApiStatus.Internal
public abstract class MixinBat
{
	//#if MC >= 1.21.11
	//$$ @WrapOperation(method = "checkBatSpawnRules",
					   //$$ at = @At(value = "INVOKE",
						             //$$ target = "Lnet/minecraft/util/RandomSource;nextInt(I)I"))
	//$$ private static int ps$modifyBatLightLevel_Normal(RandomSource instance, int i, Operation<Integer> original)
	//#else
	@ModifyConstant(method = "checkBatSpawnRules", constant = @Constant(intValue = 4))
	private static int ps$modifyBatLightLevel_Normal(int i)
	//#endif
	{
		BatOptions opts = ConfigWrap.batOpt();
		int adj = i;

		if (opts.enableBatConfig)
		{
			//#if MC >= 1.21.11
			//$$ final boolean isHalloween = opts.halloweenFeature && ps$isWeekOfHalloween();
			//$$ final int pad = isHalloween ? 4 : 1;
			//#else
			final int pad = 1;
			//#endif

			adj = MathUtils.clamp(opts.setBrightnessFactor + pad, BatOptionsLimits.MIN_FACTOR, BatOptionsLimits.MAX_FACTOR);
//			PhantomSpawningMod.LOGGER.warn("[ps$modifyBatLightLevel_Normal:Brightness] orig: [{}], adj: [{}]", value, adj);
		}

		return adj;
	}

	//#if MC >= 1.21.11
	//#else
	@ModifyConstant(method = "checkBatSpawnRules", constant = @Constant(intValue = 7))
	private static int ps$modifyBatLightLevel_Halloween(int value)
	{
		BatOptions opts = ConfigWrap.batOpt();
		int adj = value;

		if (opts.enableBatConfig)
		{
			final int pad = opts.halloweenFeature ? 4 : 1;

			adj = MathUtils.clamp(opts.setBrightnessFactor + pad, BatOptionsLimits.MIN_FACTOR, BatOptionsLimits.MAX_FACTOR);
//			PhantomSpawningMod.LOGGER.warn("[ps$modifyBatLightLevel_Halloween:Brightness] orig: [{}], adj: [{}]", value, adj);
		}

		return adj;
	}

	/**
	 * Override the isHalloween() check
	 * @return If Override is enabled, then return if it's Halloween,
	 * or if the feature is disabled; then override setting it to true.<br>
	 * If the Bat Override feature is disabled; continues to return if it's Halloween as normal using an internal wrapper method.
	 * @author Sakura-Ryoko
	 * @reason Mod Features
	 */
	@Overwrite
	private static boolean isHalloween()
	{
		if (ConfigWrap.batOpt().enableBatConfig &&
			!ConfigWrap.batOpt().halloweenFeature)
		{
			return false;
		}

		return ps$isWeekOfHalloween();
	}
	//#endif

	@TestOnly
//	@Inject(method = "checkBatSpawnRules",
//	        at = @At(value = "INVOKE",
//	                 target = "Ljava/util/Random;nextInt(I)I"))
//	private static void ps$modifyBatLightLevel_RandomTest(EntityType<Bat> entityType, LevelAccessor levelAccessor, MobSpawnType mobSpawnType, BlockPos blockPos, Random random, CallbackInfoReturnable<Boolean> cir,
//	                                                      @Local(ordinal = 0) int i, @Local(ordinal = 1) int j)
//	{
//		PhantomSpawningMod.LOGGER.warn("[ps$modifyBatLightLevel_RandomTest:nextInt()]: i: [{}], j: [{}]", i, j);
//	}
//
//	@Redirect(method = "checkBatSpawnRules",
//	          at = @At(value = "INVOKE",
//	                   target = "Ljava/util/Random;nextInt(I)I"))
//	private static int ps$modifyBatLightLevel_Random(Random instance, int j,
//	                                                 @Local(ordinal = 0) int i,
//	                                                 @Local(argsOnly = true) BlockPos blockPos)
//	{
//		// i = getMaxLocalRawBrightness()
//		// j = brightnessFactor
//		final int result = instance.nextInt(j);
//
//		if (i > result)
//		{
//			PhantomSpawningMod.LOGGER.info("[ps$modifyBatLightLevel_Random:nextInt()]: i: {}, j: {} // Rand: [{}], Pos: [{}] - FAIL", i, j, result, blockPos.toString());
//		}
//		else
//		{
//			PhantomSpawningMod.LOGGER.fatal("[ps$modifyBatLightLevel_Random:nextInt()]: i: {}, j: {} // Rand: [{}], Pos: [{}] - SUCCEED", i, j, result, blockPos.toString());
//		}
//
//		return result;
//	}

	@Unique
	private static boolean ps$isWeekOfHalloween()
	{
		LocalDate now = LocalDate.now();
		int day = now.getDayOfMonth();
		int month = now.getMonthValue();
		return  month == 10 && day >= 20 ||
				month == 11 && day <= 3;
	}
}
