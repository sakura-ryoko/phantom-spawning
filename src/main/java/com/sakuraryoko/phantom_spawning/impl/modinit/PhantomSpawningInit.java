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

package com.sakuraryoko.phantom_spawning.impl.modinit;

import org.jetbrains.annotations.ApiStatus;

import com.sakuraryoko.corelib.api.modinit.IModInitDispatcher;
import com.sakuraryoko.corelib.api.modinit.ModInitData;
import com.sakuraryoko.corelib.api.text.ITextHandler;
import com.sakuraryoko.corelib.impl.commands.CommandManager;
import com.sakuraryoko.corelib.impl.config.ConfigManager;
import com.sakuraryoko.corelib.impl.text.BuiltinTextHandler;
import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.Reference;
import com.sakuraryoko.phantom_spawning.impl.commands.BatSpawningCommand;
import com.sakuraryoko.phantom_spawning.impl.commands.PhantomSpawningAdminCommand;
import com.sakuraryoko.phantom_spawning.impl.commands.PhantomSpawningCommand;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.PhantomSpawningConfigHandler;

@ApiStatus.Internal
public class PhantomSpawningInit implements IModInitDispatcher
{
	private static final PhantomSpawningInit INSTANCE = new PhantomSpawningInit();
	public static PhantomSpawningInit getInstance() { return INSTANCE; }

	private final ModInitData MOD_DATA;
	private boolean INIT = false;

	private PhantomSpawningInit()
	{
		this.MOD_DATA = new ModInitData(Reference.MOD_ID);
		this.MOD_DATA.setTextHandler(this.getTextHandler());
	}

	@Override
	public ModInitData getModInit()
	{
		return this.MOD_DATA;
	}

	@Override
	public String getModId()
	{
		return Reference.MOD_ID;
	}

	@Override
	public ITextHandler getTextHandler()
	{
		return BuiltinTextHandler.getInstance();
	}

	@Override
	public boolean isDebug()
	{
		if (PhantomSpawningConfigHandler.getInstance().isLoaded())
		{
			return ConfigWrap.mainOpt().phantomDebug;
		}

		return Reference.DEBUG;
	}

	@Override
	public boolean isInitComplete()
	{
		return this.INIT;
	}

	@Override
	public void onModInit()
	{
		PhantomSpawningMod.debugLog("Initializing Mod.");
		for (String s : this.getBasic(ModInitData.BASIC_INFO))
		{
			PhantomSpawningMod.LOGGER.info(s);
		}

		PhantomSpawningMod.debugLog("Config Initializing.");
		ConfigManager.getInstance().registerConfigDispatcher(PhantomSpawningConfigHandler.getInstance());

		PhantomSpawningMod.debugLog("Command(s) Initializing.");
		CommandManager.getInstance().registerCommandHandler(PhantomSpawningCommand.getInstance());
		CommandManager.getInstance().registerCommandHandler(PhantomSpawningAdminCommand.getInstance());
		CommandManager.getInstance().registerCommandHandler(BatSpawningCommand.getInstance());

		this.INIT = true;
	}
}
