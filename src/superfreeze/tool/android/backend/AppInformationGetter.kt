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
 * This file contains functions that get necessary information about apps.
 */

package superfreeze.tool.android.backend

import android.app.Activity
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import superfreeze.tool.android.R
import superfreeze.tool.android.database.getFreezeMode

/**
 * Gets the running applications. Do not use from the UI thread.
 */
internal fun getRunningApplications(context: Context): List<PackageInfo> {
	return context.packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
			.filter {
				//Add the package only if it is NOT a system app:
				!it.applicationInfo.flags.isFlagSet(ApplicationInfo.FLAG_SYSTEM)
			}
}

/**
 * Queries usage stats for the last two years by calling usageStatsManager.queryAndAggregateUsageStats().
 * TAKING VERY LONG, CALL ONLY ONCE DUE TO PERFORMANCE REASONS!
 *
 * @return A map with the package names of running apps or null if it could not be determined (on older versions of Android)
 * @see android.app.usage.UsageStatsManager.queryAndAggregateUsageStats
 */
internal fun getAggregatedUsageStats(context: Context): Map<String, UsageStats>? {
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP_MR1) {
		//In Android versions older than LOLLIPOP there is no UsageStatsManager
		return null
	}
	val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager

	//Get all data starting two years ago
	val now = System.currentTimeMillis()
	val startDate = now - 1000L*60*60*24*365*2

	return usageStatsManager.queryAndAggregateUsageStats(startDate, now)
}

internal fun isRunning(applicationInfo: ApplicationInfo): Boolean {
	return ! applicationInfo.flags.isFlagSet(ApplicationInfo.FLAG_STOPPED)
}

internal fun isPendingFreeze(packageInfo: PackageInfo, usageStats: UsageStats?, activity: Activity): Boolean {
	return isPendingFreeze(
			getFreezeMode(activity, packageInfo.packageName),
			packageInfo.applicationInfo,
			usageStats)
}

internal fun isPendingFreeze(freezeMode: FreezeMode, applicationInfo: ApplicationInfo, usageStats: UsageStats?) : Boolean {

	if (!isRunning(applicationInfo)) {
		return false
	}

	return when(freezeMode) {

		FreezeMode.ALWAYS_FREEZE -> true

		FreezeMode.NEVER_FREEZE -> false

		FreezeMode.FREEZE_WHEN_INACTIVE -> {
			notUsedRecently(usageStats)
		}
	}
}

internal fun getPendingFreezeExplanation(freezeMode: FreezeMode, applicationInfo: ApplicationInfo, usageStats: UsageStats?, context: Context) : String {

	fun string(stringID: Int) = context.getString(stringID)

	val isRunning = isRunning(applicationInfo)

	return when(freezeMode) {

		FreezeMode.ALWAYS_FREEZE ->
			if (isRunning) string(R.string.pending_freeze) else string(R.string.frozen)

		FreezeMode.NEVER_FREEZE ->
			string(R.string.freeze_off)

		FreezeMode.FREEZE_WHEN_INACTIVE -> {
			if (notUsedRecently(usageStats)) {
				if (isRunning) string(R.string.pending_freeze) else string(R.string.frozen)
			} else {
				if (isRunning)
					string(R.string.used_recently)
				else
					string(R.string.used_recently) + string(R.string.space_AND_space) + string(R.string.frozen)
			}
		}
	}
}

private fun notUsedRecently(usageStats: UsageStats?) =
		System.currentTimeMillis() - getLastTimeUsed(usageStats) > 1000L * 60 * 60 * 24 * 7

/**
 * Queries the usage stats and returns those apps that are pending freeze.
 * Do not use if you have already called getAggregatedUsageStats().
 */
internal fun getAppsPendingFreeze(context: Context, activity: Activity): List<String> {

	val usageStatsMap = getAggregatedUsageStats(context)
	return getRunningApplications(context)
			.asSequence()
			.filter { isPendingFreeze(it, usageStatsMap?.get(it.packageName), activity) }
			.map { it.packageName }
			.toList()
}

private fun getLastTimeUsed(usageStats: UsageStats?): Long {
	return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		usageStats?.lastTimeUsed
				?: Long.MIN_VALUE
	} else {
		Long.MIN_VALUE
	}
}

private fun Int.isFlagSet(value: Int): Boolean {
	return (this and value) == value
}

/**
 * The freeze mode of an app: ALWAYS_FREEZE, NEVER_FREEZE or FREEZE_WHEN_INACTIVE
 */
enum class FreezeMode {

	/**
	 * This app will always be frozen if it is running, regardless of when it was used last.
	 */
	ALWAYS_FREEZE,

	/**
	 * This app will never be frozen, even if it has been running in background for whatever time.
	 */
	NEVER_FREEZE,

	/**
	 * This app will be frozen if it was not used for a specific time but is running in background.
	 */
	FREEZE_WHEN_INACTIVE
}