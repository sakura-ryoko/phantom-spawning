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

import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
//#if MC >= 1.19.2
//$$ import net.minecraft.commands.Commands;
//$$ import net.minecraft.commands.CommandBuildContext;
//#endif
import net.minecraft.network.chat.*;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.stats.Stat;
import net.minecraft.stats.Stats;

import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.corelib.api.modinit.ModInitData;
import com.sakuraryoko.corelib.impl.config.ConfigManager;
import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.Reference;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.PhantomSpawningConfigHandler;
import com.sakuraryoko.phantom_spawning.impl.modinit.InitWrap;
import com.sakuraryoko.phantom_spawning.impl.modinit.PhantomSpawningInit;
import com.sakuraryoko.phantom_spawning.impl.player.PlayerManager;
import com.sakuraryoko.phantom_spawning.impl.player.ProfileWrap;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;

@ApiStatus.Internal
public class PhantomSpawningAdminCommand implements IServerCommand
{
	private static final PhantomSpawningAdminCommand INSTANCE = new PhantomSpawningAdminCommand();
	public static PhantomSpawningAdminCommand getInstance() { return INSTANCE; }

	@Override
	//#if MC >= 1.19.2
	//$$ public void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext commandBuildContext, Commands.CommandSelection commandSelection)
	//#else
	public void register(CommandDispatcher<CommandSourceStack> dispatcher)
	//#endif
	{
		dispatcher.register(
				literal(this.getName())
						.requires(PermsWrap.check(this.getNode(), ConfigWrap.mainOpt().permission_level_admin))
						.executes(this::about)
						.then(literal("reload")
								      .requires(PermsWrap.check(this.getNode()+".reload", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::reload)
						)
						.then(literal("save")
								      .requires(PermsWrap.check(this.getNode()+".save", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::save)
						)
						.then(literal("info")
								      .requires(PermsWrap.check(this.getNode()+".info", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::infoPlayer)
								      .then(argument("player", EntityArgument.player())
										            .executes(ctx ->
												                      this.infoPlayer(ctx, EntityArgument.getPlayer(ctx, "player"))
										            )
								      )
						)
						.then(literal("at-risk")
								      .requires(PermsWrap.check(this.getNode()+".at-risk", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::displayAtRisk)
						)
						.then(literal("toggle")
								      .requires(PermsWrap.check(this.getNode()+".toggle", ConfigWrap.mainOpt().permission_level_admin))
								      .then(argument("player", EntityArgument.player())
										            .executes(ctx ->
												                      this.togglePlayer(ctx, EntityArgument.getPlayer(ctx, "player"))
										            )
										            .then(argument("toggle", BoolArgumentType.bool())
												                  .executes(ctx ->
														                            this.togglePlayer(ctx, EntityArgument.getPlayer(ctx, "player"), BoolArgumentType.getBool(ctx, "toggle"))
												                  )
										            )
								      )
						)
						.then(literal("debug")
								      .requires(PermsWrap.check(this.getNode()+".debug", ConfigWrap.mainOpt().permission_level_admin))
								      .then(argument("player", EntityArgument.player())
										            .executes(ctx ->
												                      this.debugPlayer(ctx, EntityArgument.getPlayer(ctx, "player"))
										            )
										            .then(argument("toggle", BoolArgumentType.bool())
												                  .executes(ctx ->
														                            this.debugPlayer(ctx, EntityArgument.getPlayer(ctx, "player"), BoolArgumentType.getBool(ctx, "toggle"))
												                  )
										            )
								      )
						)
						.then(literal("toggle-all")
								      .requires(PermsWrap.check(this.getNode()+".toggle-all", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::toggleAll)
								      .then(argument("toggle", BoolArgumentType.bool())
										            .executes(ctx ->
												                      this.toggleAll(ctx, BoolArgumentType.getBool(ctx, "toggle"))
										            )
								      )
						)
						.then(literal("debug-all")
								      .requires(PermsWrap.check(this.getNode()+".debug-all", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::debugAll)
								      .then(argument("toggle", BoolArgumentType.bool())
										            .executes(ctx ->
												                      this.debugAll(ctx, BoolArgumentType.getBool(ctx, "toggle"))
										            )
								      )
						)
		);
	}

	@Override
	public String getName()
	{
		return "phantomspawning-admin";
	}

	@Override
	public String getModId()
	{
		return Reference.MOD_ID;
	}

	private int about(CommandContext<CommandSourceStack> ctx)
	{
		List<Component> info = PhantomSpawningInit.getInstance().getVanillaFormatted(ModInitData.ALL_INFO);
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		for (Component entry : info)
		{
			text.append(entry).append("\n");
		}

		text.append(" - §7Global Debug status: ")
		    .append(
					this.createDebugAllHoverEvent(ConfigWrap.mainOpt().phantomDebug)
		    );

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private Component createDebugAllHoverEvent(boolean debug)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal(" ");
		//#else
		TextComponent text = new TextComponent(" ");
		//#endif

		text.append(debug ? "§dON§r" : "§bOFF§r");

		return text.withStyle(
				style -> style.setClickEvent(
						//#if MC >= 1.21.5
						//$$ new ClickEvent.SuggestCommand(
						//#else
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
								//#endif
								       "/"+this.getName()+" debug-all "+ (debug ? "false" : "true")
						)
				)
		);
	}

	private int save(CommandContext<CommandSourceStack> ctx)
	{
		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText("Saving config!"), false);
		//#else
		ctx.getSource().sendSuccess(InitWrap.text().formatText("Saving config!"), false);
		//#endif

		ConfigManager.getInstance().saveEach(PhantomSpawningConfigHandler.getInstance());
		String user = ctx.getSource().getTextName();
		PhantomSpawningMod.LOGGER.info("{} has saved the configuration.", user);

		return 1;
	}

	private int reload(CommandContext<CommandSourceStack> ctx)
	{
		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> InitWrap.text().formatText("Reloaded config!"), false);
		//#else
		ctx.getSource().sendSuccess(InitWrap.text().formatText("Reloaded config!"), false);
		//#endif

		ConfigManager.getInstance().reloadEach(PhantomSpawningConfigHandler.getInstance());
		String user = ctx.getSource().getTextName();
		PhantomSpawningMod.LOGGER.info("{} has reloaded the configuration.", user);

		return 1;
	}

	private int infoPlayer(CommandContext<CommandSourceStack> ctx)
	{
		try
		{
			return this.infoPlayer(ctx, ctx.getSource().getPlayerOrException());
		}
		catch (CommandSyntaxException err)
		{
			PhantomSpawningMod.LOGGER.warn("CMD:infoPlayer: Syntax Error; {}", err.getLocalizedMessage());
			return 0;
		}
	}

	private int infoPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player)
	{
		Pair<Boolean, Integer> pair = PlayerManager.getInstance().getPlayerInfo(player);

		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		text.append(
				InitWrap.text().formatText("§7Player Info for: ")
		).append(
				player.getDisplayName()
		).append(
				InitWrap.text().formatText("\n - §7Phantom status:§r")
		).append(
				this.createPlayerStatusHoverEvent(player, pair.getLeft())
		).append(
				InitWrap.text().formatText("\n - §7Time since rest:")
		).append(
				this.createFormattedRestTimeStat(pair)
		).append(
				InitWrap.text().formatText("\n - §7Player debug status:§r")
		).append(
				this.createPlayerDebugHoverEvent(player, PlayerManager.getInstance().getDebugStatus(player.getUUID()))
		);

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private Component createPlayerStatusHoverEvent(ServerPlayer player, boolean status)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal(" ");
		//#else
		TextComponent text = new TextComponent(" ");
		//#endif
		String name = ProfileWrap.name(player.getGameProfile());

		text.append(status ? "§cEnabled§r" : "§aDisabled§r");

		return text.withStyle(
				style -> style.setClickEvent(
						//#if MC >= 1.21.5
						//$$ new ClickEvent.SuggestCommand(
						//#else
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
					    //#endif
						               "/"+this.getName()+" toggle "+name+" "+ (status ? "false" : "true")
						)
				).setInsertion(name)
		);
	}

	private Component createFormattedRestTimeStat(Pair<Boolean, Integer> pair)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal(" ");
		//#else
		TextComponent text = new TextComponent(" ");
		//#endif
		Stat<ResourceLocation> stat = Stats.CUSTOM.get(Stats.TIME_SINCE_REST);
		boolean canSpawn = pair.getRight() >= 72000;

		return text.append("§f" +stat.format(pair.getRight()))
		           .append(canSpawn
		                   ? pair.getLeft()
		                        ? " §c(" +pair.getRight()+ ") §f[§6Could Spawn§f]§r"
		                        : " §c(" +pair.getRight()+ ")§r"
		                   : " §e(" +pair.getRight()+ ")§r"
		           );
	}

