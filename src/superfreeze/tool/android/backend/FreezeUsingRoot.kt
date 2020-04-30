/*
 * Copyright (c) 2019,2020 Hocuri, oF2pks
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
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import android.util.Log
import eu.chainfire.libsuperuser.Shell
import superfreeze.tool.android.R
import superfreeze.tool.android.userInterface.FreezeShortcutActivity
import superfreeze.tool.android.userInterface.mainActivity.AppsListAdapter
import superfreeze.tool.android.userInterface.mainActivity.MainActivity
import java.lang.Exception

// Try using root when called the first time, then cache the result
val isRootAvailable by lazy {
	try {
		Shell.SU.available()
	} catch (e: Shell.ShellDiedException) {
		false
	}
}

@Throws(Exception::class)
fun freezeAppsUsingRoot(
	packages: List<String>, context: Context,
	putScreenOffAfterFreezing: Boolean = false
) {
	if (Build.VERSION.SDK_INT >= 25) {
		val shortcutManager = context.getSystemService<ShortcutManager>(ShortcutManager::class.java)
		val shortcut: ShortcutInfo
		val intent = Intent(context, FreezeShortcutActivity::class.java)
		intent.putExtra("extraID", "dyn_screenOff")
		intent.action = Intent.ACTION_MAIN
		shortcut = ShortcutInfo.Builder(context, "FreezeShortcutOff")
				.setShortLabel(context.resources.getString(R.string.freeze_shortcut_label_screen_off))
				.setLongLabel(context.resources.getString(R.string.freeze_shortcut_label_screen_off))
				.setIntent(intent)
				//.setIcon(context.getResources().getDrawable(R.))
				.build()
		shortcutManager!!.dynamicShortcuts = listOf(shortcut)
	}


	try {
		val shell = Shell.Pool.SU.get();
		//if (Build.VERSION.SDK_INT >= 23) shell.run("dumpsys battery unplug")
		packages.forEach {
			if (Build.VERSION.SDK_INT >= 23) {
				shell.run("am set-inactive $it true")
			}
			if (it == context.packageName) {
				if (putScreenOffAfterFreezing) shell.run("input keyevent 26")
			}
			shell.run("am kill $it")
			shell.run("am force-stop $it")
		}
		if (putScreenOffAfterFreezing) shell.run("input keyevent 26")
	} catch (e: Shell.ShellDiedException) {
		Log.e(TAG, "ShellDiedException, probably we did not have root access. (???)")
	}
}


private const val TAG = "SF-FreezeUsingRoot"
