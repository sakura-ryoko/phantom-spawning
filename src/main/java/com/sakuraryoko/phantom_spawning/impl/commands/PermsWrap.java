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

import java.util.function.Predicate;

import javax.annotation.Nonnull;

//#if MC >= 11605
//$$ import me.lucko.fabric.api.permissions.v0.Permissions;
//#endif

import net.minecraft.commands.CommandSourceStack;

/**
 * (Lucko) Fabric Permissions API support only begins with MC 1.16.4+
 */
public class PermsWrap
{
	public static Predicate<CommandSourceStack> check(@Nonnull String node, int level)
	{
//#if MC >= 11605
//$$		return Permissions.require(node, level);
//#else
		return (src -> src.hasPermission(level));
//#endif
	}
}
