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

package com.sakuraryoko.phantom_spawning.impl.events;

import java.net.SocketAddress;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.sakuraryoko.corelib.api.events.IPlayerEventsDispatch;
import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.player.PlayerManager;

@ApiStatus.Internal
public class PlayerEventsHandler implements IPlayerEventsDispatch
{
	private static final PlayerEventsHandler INSTANCE = new PlayerEventsHandler();
	public static PlayerEventsHandler getInstance() { return INSTANCE; }

	@Override
	public void onConnection(SocketAddress addr, GameProfile profile, Component result)
	{
		// NO-OP
	}

	@Override
	public void onCreatePlayer(ServerPlayer player, GameProfile profile)
	{
		PlayerManager.getInstance().syncProfile(profile);
	}

	@Override
	public void onPlayerJoinPre(ServerPlayer player, Connection connection)
	{
		// NO-OP
	}

	@Override
	public void onPlayerJoinPost(ServerPlayer player, Connection connection)
	{
		// NO-OP
	}

	@Override
	public void onPlayerRespawn(ServerPlayer newPlayer)
	{
		PlayerManager.getInstance().syncProfile(newPlayer.getGameProfile());
	}

	@Override
	public void onPlayerLeave(ServerPlayer player)
	{
		// NO-OP
	}

	@Override
	public void onDisconnectAll()
	{
		// NO-OP
	}

	@Override
	public void onSetViewDistance(int distance)
	{
		// NO-OP
	}

	@Override
	public void onSetSimulationDistance(int distance)
	{
		// NO-OP
	}

	public Integer onCheckBypassInsomnia(@Nullable ServerPlayer player, Integer currentValue)
	{
		if (player == null)
		{
			return currentValue;
		}

		if (!PlayerManager.getInstance().getPhantomStatus(player.getGameProfile()))
		{
			// Phantoms can only Spawn when the value is over 72000; and then has a random chance to hit.
			if (currentValue >= 72000)
			{
				if (ConfigWrap.mainOpt().phantomDebug)
				{
					PhantomSpawningMod.LOGGER.info("[DISABLED] Player: '{}' may have been spared from phantom spawns. [{} -> 1]", player.getName().getString(), currentValue);
				}

				return 1;
			}

			if (ConfigWrap.mainOpt().phantomDebug)
			{
				PhantomSpawningMod.LOGGER.info("[DISABLED] Player: '{}' -- Current Value: [{}]", player.getName().getString(), currentValue);
			}
		}
		else if (ConfigWrap.mainOpt().phantomDebug)
		{
			PhantomSpawningMod.LOGGER.info("[ENABLED] Player: '{}' -- Current Value: [{}]", player.getName().getString(), currentValue);
		}

		return currentValue;
	}
}
