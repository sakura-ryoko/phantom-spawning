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
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import com.mojang.authlib.GameProfile;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;

import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.PlayerOptions;

@ApiStatus.Internal
public class PlayerManager
{
	private static final PlayerManager INSTANCE = new PlayerManager();
	public static PlayerManager getInstance() { return INSTANCE; }

	private final HashMap<UUID, Boolean> playerMap;
	private final HashMap<UUID, Boolean> debugMap;

	private PlayerManager()
	{
		this.playerMap = new HashMap<>();
		this.debugMap = new HashMap<>();
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

	public void syncFromConfig(PlayerOptions opt)
	{
		this.addOrUpdateProfile(ProfileWrap.profile(opt.uuid, opt.name), opt.phantomSpawning);
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
			this.checkOrUpdateFromConfig(profile);
		}

		this.debugMap.put(uuid, false);

		if (ConfigWrap.mainOpt().phantomDebug)
		{
			PhantomSpawningMod.LOGGER.warn("addOrUpdateProfile: player: ['{}'/{}] status: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), toggle);
		}
	}

	private void checkOrUpdateFromConfig(GameProfile profile)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		UUID uuid = ProfileWrap.id(profile);
		boolean status = this.getPhantomStatus(uuid);

		for (PlayerOptions opt : config)
		{
			if (opt.uuid.equals(uuid) && opt.phantomSpawning != status)
			{
				this.setPhantomStatus(profile, opt.phantomSpawning);
				return;
			}
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

		if (ConfigWrap.mainOpt().phantomDebug)
		{
			PhantomSpawningMod.LOGGER.warn("addConfig: player: ['{}'/{}]", ProfileWrap.name(profile), ProfileWrap.id(profile));
		}
	}

	private void setConfig(GameProfile profile, boolean toggle)
	{
		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		boolean dirty = false;

		for (PlayerOptions entry : config)
		{
			if (entry.uuid.equals(ProfileWrap.id(profile)) &&
				entry.phantomSpawning != toggle)
			{
				entry.phantomSpawning = toggle;
				dirty = true;
			}
		}

		if (dirty)
		{
			ConfigWrap.players().clear();

			for (PlayerOptions entry : config)
			{
				ConfigWrap.players().add(new PlayerOptions(entry));
			}
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

	public boolean getPhantomStatus(@NotNull UUID uuid)
	{
		if (this.playerMap.containsKey(uuid))
		{
			return this.playerMap.get(uuid);
		}

		this.playerMap.put(uuid, true);
		return true;
	}

	public boolean getDebugStatus(@NotNull GameProfile profile)
	{
		UUID uuid = ProfileWrap.id(profile);

		if (this.debugMap.containsKey(uuid))
		{
			return this.debugMap.get(uuid);
		}

		return false;
	}

	public boolean getDebugStatus(@NotNull UUID uuid)
	{
		if (this.debugMap.containsKey(uuid))
		{
			return this.debugMap.get(uuid);
		}

		return false;
	}

	public Pair<Boolean, Integer> getPlayerInfo(@NotNull ServerPlayer player)
	{
		return Pair.of(
				this.getPhantomStatus(player.getGameProfile()),
				Mth.clamp(player.getStats()
				                .getValue(Stats.CUSTOM.get(Stats.TIME_SINCE_REST)), 1, Integer.MAX_VALUE)
		);
	}

	public void setPhantomStatus(@NotNull GameProfile profile, boolean toggle)
	{
		this.addOrUpdateProfile(profile, toggle);
		this.setConfig(profile, toggle);

		if (ConfigWrap.mainOpt().phantomDebug || this.getDebugStatus(profile))
		{
			PhantomSpawningMod.LOGGER.warn("setPhantomStatus: player: ['{}'/{}] status: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), toggle);
		}
	}

	public void setDebugStatus(@NotNull GameProfile profile, boolean toggle)
	{
		UUID uuid = ProfileWrap.id(profile);

		this.debugMap.put(uuid, toggle);

		if (ConfigWrap.mainOpt().phantomDebug || this.getDebugStatus(uuid))
		{
			PhantomSpawningMod.LOGGER.warn("setDebugStatus: player: ['{}'/{}] status: {}", ProfileWrap.name(profile), ProfileWrap.id(profile), toggle);
		}
	}

	public void setPhantomStatusAll(boolean toggle)
	{
		for (UUID uuid : this.playerMap.keySet())
		{
			this.playerMap.replace(uuid, toggle);
		}

		List<PlayerOptions> config = new ArrayList<>(ConfigWrap.players());
		boolean dirty = false;

		for (PlayerOptions entry : config)
		{
			if (entry.phantomSpawning != toggle)
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

		if (ConfigWrap.mainOpt().phantomDebug)
		{
			PhantomSpawningMod.LOGGER.warn("setPhantomStatusAll: status: {}", toggle);
		}
	}

	public void setDebugStatusAll(boolean toggle)
	{
		for (UUID uuid : this.playerMap.keySet())
		{
			this.debugMap.put(uuid, toggle);
		}

		if (ConfigWrap.mainOpt().phantomDebug)
		{
			PhantomSpawningMod.LOGGER.warn("setDebugStatusAll: status: {}", toggle);
		}
	}
}
