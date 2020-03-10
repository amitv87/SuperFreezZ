/*
 * Copyright (c) 2019,2020 Hocuri
 *
 * This file is part of SuperFreezZ.
 *
 * SuperFreezZ is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SuperFreezZ is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SuperFreezZ.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package superfreeze.tool.android.backend

import android.content.Context
import android.os.Build
import eu.chainfire.libsuperuser.Shell


// Try using root when called the first time, then cache the result
val isRootAvailable by lazy {
	try {
		Shell.SU.available()
	} catch (e: Shell.ShellDiedException) {
		false
	}
}

@Throws(java.lang.Exception::class)
fun freezeAppsUsingRoot(b: Boolean, ctx: Context,
		packages: List<String>) {
	try {
		val shell = Shell.Pool.SU.get();
		if (Build.VERSION.SDK_INT >= 23) shell.run("dumpsys battery unplug")
		packages.forEach {
			if (Build.VERSION.SDK_INT >= 23) {
				shell.run("am set-inactive $it true")
			}
			if (it.equals(ctx.packageName)) {
				if (Build.VERSION.SDK_INT >= 23) shell.run("dumpsys deviceidle force-idle")
				if (b) shell.run("input keyevent 26")
			}
			shell.run("am kill $it")
			shell.run("am force-stop $it")
		}
		if (Build.VERSION.SDK_INT >= 23) shell.run("dumpsys deviceidle force-idle")
		if (b) shell.run("input keyevent 26")
	} catch (e: Shell.ShellDiedException) {
	}
}


private const val TAG = "SF-FreezeUsingRoot"