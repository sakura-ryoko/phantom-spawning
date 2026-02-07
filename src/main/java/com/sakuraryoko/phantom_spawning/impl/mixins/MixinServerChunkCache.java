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
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.world.entity.MobCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.sakuraryoko.corelib.api.util.MathUtils;
//#if MC >= 1.16.5
//$$ import com.sakuraryoko.corelib.impl.util.MixinDummy;
//#endif
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptions;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptionsLimits;

@Mixin(ServerChunkCache.class)
public class MixinServerChunkCache
{
	@WrapOperation(method = "method_20801(JZ[Lnet/minecraft/world/entity/MobCategory;ZILit/unimi/dsi/fastutil/objects/Object2IntMap;Lnet/minecraft/core/BlockPos;ILnet/minecraft/server/level/ChunkHolder;)V",
	               at = @At(value = "INVOKE",
	                        target = "Lnet/minecraft/world/entity/MobCategory;getMaxInstancesPerChunk()I"))
	private int ps$redirectInstancesPerChunk(MobCategory instance, Operation<Integer> original)
	{
		BatOptions opts = ConfigWrap.batOpt();
		final int orig = instance.getMaxInstancesPerChunk();

		if (Objects.equals(instance.getName(), MobCategory.AMBIENT.getName()) &&
			opts.enableBatConfig && orig != opts.setPerChunkAmbientMobCap)
		{
			return MathUtils.clamp(opts.setPerChunkAmbientMobCap, BatOptionsLimits.MIN_CAP, BatOptionsLimits.MAX_CAP);
		}

		return orig;
	}
}