	private Component createPlayerDebugHoverEvent(ServerPlayer player, boolean debug)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal(" ");
		//#else
		TextComponent text = new TextComponent(" ");
		//#endif
		String name = ProfileWrap.name(player.getGameProfile());

		text.append(debug ? "§dON§r" : "§bOFF§r");

		return text.withStyle(
				style -> style.setClickEvent(
						//#if MC >= 1.21.5
						//$$ new ClickEvent.SuggestCommand(
						//#else
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
					    //#endif
					                   "/"+this.getName()+" debug "+name+" "+ (debug ? "false" : "true")
						)
				).setInsertion(name)
		);
	}

	private int displayAtRisk(CommandContext<CommandSourceStack> ctx)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif
		PlayerList list = ctx.getSource().getServer().getPlayerList();
		int count = 0;

		text.append("§9Showing all at-risk players for Phantom Spawning:§r\n");

		for (ServerPlayer player : list.getPlayers())
		{
			Pair<Boolean, Integer> pair = PlayerManager.getInstance().getPlayerInfo(player);

			if (pair.getLeft() && pair.getRight() >= 72000)
			{
				if (count > 0)
				{
					text.append("§r§f, ");
				}
				else if (count == 0)
				{
					text.append(" §7- §f");
				}

				text.append(
						this.createPlayerInfoHoverEvent(player)
				).append("\n");
				count++;
			}
		}

		text.append(
				String.format("§6(%d total)§r", count)
		);

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private Component createPlayerInfoHoverEvent(ServerPlayer player)
	{
		String name = ProfileWrap.name(player.getGameProfile());

		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal(name);
		//#else
		TextComponent text = new TextComponent(name);
		//#endif

		return text.withStyle(
				style -> style.setClickEvent(
						//#if MC >= 1.21.5
						//$$ new ClickEvent.SuggestCommand(
						//#else
						new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND,
								//#endif
								       "/"+this.getName()+" info "+name
						)
				).setInsertion(name)
		);
	}

	private int togglePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player)
	{
		return this.togglePlayer(ctx, player, !PlayerManager.getInstance().getPhantomStatus(player.getGameProfile()));
	}

	private int togglePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player, boolean toggle)
	{
		PlayerManager.getInstance().setPhantomStatus(player.getGameProfile(), toggle);

		final Component text = toggle
		                 ? InitWrap.text().formatText("§cEnabled Phantom spawning; for player: §r")
		                 : InitWrap.text().formatText("§aDisabled Phantoms from spawning; for player: §r");

		//#if MC >= 1.19.2
		//$$ ((MutableComponent) text).append(
		//#else
		((BaseComponent) text).append(
		//#endif
				player.getDisplayName()
		);

		//#if MC >= 1.20.1
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

		return 1;
	}

	private int debugPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player)
	{
		return this.debugPlayer(ctx, player, !PlayerManager.getInstance().getDebugStatus(player.getUUID()));
	}

	private int debugPlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer player, boolean toggle)
	{
		PlayerManager.getInstance().setDebugStatus(player.getGameProfile(), toggle);

		final Component text = toggle
		                       ? InitWrap.text().formatText("§dEnabled Phantom spawning debug; for player: §r")
		                       : InitWrap.text().formatText("§bDisabled Phantom spawning debug; for player: §r");

		//#if MC >= 1.19.2
		//$$ ((MutableComponent) text).append(
		//#else
		((BaseComponent) text).append(
		//#endif
				player.getDisplayName()
		);

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		if (toggle)
		{
			PhantomSpawningMod.LOGGER.info("Phantom spawn debug enabled for player: '{}'", player.getName().getString());
		}
		else
		{
			PhantomSpawningMod.LOGGER.info("Phantom spawn debug disabled for player: '{}'", player.getName().getString());
		}

		return 1;
	}

	private int toggleAll(CommandContext<CommandSourceStack> ctx)
	{
		return this.toggleAll(ctx, true);
	}

	private int toggleAll(CommandContext<CommandSourceStack> ctx, boolean toggle)
	{
		PlayerManager.getInstance().setPhantomStatusAll(toggle);

		final Component text = toggle
		                       ? InitWrap.text().formatText("§cEnabled Phantom spawning for all players§r")
		                       : InitWrap.text().formatText("§aDisabled Phantom spawning for all players§r");

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		if (toggle)
		{
			PhantomSpawningMod.LOGGER.info("Phantom spawning enabled for all players");
		}
		else
		{
			PhantomSpawningMod.LOGGER.info("Phantom spawning disabled for all players");
		}

		return 1;
	}

	private int debugAll(CommandContext<CommandSourceStack> ctx)
	{
		return this.debugAll(ctx, !ConfigWrap.mainOpt().phantomDebug);
	}

	private int debugAll(CommandContext<CommandSourceStack> ctx, boolean toggle)
	{
		PlayerManager.getInstance().setDebugStatusAll(toggle);
		ConfigWrap.mainOpt().phantomDebug = toggle;

		final Component text = toggle
		                       ? InitWrap.text().formatText("§dPhantom spawning debug enabled for all players§r")
		                       : InitWrap.text().formatText("§bPhantom spawning debug for all players§r");

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		if (toggle)
		{
			PhantomSpawningMod.LOGGER.info("Phantom spawning debug enabled for all players");
		}
		else
		{
			PhantomSpawningMod.LOGGER.info("Phantom spawning debug disabled for all players");
		}

		return 1;
	}
}
