/*
Copyright (c) 2018 Hocuri

This file is part of SuperFreezZ.

SuperFreezZ is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SuperFreezZ is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SuperFreezZ.  If not, see <http://www.gnu.org/licenses/>.
*/

/**
 * This file contains functions responsible for general UI things like requesting permissions.
 */

package superfreeze.tool.android.userInterface

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.provider.Settings
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat.recreate
import superfreeze.tool.android.R
import superfreeze.tool.android.backend.usageStatsPermissionGranted
import superfreeze.tool.android.database.neverCalled
import superfreeze.tool.android.database.usageStatsAvailable

internal fun requestUsageStatsPermission(
	activityCompanion: MyActivityCompanion,
	activity: Activity,
	doAfterwards: () -> Unit
) {

	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
		&& !usageStatsPermissionGranted(activity)
		&& neverCalled("requestUsageStatsPermission", activity)
	) {

		AlertDialog.Builder(activity, R.style.myAlertDialog)
			.setTitle(activity.getString(R.string.usagestats_access))
			.setMessage(activity.getString(R.string.usatestats_explanation))
			.setPositiveButton(activity.getString(R.string.enable)) { _, _ ->
				showUsageStatsSettings(activity)
				activityCompanion.doOnResume {

					if (!usageStatsPermissionGranted(this)) {
						toast(getString(R.string.usagestats_not_enabled), Toast.LENGTH_SHORT)
					}
					doAfterwards()

					//Do not execute again
					false
				}
			}
			.setNegativeButton(activity.getString(android.R.string.no)) { _, _ ->
				//directly load running applications
				doAfterwards()
			}
			.setIcon(R.drawable.symbol_freeze_when_inactive)
			.setCancelable(false)
			.show()


	} else {
		//directly load running applications
		doAfterwards()
	}

}

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
private fun showUsageStatsSettings(context: Context) {
	val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
	context.startActivity(intent)
	context.toast(context.getString(R.string.select_enable_toast), Toast.LENGTH_LONG)
}

/**
 * If doAfterwards is given (not null), activityCompanion must be given, too.
 */
internal fun showAccessibilityDialog(context: Context, activityCompanion: MyActivityCompanion? = null, doAfterwards: Context.() -> Unit = {}) {
	AlertDialog.Builder(context, R.style.myAlertDialog)
		.setTitle(context.getString(R.string.accessibility_service))
		.setMessage(context.getString(R.string.accessibility_service_dialog_message))
		.setPositiveButton(context.getString(R.string.enable)) { _, _ ->
			val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
			context.startActivity(intent)
			activityCompanion?.doOnResume {
				doAfterwards()
				false
			}
		}
		.setNegativeButton(context.getString(android.R.string.cancel)) { _, _ ->
			context.doAfterwards()
		}
		.setIcon(R.mipmap.ic_launcher)
		.setCancelable(false)
		.show()
}


internal fun showSortChooserDialog(
	activityCompanion: MyActivityCompanion,
	context: Context,
	current: Int,
	onChosen: (which: Int) -> Unit
) {
	lateinit var dialog: AlertDialog
	dialog = AlertDialog.Builder(context)
		.setTitle(context.getString(R.string.sort_by))
		.setSingleChoiceItems(R.array.sortmodes, current) { _, which ->
			dialog.dismiss()

			// 2 means "Last time used" and requires usagestats access
			if (which == 2 && !usageStatsAvailable) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
					showUsageStatsSettings(context)
					activityCompanion.doOnResume {
						if (usageStatsPermissionGranted(context)) {
							onChosen(which)
							Handler().post { recreate() }
							// Recreate because the list items shall show the "Intelligent freeze"
							// button now (they have to change). The easiest possibility to refresh them
							// is to recreate the whole activity.
						}
						false
					}
				} else {
					Toast.makeText(
						context,
						context.getString(R.string.not_available),
						Toast.LENGTH_SHORT
					).show()
				}


			} else {
				onChosen(which)
			}
		}
		.show()
}

internal fun Context.toast(s: String, duration: Int) {
	Toast.makeText(this, s, duration).show()
}

//private const val TAG = "SF-GeneralUI"