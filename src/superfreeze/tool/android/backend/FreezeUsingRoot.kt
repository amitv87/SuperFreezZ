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

// Try using root when called the first time, then cache the result
val isRootAvailable by lazy {
	try {
		execAsRoot(null)
		true
	} catch (e: java.lang.Exception) {
		false
	}
}

@Throws(java.lang.Exception::class)
fun freezeAppsUsingRoot(
	packages: List<String>
) {
	for (p in packages) {
		execAsRoot("am force-stop $p")
	}
}

private fun execAsRoot(command: String?) {
	var p: Process? = null
	try {
		p = Runtime.getRuntime().exec("su")
		if (command != null) {
			p!!.outputStream.write((command + "\n").toByteArray())
		}
		p!!.outputStream.write("exit\n".toByteArray())
		p.outputStream.flush()
		p.outputStream.close()
	} finally {
		p?.destroy()
	}
}

private const val TAG = "SF-FreezeUsingRoot"