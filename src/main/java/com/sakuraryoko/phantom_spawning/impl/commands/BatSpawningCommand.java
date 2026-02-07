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

package com.sakuraryoko.phantom_spawning.impl.commands;

import org.jetbrains.annotations.ApiStatus;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;
//#if MC >= 11902
//$$ import net.minecraft.commands.Commands;
//$$ import net.minecraft.commands.CommandBuildContext;
//$$ import net.minecraft.network.chat.MutableComponent;
//$$ import net.minecraft.network.chat.Component;
//#else
import net.minecraft.network.chat.TextComponent;
//#endif

import com.sakuraryoko.corelib.api.commands.IServerCommand;
import com.sakuraryoko.phantom_spawning.impl.PhantomSpawningMod;
import com.sakuraryoko.phantom_spawning.impl.Reference;
import com.sakuraryoko.phantom_spawning.impl.config.ConfigWrap;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptions;
import com.sakuraryoko.phantom_spawning.impl.config.data.options.BatOptionsLimits;
import com.sakuraryoko.phantom_spawning.impl.modinit.InitWrap;

import static net.minecraft.commands.Commands.argument;
import static net.minecraft.commands.Commands.literal;


@ApiStatus.Internal
public class BatSpawningCommand implements IServerCommand
{
	private static final BatSpawningCommand INSTANCE = new BatSpawningCommand();

	public static BatSpawningCommand getInstance() {return INSTANCE;}

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
						.then(literal("enable")
								      .requires(PermsWrap.check(this.getNode() + ".enable", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::toggleEnabled)
						)
						.then(literal("disable")
								      .requires(PermsWrap.check(this.getNode() + ".disable", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::toggleDisabled)
						)
						.then(literal("reset")
								      .requires(PermsWrap.check(this.getNode() + ".reset", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::resetConfig)
						)
						.then(literal("set")
								      .requires(PermsWrap.check(this.getNode() + ".set", ConfigWrap.mainOpt().permission_level_admin))
								      .executes(this::about)
								      .then(literal("ambientMobCap")
										            .requires(PermsWrap.check(this.getNode() + ".set.ambient_mob_cap", ConfigWrap.mainOpt().permission_level_admin))
										            .then(argument("cap", IntegerArgumentType.integer(BatOptionsLimits.MIN_CAP, BatOptionsLimits.MAX_CAP))
												                  .executes(ctx ->
														                            this.setAmbientMobCap(ctx, IntegerArgumentType.getInteger(ctx, "cap"))
												                  )
										            )
								      )
								      .then(literal("brightnessFactor")
										            .requires(PermsWrap.check(this.getNode() + ".set.brightness_factor", ConfigWrap.mainOpt().permission_level_admin))
										            .executes(this::about)
										            .then(argument("factor", IntegerArgumentType.integer(BatOptionsLimits.MIN_FACTOR, BatOptionsLimits.MAX_FACTOR))
												                  .executes(ctx ->
														                            this.setBatLightFactor(ctx, IntegerArgumentType.getInteger(ctx, "factor"))
												                  )
										            )
								      )
								      .then(literal("halloweenFeatures")
										            .requires(PermsWrap.check(this.getNode() + ".set.halloween_features", ConfigWrap.mainOpt().permission_level_admin))
										            .executes(this::about)
										            .then(argument("halloween", BoolArgumentType.bool())
												                  .executes(ctx ->
														                            this.toggleHalloweenFeatures(ctx, BoolArgumentType.getBool(ctx, "halloween"))
												                  )
										            )
								      )
						)
		);
	}

	@Override
	public String getName()
	{
		return "batspawning";
	}

	@Override
	public String getModId()
	{
		return Reference.MOD_ID;
	}

	private int about(CommandContext<CommandSourceStack> ctx)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		BatOptions opts = ConfigWrap.batOpt();

		text.append(
				InitWrap.text().formatText("§eBat Spawning Config: ")
		).append(
				InitWrap.text().formatText(opts.enableBatConfig ? "§aEnabled§r" : "§cDisabled§r (Default)")
		).append(
				InitWrap.text().formatText("\n - §7Ambient (Per-Chunk) Mob Cap: ")
		).append(
				InitWrap.text().formatText("§b" + opts.setPerChunkAmbientMobCap + "§r (Default: " + BatOptionsLimits.DEFAULT_CAP + ")")
		).append(
				InitWrap.text().formatText("\n - §7Brightness Factor: ")
		).append(
				InitWrap.text().formatText("§b" + opts.setBrightnessFactor + "§r (Default: " + BatOptionsLimits.DEFAULT_FACTOR + ")")
		).append(
				InitWrap.text().formatText("\n - §7Halloween Feature allowed: ")
		).append(
				InitWrap.text().formatText(opts.halloweenFeature ? "§aEnabled§r" : "§cDisabled§r (Default)")
		).append(
				InitWrap.text().formatText("\n   §7(adds 3 to the brightness factor)§r")
		);

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private int resetConfig(CommandContext<CommandSourceStack> ctx)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		BatOptions opts = ConfigWrap.batOpt();
		opts.defaults();

		text.append(
				InitWrap.text().formatText("§eBat Spawning Config has ben reset to defaults")
		);

		try
		{
			ServerPlayer player = ctx.getSource().getPlayerOrException();
			PhantomSpawningMod.LOGGER.info("Player: '{}' has reset the Bat spawning config.", player.getName().getString());
		}
		catch (Exception ignored) { }

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private int toggleEnabled(CommandContext<CommandSourceStack> ctx)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		BatOptions opts = ConfigWrap.batOpt();

		if (opts.enableBatConfig)
		{
			text.append(InitWrap.text().formatText("§7Bat Spawning Config already enabled"));
		}
		else
		{
			opts.enableBatConfig = true;
			text.append(InitWrap.text().formatText("§7Bat Spawning Config: §aEnabled§r"));
		}

		try
		{
			ServerPlayer player = ctx.getSource().getPlayerOrException();

			if (opts.enableBatConfig)
			{
				PhantomSpawningMod.LOGGER.info("Player: '{}' Enabled the Bat spawning config.", player.getName().getString());
			}
		}
		catch (Exception ignored) { }

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private int toggleDisabled(CommandContext<CommandSourceStack> ctx)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		BatOptions opts = ConfigWrap.batOpt();

		if (opts.enableBatConfig)
		{
			opts.enableBatConfig = false;
			text.append(InitWrap.text().formatText("§7Bat Spawning Config: §cDisabled§r (Default)"));
		}
		else
		{
			text.append(InitWrap.text().formatText("§7Bat Spawning Config already disabled"));
		}

		try
		{
			ServerPlayer player = ctx.getSource().getPlayerOrException();

			if (!opts.enableBatConfig)
			{
				PhantomSpawningMod.LOGGER.info("Player: '{}' Disabled the Bat spawning config.", player.getName().getString());
			}
		}
		catch (Exception ignored) { }

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private int setAmbientMobCap(CommandContext<CommandSourceStack> ctx, int max)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		BatOptions opts = ConfigWrap.batOpt();
		final int last = opts.setPerChunkAmbientMobCap;

		if (max < BatOptionsLimits.MIN_CAP)
		{
			text.append(InitWrap.text().formatText("§7Value given is too low; §b" + BatOptionsLimits.MIN_CAP + "§7 is the lower limit"));
		}
		else if (max > BatOptionsLimits.MAX_CAP)
		{
			text.append(InitWrap.text().formatText("§7Value given is too high; §b" + BatOptionsLimits.MAX_CAP + "§7 is the upper limit"));
		}
		else if (max == last)
		{
			text.append(InitWrap.text().formatText("§7Value given is equal to the current value: §b" + last + "§r"));
		}
		else
		{
			opts.setPerChunkAmbientMobCap = max;
			text.append(InitWrap.text().formatText("§7Bat (Ambient Per-Chunk) Mob Cap set: §b" + max + "§r"));

			try
			{
				ServerPlayer player = ctx.getSource().getPlayerOrException();
				PhantomSpawningMod.LOGGER.info("Player: '{}' has changed the Bat (Ambient Per-Chunk) Mob Cap to: '{}'", player.getName().getString(), opts.setPerChunkAmbientMobCap);
			}
			catch (Exception ignored) { }
		}

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private int setBatLightFactor(CommandContext<CommandSourceStack> ctx, int max)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		BatOptions opts = ConfigWrap.batOpt();
		final int last = opts.setBrightnessFactor;

		if (max < BatOptionsLimits.MIN_FACTOR)
		{
			text.append(InitWrap.text().formatText("§7Value given is too low; §b" + BatOptionsLimits.MIN_FACTOR + "§7 is the lower limit"));
		}
		else if (max > BatOptionsLimits.MAX_FACTOR)
		{
			text.append(InitWrap.text().formatText("§7Value given is too high; §b" + BatOptionsLimits.MAX_FACTOR + "§7 is the upper limit"));
		}
		else if (max == last)
		{
			text.append(InitWrap.text().formatText("§7Value given is equal to the current value: §b" + last + "§r"));
		}
		else
		{
			opts.setBrightnessFactor = max;
			text.append(InitWrap.text().formatText("§7Bat Spawning brightness factor set: §b" + max + "§r"));

			try
			{
				ServerPlayer player = ctx.getSource().getPlayerOrException();
				PhantomSpawningMod.LOGGER.info("Player: '{}' has changed the Bat brightness factor to: '{}'", player.getName().getString(), opts.setBrightnessFactor);
			}
			catch (Exception ignored) { }
		}

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}

