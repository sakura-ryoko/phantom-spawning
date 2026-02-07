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

package com.sakuraryoko.phantom_spawning.impl.config;

import java.util.ArrayList;
import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.corelib.api.config.IConfigData;
import com.sakuraryoko.corelib.api.config.IConfigDispatch;
import com.sakuraryoko.corelib.api.time.TimeFormat;
import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.Reference;
import com.sakuraryoko.phantom_spawning.impl.config.data.PhantomSpawningData;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.MainOptions;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.PlayerOptions;
import com.sakuraryoko.phantom_spawning.impl.modinit.PhantomSpawningInit;
import com.sakuraryoko.phantom_spawning.impl.player.PlayerManager;

@ApiStatus.Internal
public class PhantomSpawningConfigHandler implements IConfigDispatch
{
	private static final PhantomSpawningConfigHandler INSTANCE = new PhantomSpawningConfigHandler();
	public static PhantomSpawningConfigHandler getInstance() { return INSTANCE; }
	private final PhantomSpawningData CONFIG = newConfig();
	private final String CONFIG_ROOT = Reference.MOD_ID;
	private final String CONFIG_NAME = Reference.MOD_ID;
	private boolean loaded = false;

	@Override
	public String getConfigRoot()
	{
		return this.CONFIG_ROOT;
	}

	@Override
	public boolean useRootDir()
	{
		return true;
	}

	@Override
	public String getConfigName()
	{
		return this.CONFIG_NAME;
	}

	@Override
	public PhantomSpawningData newConfig()
	{
		return new PhantomSpawningData();
	}

	@Override
	public PhantomSpawningData getConfig()
	{
		return this.CONFIG;
	}

	@Override
	public boolean isLoaded()
	{
		return this.loaded;
	}

	@Override
	public void initConfig()
	{
		// NO-OP
	}

	@Override
	public void onPreLoadConfig()
	{
		this.loaded = false;
	}

	@Override
	public void onPostLoadConfig()
	{
		this.loaded = true;
	}

	@Override
	public void onPreSaveConfig()
	{
		this.loaded = false;
	}

	@Override
	public void onPostSaveConfig()
	{
		this.loaded = true;
	}

	@Override
	public PhantomSpawningData defaults()
	{
		PhantomSpawningData config = this.newConfig();

		PhantomSpawningMod.debugLog("PhantomSpawningConfigHandler#defaults(): Setting default config.");

		// Set default values
		config.config_date = TimeFormat.RFC1123.formatNow(null);
		config.MAIN = new MainOptions();

		// Some of these are possibly in use; but remove them later if they are.
		config.PLAYERS = new ArrayList<>();

		return config;
	}

	@Override
	public PhantomSpawningData update(IConfigData newConfig)
	{
		PhantomSpawningData newConf = (PhantomSpawningData) newConfig;
		PhantomSpawningMod.debugLog("PhantomSpawningConfigHandler#update(): Refresh config.");

		// Refresh
		CONFIG.comment = PhantomSpawningInit.getInstance().getModVersionString() + " Config";
		CONFIG.config_date = TimeFormat.RFC1123.formatNow(null);
		PhantomSpawningMod.debugLog("PhantomSpawningConfigHandler#update(): save_date: {} --> {}", newConf.config_date, CONFIG.config_date);

		// Copy Main/Bats Config
		CONFIG.MAIN.copy(newConf.MAIN);
		CONFIG.BATS.copy(newConf.BATS);

		// Copy Players Config
		CONFIG.PLAYERS.clear();
		newConf.PLAYERS.forEach(
				player ->
						CONFIG.PLAYERS.add(new PlayerOptions(player))
		);      // Deep copy

		return CONFIG;
	}

	@Override
	public void execute(boolean fromInit)
	{
		PhantomSpawningMod.debugLog("PhantomSpawningConfigHandler#execute(): Execute config.");

		if (!fromInit)
		{
			if (CONFIG.MAIN.phantomDebug)
			{
				PhantomSpawningMod.LOGGER.info("Phantom spawning debug is enabled for all players");
			}
			else
			{
				PhantomSpawningMod.LOGGER.info("Phantom spawning debug is disabled for all players");
			}
		}

		// Load data into Player Manager.
		CONFIG.PLAYERS.forEach(
				player ->
						PlayerManager.getInstance().syncFromConfig(player)
		);

		// Do this when the Config gets finalized.
		PhantomSpawningMod.debugLog("PhantomSpawningConfigHandler#execute(): new config_date: {}", CONFIG.config_date);
	}
}
