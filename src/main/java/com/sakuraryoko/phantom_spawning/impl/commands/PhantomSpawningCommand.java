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

package com.sakuraryoko.phantom_spawning.impl.commands;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
//#if MC >= 11902
//$$ import net.minecraft.commands.Commands;
//$$ import net.minecraft.commands.CommandBuildContext;
//#endif

import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.Reference;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.modinit.InitWrap;
import com.sakuraryoko.phantom_spawning.impl.player.PlayerManager;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class PhantomSpawningCommand implements IServerCommand
{
	private static final PhantomSpawningCommand INSTANCE = new PhantomSpawningCommand();
	public static PhantomSpawningCommand getInstance() { return INSTANCE; }

	@Override
	//#if MC >= 11902
	//$$ public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection)
	//#else
	public void register(CommandDispatcher<CommandSourceStack> dispatcher)
	//#endif
	{
		dispatcher.register(
				literal(this.getName())
						.requires(PermsWrap.check(this.getNode()+".about", ConfigWrap.mainOpt().permission_level))
						.executes(this::about)
						.then(argument("true_false", BoolArgumentType.bool())
						    .requires(PermsWrap.check(this.getNode()+".true_false", ConfigWrap.mainOpt().permission_level))
							.executes(ctx -> this.toggle(ctx, BoolArgumentType.getBool(ctx, "true_false")))
						)
		);
	}

	@Override
	public String getName()
	{
		return "phantomspawning";
	}

	@Override
	public String getModId()
	{
		return Reference.MOD_ID;
	}

	private int about(CommandContext<CommandSourceStack> ctx)
	{
		final Component text = InitWrap.text().formatText(
				"§7Please use §b/"+ this.getName() +" <true|false>§7 --\n§7To Enable or disable Phantoms from Spawning.\n§7This setting is Per-User.§r"
		);

		//#if MC >= 12001
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		try
		{
			ServerPlayer player = ctx.getSource().getPlayerOrException();
			boolean status = PlayerManager.getInstance().getPhantomStatus(player.getGameProfile());

			if (InitWrap.debug())
			{
				PhantomSpawningMod.LOGGER.warn("CMD:about: from [{}]", player.getName().getString());
			}

			final Component text2 = InitWrap.text().formatText(
					"§eCurrent status: "+ (status ? "§cSpawning Enabled" : "§aSpawning Disabled") + "§r"
			);

			//#if MC >= 12001
			//$$ ctx.getSource().sendSuccess(() -> text2, false);
			//#else
			ctx.getSource().sendSuccess(text2, false);
			//#endif
		}
		catch (CommandSyntaxException err)
		{
			PhantomSpawningMod.LOGGER.warn("CMD:about: Syntax Error; {}", err.getLocalizedMessage());
			return 0;
		}

		return 1;
	}

	private int toggle(CommandContext<CommandSourceStack> ctx, boolean toggle)
	{
		try
		{
			ServerPlayer player = ctx.getSource().getPlayerOrException();
			PlayerManager.getInstance().setPhantomStatus(player.getGameProfile(), toggle);

			final Component text = toggle
			                 ? InitWrap.text().formatText("§cEnabled Phantom spawning.§r")
			                 : InitWrap.text().formatText("§aDisabled Phantoms from spawning.§r");

			//#if MC >= 12001
			//$$ ctx.getSource().sendSuccess(() -> text, false);
			//#else
			ctx.getSource().sendSuccess(text, false);
			//#endif

			if (toggle)
			{
				PhantomSpawningMod.LOGGER.info("Player: '{}' has enabled Phantom spawns.", player.getName().getString());
			}
			else
			{
				PhantomSpawningMod.LOGGER.info("Player: '{}' has disabled Phantom spawns.", player.getName().getString());
			}
		}
		catch (CommandSyntaxException err)
		{
			PhantomSpawningMod.LOGGER.warn("CMD:toggle: Syntax Error; {}", err.getLocalizedMessage());
			return 0;
		}

		return 1;
	}
}
