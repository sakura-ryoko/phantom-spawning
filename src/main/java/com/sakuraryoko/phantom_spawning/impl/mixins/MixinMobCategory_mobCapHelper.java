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

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import com.sakuraryoko.corelib.api.util.MathUtils;
import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptions;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptionsLimits;

@Mixin(value = MobCategory.class, priority = 990)
@ApiStatus.Internal
//@Restriction(conflict = @Condition(value = ModIds.carpet))
public class MixinMobCategory_mobCapHelper
{
	@Mutable @Shadow @Final private int max;
	@Shadow @Final private String name;

	@WrapMethod(method = "getMaxInstancesPerChunk()I")
	private int ps$overrideAmbientMax(Operation<Integer> original)
	{
		BatOptions opts = ConfigWrap.batOpt();

		if (this.name.equalsIgnoreCase(MobCategory.AMBIENT.getName()) &&
			opts.enableBatConfig && opts.setPerChunkAmbientMobCap != this.max)
		{
			final int adj = MathUtils.clamp(opts.setPerChunkAmbientMobCap, BatOptionsLimits.MIN_CAP, BatOptionsLimits.MAX_CAP);
			PhantomSpawningMod.LOGGER.warn("[MobCategory_helper]: getMaxInstancesPerChunk() orig: [{}] --> adj: [{}]", this.max, adj);
			this.max = adj;
			return adj;
		}

		return this.max;
	}
}
