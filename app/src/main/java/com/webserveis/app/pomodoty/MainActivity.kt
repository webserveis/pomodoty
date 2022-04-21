package com.webserveis.app.pomodoty

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.webserveis.app.pomodoty.core.ext.isServiceRunning
import com.webserveis.app.pomodoty.core.ext.setPortraitSmartphone
import com.webserveis.app.pomodoty.databinding.ActivityMainBinding
import com.webserveis.app.pomodoty.preferences.SettingsActivity
import com.webserveis.app.pomodoty.ui.pomotimer.MaterialAlertDialog
import com.webserveis.app.ratethisapp.RateThisApp

class MainActivity : AppCompatActivity() {

    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val resultSettings = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // There are no request codes
            //val data: Intent? = result.data
            Log.i(TAG, "ResultCode by SettingsActivity: RESULT_OK")
            //recreate()
            invalidateOptionsMenu()

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.setPortraitSmartphone()

        binding = ActivityMainBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar)
        }

        setupNavigation()
        //iniAdvertisements()

        setupRateApp()
    }

    private fun setupNavigation() {
        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    /*private fun setupNavigation() {
        val host: NavHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = host.navController
        //appBarConfiguration = AppBarConfiguration(navController.graph)
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_list, R.id.nav_list
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        binding.contentMain.bottomNavView.setupWithNavController(navController)

        binding.contentMain.bottomNavView.setOnItemSelectedListener { item ->
            if (item.itemId != binding.contentMain.bottomNavView.selectedItemId)
                NavigationUI.onNavDestinationSelected(item, navController)
            true
        }

    }*/

    private fun setupRateApp() {
        val rateThisAppDialog = RateThisApp(this)
        val builder = RateThisApp.Builder(3, 5)
        rateThisAppDialog.init(builder)
        rateThisAppDialog.showRateDialogIfNeeded()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_settings -> {

                if (isServiceRunning(MyService::class.java)) {
                    showPomodoroCloseDialog()
                } else {
                    val intent = Intent(this, SettingsActivity::class.java)
                    resultSettings.launch(intent)
                }
                //navController.navigate(R.id.settingsActivity)
                /*val intent = Intent(this, SettingsActivity::class.java)
                resultSettings.launch(intent)*/
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    private fun showPomodoroCloseDialog() {
        val navHostFragment = supportFragmentManager.fragments.first() as NavHostFragment
        if (navHostFragment.childFragmentManager.findFragmentByTag("POMODORO_CLOSE_DIALOG") != null) return

        val dialog = MaterialAlertDialog()

        dialog.isCancelable = false
        dialog.show(navHostFragment.childFragmentManager, "POMODORO_CLOSE_DIALOG")
    }

}