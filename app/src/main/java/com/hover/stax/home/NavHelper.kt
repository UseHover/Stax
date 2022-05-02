package com.hover.stax.home

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
import com.hover.stax.utils.NavUtil

const val NAV_HOME = 600
const val NAV_TRANSFER = 601
const val NAV_AIRTIME = 602
const val NAV_REQUEST = 603
const val NAV_BALANCE = 604
const val NAV_SETTINGS = 605
const val NAV_LINK_ACCOUNT = 606
const val NAV_PAYBILL = 608
const val NAV_EMAIL_CLIENT = 609
const val NAV_USSD_LIB = 610
const val PERMS_REQ_CODE = 700

class NavHelper(val activity: AppCompatActivity) {

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

    fun navigateWellness(tipId: String?) = NavUtil.navigate(getNavController(), MainNavigationDirections.actionGlobalWellnessFragment(tipId))

    fun navigateToBountyList() = NavUtil.navigate(getNavController(), BountyEmailFragmentDirections.actionBountyEmailFragmentToBountyListFragment())

    private fun getNavController(): NavController = navHostFragment!!.navController

    private fun setNavClickListener(nav: BottomNavigationView) {
        nav.setOnItemSelectedListener {
            checkPermissionsAndNavigate(getNavDirections(it.itemId))
            true
        }
        nav.setOnItemReselectedListener {
            if (getNavController().currentDestination?.id != it.itemId) {
                checkPermissionsAndNavigate(getNavDirections(it.itemId))
            }
        }
    }

    private fun setDestinationChangeListener(nav: BottomNavigationView) = navController?.let {
        it.addOnDestinationChangedListener { _, destination, _ ->
            nav.visibility = if (destination.id == R.id.navigation_linkAccount) View.GONE else View.VISIBLE

            if (destination.id == R.id.bountyEmailFragment || destination.id == R.id.bountyListFragment)
                nav.menu.findItem(R.id.navigation_settings).isChecked = true
        }
    }

    fun checkPermissionsAndNavigate(toWhere: Int) = checkPermissionsAndNavigate(getNavDirections(toWhere))

    fun checkPermissionsAndNavigate(navDirections: NavDirections?) = navDirections?.let {
        val permissionHelper = PermissionHelper(activity)

        val exemptRoutes = setOf(
            MainNavigationDirections.actionGlobalNavigationSettings(),
            MainNavigationDirections.actionGlobalNavigationHome(), MainNavigationDirections.actionGlobalLibraryFragment()
        )

        when {
            exemptRoutes.contains(it) || permissionHelper.hasBasicPerms() -> NavUtil.navigate(getNavController(), it)
            else -> PermissionUtils.showInformativeBasicPermissionDialog(
                0,
                { PermissionUtils.requestPerms(PERMS_REQ_CODE, activity) },
                { AnalyticsUtil.logAnalyticsEvent(activity.getString(R.string.perms_basic_cancelled), activity) }, activity
            )
        }
    }

    private fun getNavDirections(destId: Int): NavDirections? = when (destId) {
        R.id.navigation_request, NAV_REQUEST -> MainNavigationDirections.actionGlobalNavigationRequest()
        R.id.navigation_settings, NAV_SETTINGS -> MainNavigationDirections.actionGlobalNavigationSettings()
        R.id.navigation_home, NAV_HOME -> MainNavigationDirections.actionGlobalNavigationHome()
        R.id.libraryFragment, NAV_USSD_LIB -> MainNavigationDirections.actionGlobalLibraryFragment()
        R.id.navigation_balance, NAV_BALANCE -> MainNavigationDirections.actionGlobalNavigationBalance()
        NAV_TRANSFER -> MainNavigationDirections.actionGlobalTransferFragment(HoverAction.P2P)
        NAV_AIRTIME -> MainNavigationDirections.actionGlobalTransferFragment(HoverAction.AIRTIME)
        NAV_LINK_ACCOUNT -> MainNavigationDirections.actionGlobalAddChannelsFragment()
        NAV_PAYBILL -> MainNavigationDirections.actionGlobalPaybillFragment(false)
        else -> null //invalid or unmapped route, return nothing
    }
}