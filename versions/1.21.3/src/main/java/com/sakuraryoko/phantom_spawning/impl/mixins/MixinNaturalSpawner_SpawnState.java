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

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.sakuraryoko.corelib.api.util.MathUtils;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptions;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptionsLimits;

@Mixin(value = NaturalSpawner.SpawnState.class)
public class MixinNaturalSpawner_SpawnState
{
	@WrapOperation(method = "canSpawnForCategoryGlobal(Lnet/minecraft/world/entity/MobCategory;)Z",
	               at = @At(value = "INVOKE",
						target = "Lnet/minecraft/world/entity/MobCategory;getMaxInstancesPerChunk()I"))
	private int ps$onCheckMobCap_Spawner(MobCategory instance, Operation<Integer> original)
	{
		final int orig = instance.getMaxInstancesPerChunk();
		BatOptions opts = ConfigWrap.batOpt();
		int adj = orig;

		if (instance.getName().equals(MobCategory.AMBIENT.getName()) &&
			opts.enableBatConfig && opts.setPerChunkAmbientMobCap != orig)
		{
			adj = MathUtils.clamp(opts.setPerChunkAmbientMobCap, BatOptionsLimits.MIN_CAP, BatOptionsLimits.MAX_CAP);
//			PhantomSpawningMod.LOGGER.warn("[SpawnState]: getMaxInstancesPerChunk() orig: [{}] --> adj: [{}]", orig, adj);
		}

		return adj;
	}
}
