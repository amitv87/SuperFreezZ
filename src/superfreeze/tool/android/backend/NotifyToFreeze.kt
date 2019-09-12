/*
 * Copyright (c) 2019 Hocuri
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

/**
 * This file contains code to send a notification when many apps are pending freeze.
 */

package superfreeze.tool.android.backend

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import superfreeze.tool.android.R
import superfreeze.tool.android.database.cumulatedAppsPendingFreeze
import superfreeze.tool.android.database.prefFreezeOnScreenOff
import superfreeze.tool.android.userInterface.FreezeShortcutActivity
import superfreeze.tool.android.userInterface.mainActivity.MainActivity

const val jobID = 23 // Just some random number, do not change it!
const val channelID = "23"
const val notificationID = 23

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NotifyToFreezeJob : JobService() {
	override fun onStartJob(params: JobParameters?): Boolean {
		if (prefFreezeOnScreenOff) {
			(getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancel(jobID)
			return false
		}
		val pendingFreeze = getAppsPendingFreeze(this).size
		cumulatedAppsPendingFreeze += pendingFreeze
		Log.i(TAG, "$pendingFreeze apps pending freeze, cumulated is $cumulatedAppsPendingFreeze")

		if (cumulatedAppsPendingFreeze > 10 && pendingFreeze > 0) {
			showNotification(this, pendingFreeze)
		}
		return false
	}

	override fun onStopJob(params: JobParameters?): Boolean {
		throw IllegalStateException()
	}

	companion object {
		@JvmStatic

		fun startJob(context: Context) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {

				Log.i(TAG, "Starting job")
				val jobService = ComponentName(context.packageName, NotifyToFreezeJob::class.java.name)
				val jobInfo = JobInfo.Builder(jobID, jobService)
					.setPeriodic(1000 * 5) // 10 hours TODO
					.setPersisted(true)
					.build()
				(context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo)

			}
		}

	}
}

private fun createNotificationChannel(context: Context) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
		val name = "SuperFreezZ"
		val importance = NotificationManager.IMPORTANCE_LOW
		val channel = NotificationChannel(channelID, name, importance)
		// Register the channel with the system
		val notificationManager: NotificationManager =
			context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager.createNotificationChannel(channel)
	}
}

private fun showNotification(context: Context, pendingFreeze: Int) {
	createNotificationChannel(context)

	val mainIntent = Intent(context, MainActivity::class.java).apply {
		flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
	}
	val pendingMainIntent = PendingIntent.getActivity(context, 0, mainIntent, 0)

	val freezeIntent = FreezeShortcutActivity.createShortcutIntent(context)
	val freezePendingIntent: PendingIntent =
		PendingIntent.getActivity(context, 0, freezeIntent, 0)

	val configureIntent = freezeIntent //TODO
	val configurePendingIntent: PendingIntent =
		PendingIntent.getActivity(context, 0, configureIntent, 0)

	val builder = NotificationCompat.Builder(context, channelID)
		.setSmallIcon(R.drawable.ic_lightning_bolt_white)
		.setContentTitle("SuperFreezZ")
		.setContentText("$pendingFreeze apps are pending freeze")
		.setContentIntent(pendingMainIntent)
		.setPriority(NotificationCompat.PRIORITY_LOW)
		.setAutoCancel(true)
		.addAction(R.drawable.ic_freeze, "Freeze", freezePendingIntent)
		.addAction(
			R.drawable.ic_settings_applications_black_24dp,
			"Configure",
			configurePendingIntent
		)
	NotificationManagerCompat.from(context).notify(notificationID, builder.build())
}

private const val TAG = "SF-NotifyToFreeze"
