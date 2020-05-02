package superfreeze.tool.android.userInterface

import android.app.Activity

abstract class MyActivityCompanion {
	private val toBeDoneOnResume: MutableList<Activity.() -> Boolean> = mutableListOf()
	/**
	 *
	 * @param task
	 *      Execute this task on resume.
	 *      If it returns true, then it will be executed again at the next onResume.
	 */
	fun doOnResume(task: Activity.() -> Boolean) {
		toBeDoneOnResume.add(task)
	}

	// Must be called from the activity's onResume()
	fun onResume(activity: Activity) {
		//Execute all tasks and retain only those that returned true.
		toBeDoneOnResume.retainAll { task -> activity.task() }
	}
}