	private int toggleHalloweenFeatures(CommandContext<CommandSourceStack> ctx, boolean toggle)
	{
		//#if MC >= 1.19.2
		//$$ MutableComponent text = Component.literal("");
		//#else
		TextComponent text = new TextComponent("");
		//#endif

		BatOptions opts = ConfigWrap.batOpt();
		final boolean last = opts.halloweenFeature;
		String curr = last ? "§aEnabled§r" : "§cDisabled§r (Default)";

		if (toggle == last)
		{
			text.append(InitWrap.text().formatText("§7Value given is equal to the current value: " + curr));
		}
		else
		{
			opts.halloweenFeature = toggle;
			curr = toggle ? "§aEnabled§r" : "§cDisabled§r (Default)";
			text.append(InitWrap.text().formatText("§7Bat Spawning (Halloween Feature) set to: " + curr));

			try
			{
				ServerPlayer player = ctx.getSource().getPlayerOrException();

				if (opts.halloweenFeature)
				{
					PhantomSpawningMod.LOGGER.info("Player: '{}' has Enabled the Bat Halloween Feature", player.getName().getString());
				}
				else
				{
					PhantomSpawningMod.LOGGER.info("Player: '{}' has Disabled the Bat Halloween Feature", player.getName().getString());
				}
			}
			catch (Exception ignored) { }
		}

		//#if MC >= 1.20.1
		//$$ ctx.getSource().sendSuccess(() -> text, false);
		//#else
		ctx.getSource().sendSuccess(text, false);
		//#endif

		return 1;
	}
}
