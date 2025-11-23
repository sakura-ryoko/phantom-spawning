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

package com.sakuraryoko.phantom_spawning.impl.player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.PlayerOptions;

@ApiStatus.Internal
public class PlayerManager
{
	private static final PlayerManager INSTANCE = new PlayerManager();
	public static PlayerManager getInstance() { return INSTANCE; }

	private final HashMap<UUID, Boolean> playerMap;

	private PlayerManager()
	{
		this.playerMap = new HashMap<>();
	}

	public void syncProfile(GameProfile profile)
	{
		List<PlayerOptions> config = ConfigWrap.players();
		UUID uuid = ProfileWrap.id(profile);

		for (PlayerOptions opt : config)
		{
			if (opt.uuid.equals(uuid))
			{
				this.addOrUpdateProfile(profile, opt.phantomSpawning);
				return;
			}
		}

		// Doesn't exist in config --> Add
		this.addConfig(profile);
	}

	private void addOrUpdateProfile(GameProfile profile, boolean toggle)
	{
		UUID uuid = ProfileWrap.id(profile);

		if (this.playerMap.containsKey(uuid) && this.playerMap.get(uuid) != toggle)
		{
			this.playerMap.remove(uuid);
			this.playerMap.put(uuid, toggle);
		}
		else if (!this.playerMap.containsKey(uuid))
		{
			this.playerMap.put(uuid, toggle);
		}
	}

	private void addConfig(GameProfile profile)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		boolean exists = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(ProfileWrap.id(profile)))
			{
				exists = true;
			}
		}

		if (!exists)
		{
			ConfigWrap.players().add(PlayerOptions.fromProfile(profile, true));
		}
	}

	private void setConfig(GameProfile profile, boolean toggle)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		boolean dirty = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(ProfileWrap.id(profile)))
			{
				entry.phantomSpawning = toggle;
				dirty = true;
			}
		}

		if (dirty)
		{
			ConfigWrap.players().clear();
			ConfigWrap.players().addAll(config);
		}
	}

	public boolean getPhantomStatus(@NotNull GameProfile profile)
	{
		UUID uuid = ProfileWrap.id(profile);

		if (this.playerMap.containsKey(uuid))
		{
			return this.playerMap.get(uuid);
		}

		this.addOrUpdateProfile(profile, true);
		this.addConfig(profile);

		return true;
	}

	public void setPhantomStatus(@NotNull GameProfile profile, boolean toggle)
	{
		this.addOrUpdateProfile(profile, toggle);
		this.setConfig(profile, toggle);
	}
}
