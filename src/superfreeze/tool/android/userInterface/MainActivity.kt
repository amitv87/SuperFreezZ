/*
Copyright (c) 2015 axxapy
Copyright (c) 2018 Hocceruser

This file is part of SuperFreeze.

SuperFreeze is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

SuperFreeze is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with SuperFreeze.  If not, see <http://www.gnu.org/licenses/>.
*/

package superfreeze.tool.android.userInterface

import android.annotation.SuppressLint
import android.app.SearchManager
import android.app.usage.UsageStats
import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.SearchView
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.ProgressBar
import kotlinx.android.synthetic.main.activity_main.*
import superfreeze.tool.android.R
import superfreeze.tool.android.backend.FreezerService
import superfreeze.tool.android.backend.freezeAll
import superfreeze.tool.android.backend.getAggregatedUsageStats
import superfreeze.tool.android.backend.getRunningApplications

/**
 * The activity that is shown at startup
 */
class MainActivity : AppCompatActivity() {
	private lateinit var appsListAdapter: AppsListAdapter

	private lateinit var progressBar: ProgressBar

	private val usageStatsMap: Map<String, UsageStats>? by lazy {
		getAggregatedUsageStats(this)
	}

	override fun onCreate(savedInstanceState: Bundle?) {

		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		setSupportActionBar(toolbar)

		val listView = list

		appsListAdapter = AppsListAdapter(this)
		listView.layoutManager = LinearLayoutManager(this)
		listView.adapter = appsListAdapter

		progressBar = progress
		progressBar.visibility = View.VISIBLE

		requestUsageStatsPermission(this) {
			loadRunningApplications()
		}
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
		searchView.setOnQueryTextFocusChangeListener { _, queryTextFocused ->
			if (!queryTextFocused && searchView.query.isEmpty()) {
				val supportActionBar = supportActionBar
				if(supportActionBar != null) {
					supportActionBar.collapseActionView()
				} else {
					Log.e("SuperFreezeUI", "There is no action bar")
				}
			}
		}
		searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
			override fun onQueryTextSubmit(s: String): Boolean {
				return false
			}

			override fun onQueryTextChange(s: String): Boolean {
				appsListAdapter.searchPattern = s
				return true
			}
		})
		fab.setOnClickListener {
			val freezeNext = freezeAll(applicationContext, appsListAdapter.listPendingFreeze, this)
			doOnResume(freezeNext)
		}
		return super.onCreateOptionsMenu(menu)
	}

	override fun onResume() {
		super.onResume()

		//Execute all tasks and retain only those that returned true.
		toBeDoneOnResume.retainAll { it() }

		if (!FreezerService.busy()) {
			appsListAdapter.refresh(getAggregatedUsageStats(this))
		}
	}


	override fun onConfigurationChanged(newConfig: Configuration?) {
		super.onConfigurationChanged(newConfig)

		//This is necessary so that the list items change their look when the screen is rotated.
		val listView = list
		val position = (listView.layoutManager as LinearLayoutManager).findFirstVisibleItemPosition()
		listView.adapter = null
		listView.layoutManager = null
		listView.recycledViewPool.clear()
		listView.adapter = appsListAdapter
		listView.layoutManager = LinearLayoutManager(this)
		appsListAdapter.notifyDataSetChanged()
		listView.layoutManager.scrollToPosition(position)
	}

	companion object {
		private val toBeDoneOnResume: MutableList<() -> Boolean> = mutableListOf()
		/**
		 * Execute this task on resume.
		 * @param task If it returns true, then it will be executed again at the next onResume.
		 */
		internal fun doOnResume(task: ()->Boolean) {
			toBeDoneOnResume.add(task)
		}
	}



	private fun loadRunningApplications() {

		Thread {
			val packages = getRunningApplications(applicationContext)

			runOnUiThread {
				appsListAdapter.setAndLoadItems(packages, usageStatsMap)
			}
		}.start()

	}
}