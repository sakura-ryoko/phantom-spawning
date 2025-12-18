/*
 * This file is part of the Phantom Spawning project, licensed under the
 * GNU Lesser General Public License v3.0
 *
 * Copyright (C) 2025  Sakura Ryoko and contributors
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

@ApiStatus.Internal
public class MainOptions implements IConfigOption
{
	public int permission_level;
	public int permission_level_admin;
	public boolean phantomDebug;

	public MainOptions()
	{
		this.defaults();
	}

	@Override
	public void defaults()
	{
		this.permission_level = 0;
		this.permission_level_admin = 3;
		this.phantomDebug = false;
	}

	@Override
	public MainOptions copy(IConfigOption opt)
	{
		MainOptions opts = (MainOptions) opt;
		this.permission_level = opts.permission_level;
		this.permission_level_admin = opts.permission_level_admin;
		this.phantomDebug = opts.phantomDebug;

		return this;
	}
}
