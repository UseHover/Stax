package com.hover.stax.home

import android.content.Intent
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDirections
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.hover.sdk.actions.HoverAction
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.MainNavigationDirections
import com.hover.stax.R
import com.hover.stax.bounties.BountyEmailFragmentDirections
import com.hover.stax.permissions.PermissionUtils
import com.hover.stax.utils.AnalyticsUtil
import com.hover.stax.utils.Constants
import com.hover.stax.utils.NavUtil
import com.hover.stax.utils.Utils

class NavHelper(val activity: AppCompatActivity, private val isMainActivity: Boolean) {

    private var navController: NavController? = null
    private var appBarConfiguration: AppBarConfiguration? = null
    private var navHostFragment: NavHostFragment? = null

    fun setUpNav() {
        val nav = activity.findViewById<BottomNavigationView>(R.id.nav_view)
        navHostFragment = activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment

        navHostFragment?.let {
            navController = getNavController()
            NavigationUI.setupWithNavController(nav, navController!!)
            appBarConfiguration = AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_balance, R.id.navigation_request, R.id.libraryFragment, R.id.navigation_settings
            ).build()
        }

        setNavClickListener(nav)
        setDestinationChangeListener(nav)
    }

    fun handleDeeplink(intent: Intent) = getNavController().handleDeepLink(intent)

    fun navigateAccountDetails(accountId: Int) = NavUtil.navigate(getNavController(), HomeFragmentDirections.actionNavigationHomeToAccountDetailsFragment(accountId))

    fun navigateWellness(tipId: String?) = NavUtil.navigate(getNavController(), MainNavigationDirections.actionGlobalWellnessFragment(tipId))

    fun navigateToBountyList() = NavUtil.navigate(getNavController(), BountyEmailFragmentDirections.actionBountyEmailFragmentToBountyListFragment())

    private fun getNavController(): NavController = navHostFragment!!.navController

    private fun setNavClickListener(nav: BottomNavigationView) {
        nav.setOnNavigationItemSelectedListener {
            when {
                isMainActivity -> checkPermissionsAndNavigate(getNavDirections(it.itemId))
                it.itemId == R.id.navigation_home -> activity.onBackPressed()
                else -> NavUtil.navigate(getNavController(), getNavDirections(it.itemId))
            }
            true
        }
        nav.setOnItemReselectedListener { navController?.popBackStack() }
    }

    private fun setDestinationChangeListener(nav: BottomNavigationView) = navController?.let {
        it.addOnDestinationChangedListener { _, destination, _ ->
            nav.visibility = if (destination.id == R.id.navigation_linkAccount) View.GONE else View.VISIBLE

            if (destination.id == R.id.bountyEmailFragment || destination.id == R.id.bountyListFragment)
                nav.menu.findItem(R.id.navigation_settings).isChecked = true
        }
    }

    fun checkPermissionsAndNavigate(toWhere: Int) = checkPermissionsAndNavigate(getNavDirections(toWhere))

    fun checkPermissionsAndNavigate(navDirections: NavDirections) {
        val permissionHelper = PermissionHelper(activity)

        when {
            navDirections == MainNavigationDirections.actionGlobalNavigationSettings() ||
                    navDirections == MainNavigationDirections.actionGlobalNavigationHome() ||
                    permissionHelper.hasBasicPerms() -> NavUtil.navigate(getNavController(), navDirections)
            else -> PermissionUtils.showInformativeBasicPermissionDialog(
                0,
                { PermissionUtils.requestPerms(Constants.PERMS_REQ_CODE, activity) },
                { AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.perms_basic_cancelled), activity) }, activity
            )
        }
    }

    private fun getNavDirections(destId: Int): NavDirections = when (destId) {
        R.id.navigation_request, Constants.NAV_REQUEST -> MainNavigationDirections.actionGlobalNavigationRequest()
        R.id.navigation_settings, Constants.NAV_SETTINGS -> MainNavigationDirections.actionGlobalNavigationSettings()
        R.id.navigation_home, Constants.NAV_HOME -> MainNavigationDirections.actionGlobalNavigationHome()
        R.id.libraryFragment, Constants.NAV_USSD_LIB -> MainNavigationDirections.actionGlobalLibraryFragment()
        R.id.navigation_balance, Constants.NAV_BALANCE -> MainNavigationDirections.actionGlobalNavigationBalance()
        Constants.NAV_TRANSFER -> MainNavigationDirections.actionGlobalTransferFragment(HoverAction.P2P)
        Constants.NAV_AIRTIME -> MainNavigationDirections.actionGlobalTransferFragment(HoverAction.AIRTIME)
        Constants.NAV_LINK_ACCOUNT -> MainNavigationDirections.actionGlobalAddChannelsFragment()
        Constants.NAV_PAYBILL -> MainNavigationDirections.actionGlobalPaybillFragment(false)
        else -> MainNavigationDirections.actionGlobalNavigationHome()
    }
}