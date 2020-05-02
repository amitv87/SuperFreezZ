/*
The MIT License (MIT)

Copyright (c) 2015 axxapy
Copyright (c) 2018 Hocuri

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package superfreeze.tool.android.userInterface.mainActivity

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.SearchManager
import android.content.ComponentCallbacks2
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import superfreeze.tool.android.AsyncDelegated
import superfreeze.tool.android.R
import superfreeze.tool.android.backend.getApplications
import superfreeze.tool.android.database.neverCalled
import superfreeze.tool.android.database.prefIntroAlreadyShown
import superfreeze.tool.android.database.prefListSortMode
import superfreeze.tool.android.userInterface.FreezeShortcutActivity
import superfreeze.tool.android.userInterface.MyActivityCompanion
import superfreeze.tool.android.userInterface.intro.IntroActivity
import superfreeze.tool.android.userInterface.requestUsageStatsPermission
import superfreeze.tool.android.userInterface.settingsActivity.SettingsActivity
import superfreeze.tool.android.userInterface.showSortChooserDialog


/**
 * The activity that is shown at startup
 */
class MainActivity : AppCompatActivity() {
	private lateinit var appsListAdapter: AppsListAdapter

	private lateinit var progressBar: ProgressBar

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		// Accessing prefListSortMode sometimes took a lot of time -> I am doing it asynchronously
		val listSortMode by AsyncDelegated { prefListSortMode }
		val applications by AsyncDelegated { getApplications(applicationContext) }

		setContentView(R.layout.activity_main)

		val listView = list

		listView.layoutManager = LinearLayoutManager(this)
		appsListAdapter = AppsListAdapter(this, listSortMode)
		listView.adapter = appsListAdapter

		progressBar = progress
		progressBar.visibility = View.VISIBLE

		// We want the dialog to be only shown in onResume, not in onCreate as the app intro is supposed to be shown before this dialog:
		doOnResume {
			requestUsageStatsPermission(Companion, this) {
				appsListAdapter.setAndLoadItems(applications)
			}
			false // do not execute again
		}

		setSupportActionBar(toolbar)

		swiperefresh.setOnRefreshListener {
			recreate()
		}
	}

	override fun onResume() {
		super.onResume()

		// Show the app intro at the first launch:
		if (prefIntroAlreadyShown) {
			startActivity(Intent(this, IntroActivity::class.java))
			return
		}

		// TODO delete from here on:
		if (neverCalled("RootSupport", this)) {
			AlertDialog.Builder(this, R.style.myAlertDialog)
				.setTitle("EXPERIMENTAL root support")
				.setMessage("""I added root support. If you would like to try it, USE IT AT YOUR OWN RISK! It is still experimental. I'd be happy about feedback.""")
				.setPositiveButton("Ok") { _, _ -> }
				.show()
		}
		// TODO delete up to here

		Companion.onResume(this)

		// Refresh the apps list because it could have changed in the meantime (or Companion.onResume())
		appsListAdapter.refresh()
	}

	override fun onPause() {
		super.onPause()
		appsListAdapter.deleteAppInfos() // The ApplicationInfos might be outdated when we return
	}

	override fun onStop() {
		super.onStop()
		appsListAdapter.trimMemory()
	}


	/**
	 * At startup, there will be a spinning progress bar at the top right hand corner.
	 * Invoking this method will hide this progress bar.
	 */
	fun hideProgressBar() {
		progressBar.visibility = View.GONE
	}

	/**
	 * The method that is responsible for showing the search icon in the top right hand corner.
	 * @param menu The Menu to which the search icon is added.
	 */
	@SuppressLint("RestrictedApi")
	override fun onCreateOptionsMenu(menu: Menu): Boolean {
		menuInflater.inflate(R.menu.main, menu)

		val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
		val searchView = menu.findItem(R.id.action_search).actionView as SearchView
		searchView.setSearchableInfo(searchManager.getSearchableInfo(componentName))
		/*searchView.setOnQueryTextFocusChangeListener { _, queryTextFocused ->
			if (!queryTextFocused && searchView.query.isEmpty()) {
				val supportActionBar = supportActionBar
				supportActionBar?.expectNonNull(TAG)?.collapseActionView()
			}
		}*/
		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(s: String) = false

			override fun onQueryTextChange(s: String): Boolean {
				appsListAdapter.searchPattern = s
				return true
			}
		})

		//Listen on clicks on the floating action button:
		fab.setOnClickListener {
			startActivity(Intent(this, FreezeShortcutActivity::class.java))
		}
		return super.onCreateOptionsMenu(menu)
	}

	override fun onOptionsItemSelected(item: MenuItem?): Boolean {
		return when (item?.itemId) {

			R.id.action_create_shortcut -> {
				//Adding shortcut for FreezeShortcutActivity:
				val intent = FreezeShortcutActivity.createShortcutResultIntent(this)
				intent.action = "com.android.launcher.action.INSTALL_SHORTCUT"
				//addIntent.putExtra("duplicate", false)  //uncomment to not install a shortcut if it's already there
				applicationContext.sendBroadcast(intent)
				true
			}

			R.id.action_settings -> {
				startActivity(Intent(this, SettingsActivity::class.java))
				// Recreate after the settingsActivity was shown as settings might have changed:
				doOnResume {
					Handler().post { recreate() }
					// BTW Handler().post{...} is necessary because recreate() should not be called from onResume().
					false
				}
				true
			}

			R.id.action_sort -> {
				showSortChooserDialog(Companion, this, prefListSortMode) { index ->
					prefListSortMode = index

					appsListAdapter.sortModeIndex = index

					appsListAdapter.sortList()
					appsListAdapter.refresh()
				}
				true
			}

			else -> false
		}
	}

	override fun onConfigurationChanged(newConfig: Configuration) {
		super.onConfigurationChanged(newConfig)

		//This is necessary so that the list items change their look when the screen is rotated:
		val listView = list
		val position =
			(listView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
		listView.adapter = null
		listView.layoutManager = null
		listView.recycledViewPool.clear()
		listView.adapter = appsListAdapter
		listView.layoutManager = LinearLayoutManager(this)
		appsListAdapter.notifyDataSetChanged()
		(listView.layoutManager as LinearLayoutManager).scrollToPosition(position)
	}

	override fun onTrimMemory(level: Int) {
		//See https://developer.android.com/topic/performance/memory#release

		when (level) {
			ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN -> appsListAdapter.trimMemory()

			ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE -> {
			}

			ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW,
			ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL,

			ComponentCallbacks2.TRIM_MEMORY_BACKGROUND,
			ComponentCallbacks2.TRIM_MEMORY_MODERATE,
			ComponentCallbacks2.TRIM_MEMORY_COMPLETE -> {
				appsListAdapter.trimMemory()
			}

			else -> {
			}
		}
	}

	companion object Companion : MyActivityCompanion()

}

//private const val TAG = "SF-MainActivity"