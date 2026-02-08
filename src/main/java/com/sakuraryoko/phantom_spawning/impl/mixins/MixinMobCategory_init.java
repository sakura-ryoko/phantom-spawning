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

import java.util.Objects;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.sakuraryoko.corelib.api.util.MathUtils;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptions;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptionsLimits;

@Mixin(value = MobCategory.class, priority = 900)
@ApiStatus.Internal
public class MixinMobCategory_init
{
	@Mutable @Shadow @Final private int max;

	@ModifyConstant(method = "<clinit>",
	                constant = @Constant(intValue = 15, ordinal = 0))
	private static int ps$overrideAmbientMax_Init(int value)
	{
		BatOptions opts = ConfigWrap.batOpt();

		if (opts.enableBatConfig && opts.setPerChunkAmbientMobCap != value)
		{
			return MathUtils.clamp(opts.setPerChunkAmbientMobCap, BatOptionsLimits.MIN_CAP, BatOptionsLimits.MAX_CAP);
		}

		return value;
	}

	@Inject(method = "<init>", at = @At("TAIL"))
	//#if MC >= 1.16.5
	//$$ private void ps$overrideAmbientMax_Init(String string, int i, String name, int max, boolean isFriendly, boolean isPersistent, int despawnDistance, CallbackInfo ci)
	//#else
	private void ps$overrideAmbientMax_Init(String string, int i, String name, int max, boolean isFriendly, boolean isPersistent, CallbackInfo ci)
	//#endif
	{
		BatOptions opts = ConfigWrap.batOpt();

		if (Objects.equals(name.toLowerCase(), "ambient") &&
			opts.enableBatConfig && opts.setPerChunkAmbientMobCap != max)
		{
			this.max = MathUtils.clamp(opts.setPerChunkAmbientMobCap, BatOptionsLimits.MIN_CAP, BatOptionsLimits.MAX_CAP);
		}
	}
}
