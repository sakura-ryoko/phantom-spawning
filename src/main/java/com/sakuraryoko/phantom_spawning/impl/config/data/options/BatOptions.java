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

package com.sakuraryoko.phantom_spawning.impl.config.data.options;

import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.corelib.api.config.IConfigOption;
import com.sakuraryoko.corelib.api.util.MathUtils;

/**
 * {@link net.minecraft.world.entity.ambient.Bat}
 */
@ApiStatus.Internal
public class BatOptions implements IConfigOption
{
	public int setBrightnessFactor;
	public int setPerChunkAmbientMobCap;                           // MobCategory.AMBIENT.getMaxInstancesPerChunk()
	public boolean enableBatConfig;
	public boolean halloweenFeature;

	public BatOptions()
	{
		this.defaults();
	}

	@Override
	public void defaults()
	{
		this.setBrightnessFactor = BatOptionsLimits.DEFAULT_FACTOR;
		this.setPerChunkAmbientMobCap = BatOptionsLimits.DEFAULT_CAP;
		this.enableBatConfig = false;
		this.halloweenFeature = false;
	}

	@Override
	public BatOptions copy(IConfigOption opt)
	{
		BatOptions opts = (BatOptions) opt;
		this.enableBatConfig = opts.enableBatConfig;
		this.halloweenFeature = opts.halloweenFeature;
		this.setBrightnessFactor = MathUtils.clamp(opts.setBrightnessFactor, BatOptionsLimits.MIN_FACTOR, BatOptionsLimits.MAX_FACTOR);
		this.setPerChunkAmbientMobCap = MathUtils.clamp(opts.setPerChunkAmbientMobCap, BatOptionsLimits.MIN_CAP, BatOptionsLimits.MAX_CAP);

		return this;
	}
}
