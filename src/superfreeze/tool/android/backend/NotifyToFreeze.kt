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

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import superfreeze.tool.android.database.cumulatedAppsPendingFreeze
import superfreeze.tool.android.database.prefFreezeOnScreenOff

const val jobID = 2398 // Just some random number, do not change it!

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NotifyToFreezeJob : JobService() {
	override fun onStartJob(params: JobParameters?): Boolean {
		if (prefFreezeOnScreenOff) {
			(getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).cancel(jobID)
			return false
		}
		cumulatedAppsPendingFreeze += getAppsPendingFreeze(this).size
		if (cumulatedAppsPendingFreeze > 10) {
			// Send notification
		}
		return false
	}

	override fun onStopJob(params: JobParameters?): Boolean {
		throw IllegalStateException()
	}

}

fun startJob(context: Context) {
	if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
		val jobService = ComponentName(context.packageName, NotifyToFreezeJob::class.java.name)
		val jobInfo = JobInfo.Builder(jobID, jobService)
			.setPeriodic(1000 * 60 * 60 * 10) // 10 hours
			.setPersisted(true)
			.build()
		(context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler).schedule(jobInfo)
	}
}

