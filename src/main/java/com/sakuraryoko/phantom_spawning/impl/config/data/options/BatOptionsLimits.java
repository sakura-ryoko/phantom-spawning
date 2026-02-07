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

@ApiStatus.Internal
@ApiStatus.NonExtendable
public class BatOptionsLimits
{
	// Limit
	public static final int MIN_FACTOR = 0;         // We are calling it a factor, but it's a Function of the light level; but it doesn't "directly" correlate in terms of the spawning rules.
	public static final int MAX_FACTOR = 15;
	public static final int MIN_CAP = 0;            // 0 essentially means disabled ^_^
	public static final int MAX_CAP = 1024;         // A ridiculous max, mostly for fun / testing / pranking your friends

	// Default
	public static final int DEFAULT_FACTOR = 3;     // Bat.checkBatSpawnRules()
	public static final int DEFAULT_CAP = 15;       // MobCategory.AMBIENT.getMaxInstancesPerChunk()
}
