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

import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

import com.sakuraryoko.corelib.api.config.IConfigOption;
import com.sakuraryoko.phantom_spawning.impl.player.ProfileWrap;

@ApiStatus.Internal
public class PlayerOptions implements IConfigOption
{
	public UUID uuid;
	public String name;
	public boolean phantomSpawning;

	public PlayerOptions()
	{
		this.defaults();
	}

	public PlayerOptions(PlayerOptions other)
	{
		this.defaults();
		this.copy(other);
	}

	@Override
	public void defaults()
	{
		this.uuid = UUID.randomUUID();
		this.name = this.uuid.toString();
		this.phantomSpawning = true;
	}

	@Override
	public PlayerOptions copy(IConfigOption other)
	{
		PlayerOptions opts = (PlayerOptions) other;
		this.uuid = opts.uuid;
		this.name = opts.name;
		this.phantomSpawning = opts.phantomSpawning;

		return this;
	}

	@Override
	public boolean equals(Object o)
	{
		if (o instanceof PlayerOptions)
		{
			PlayerOptions opt = (PlayerOptions) o;

			// Only match the UUID
			return opt.uuid.equals(this.uuid);
		}

		return false;
	}

	public static PlayerOptions fromProfile(@NotNull GameProfile profile)
	{
		return fromProfile(profile, true);
	}

	public static PlayerOptions fromProfile(@NotNull GameProfile profile, boolean toggle)
	{
		PlayerOptions opts = new PlayerOptions();
		opts.uuid = ProfileWrap.id(profile);
		opts.name = ProfileWrap.name(profile);
		opts.phantomSpawning = toggle;
		return opts;
	}
}
