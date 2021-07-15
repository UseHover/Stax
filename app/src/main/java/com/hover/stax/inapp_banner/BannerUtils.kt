package com.hover.stax.inapp_banner

import android.content.Context
import com.hover.sdk.permissions.PermissionHelper
import com.hover.stax.utils.DateUtils
import com.hover.stax.utils.Utils
import java.util.*
import kotlin.properties.Delegates

class BannerUtils(val context: Context) {
    private val FIRST_WEEK = 1;
    private val SECOND_WEEK = 2;
    private val THIRD_WEEK = 3
    private val FOURTH_WEEK = 4;

    private val TAG: String = "StaxBanner"
    private val IMP: String = "StaxBannerImpressions"
    private val LAST_GIST_IMP_DATE: String = "StaxBannerGistLastShown"
    private val LAST_UPVOTE_IMP_DATE: String = "StaxBannerUpvoteLastShown"
    private val PERM_CLICKED = "PermBannerClicked";
    private val GENERAL_LAST_IMP_DATE = "AnyBannerLastImpDate"



    private fun setLastBanner(bannerId: Int) {
        Utils.saveInt(TAG, bannerId, context)
        bannerId_in_cache = bannerId
    }

    private fun lastBanner(): Int = Utils.getInt(TAG, context)

    private fun getImpression(): Int = Utils.getInt(IMP, context)
    private fun decrementImpression() = Utils.saveInt(IMP, getImpression() - 1, context)


    private fun renewImpression(num: Int) = Utils.saveInt(IMP, num, context)

    private fun campaignRunning(): Boolean = getImpression() > 0

    private fun lastGistImpressionDate() = Utils.getLong(LAST_GIST_IMP_DATE, context)
    private fun setLastGistImpressionDate(value: Long) = Utils.saveLong(LAST_GIST_IMP_DATE, value, context)
    private fun lastUpvoteImpressionDate() = Utils.getLong(LAST_UPVOTE_IMP_DATE, context)
    private fun setLastUpvoteImpressionDate(value: Long) = Utils.saveLong(LAST_UPVOTE_IMP_DATE, value, context)

    private fun invalidatePermissionCampaign() = Utils.saveBoolean(PERM_CLICKED, true, context)
    private fun isPermissionCampaignValid() = !Utils.getBoolean(PERM_CLICKED, context) && areCampaignsUnlocked()
    private fun areCampaignsUnlocked():Boolean = Utils.getInt(APP_SESSIONS, context) >= 3

    private fun setGeneralLastImpressionDate() = Utils.saveLong(GENERAL_LAST_IMP_DATE, DateUtils.today(), context)
    private fun generalLastImpressionDate() = Utils.getLong(GENERAL_LAST_IMP_DATE, context)

    private fun withinDuration(mainDate: Date, dateStart: Date, dateEnd: Date) = mainDate in dateStart..dateEnd

    private fun run(bannerId: Int) : Banner? {
       return run(bannerId, isNewCampaign = false, updateImpression = false)
    }
    private fun run(bannerId: Int, isNewCampaign: Boolean, updateImpression: Boolean): Banner? {
        var banner: Banner? = null;
        with(Banner) {
            when (bannerId) {
                PERMISSION -> banner = if (isPermissionCampaignValid()) permissions() else null
                ROUND_UP_NEWS -> banner = roundupNews()
                GIST -> banner = gist()
                UPVOTE -> banner = upvote()
            }
        }

        banner?.let { if (updateImpression) updateImpressionMeta(it, bannerId, isNewCampaign) }

        return banner
    }

    private fun updateImpressionMeta(banner: Banner, bannerId: Int, isNewCampaign: Boolean) {
        with(DateUtils.today()) {
            if (bannerId == Banner.GIST) setLastGistImpressionDate(this)
            else if (bannerId == Banner.UPVOTE) setLastUpvoteImpressionDate(this)
        }

        setLastBanner(bannerId)
        if (isNewCampaign) renewImpression(banner.impressions - 1) else decrementImpression()
        setGeneralLastImpressionDate()
    }

    private fun enforceCampaignLimit(bannerId: Int): Int {
        fun isNewCampaign(): Boolean = bannerId != lastBanner()
        fun hasAnyCampaignMadeToday(): Boolean = DateUtils.todayDate() == DateUtils.getDate(generalLastImpressionDate())
        return if (isNewCampaign() && hasAnyCampaignMadeToday()) 0 else bannerId
    }

    private fun permissionQualifies(): Boolean = !(PermissionHelper(context).hasBasicPerms() && PermissionHelper(context).hasHardPerms())
    private fun roundUpNewsQualifies(): Boolean = DateUtils.todayDate() == DateUtils.getDate(Calendar.THURSDAY, FIRST_WEEK)
    private fun gistQualifies(): Boolean {
        with(DateUtils) {
            return if (withinDuration(todayDate(), beginningOfTheMonth(), getDate(SECOND_WEEK))) {
                !withinDuration(getDate(lastGistImpressionDate()), beginningOfTheMonth(), getDate(SECOND_WEEK))
            } else {
                !withinDuration(getDate(lastGistImpressionDate()), getDate(SECOND_WEEK), getDate(FOURTH_WEEK))
            }
        }
    }

    private fun upvoteQualifies(): Boolean {
        with(DateUtils) {
            return when {
                withinDuration(todayDate(), beginningOfTheMonth(), getDate(FIRST_WEEK)) -> {
                    !withinDuration(getDate(lastUpvoteImpressionDate()), beginningOfTheMonth(), getDate(FIRST_WEEK))
                }
                withinDuration(todayDate(), getDate(FIRST_WEEK), getDate(SECOND_WEEK)) -> {
                    !withinDuration(getDate(lastUpvoteImpressionDate()), getDate(FIRST_WEEK), getDate(SECOND_WEEK))
                }
                withinDuration(todayDate(), getDate(SECOND_WEEK), getDate(THIRD_WEEK)) -> {
                    !withinDuration(getDate(lastUpvoteImpressionDate()), getDate(SECOND_WEEK), getDate(THIRD_WEEK))
                }
                else -> !withinDuration(getDate(lastUpvoteImpressionDate()), getDate(THIRD_WEEK), getDate(FOURTH_WEEK))
            }
        }
    }

    private fun researchQualifies(): Boolean {

    }

    fun getQualifiedBanner(hasTransactionLastMonth: Boolean): Banner? {
        if(!areCampaignsUnlocked()) return run(0)
        if (bannerId_in_cache > 0) return run(bannerId_in_cache, isNewCampaign = false, updateImpression = false)
        if (campaignRunning()) return run(lastBanner(), isNewCampaign = false, updateImpression = true)

        var bannerId = 0;
        when {
            permissionQualifies() && isPermissionCampaignValid() -> bannerId = Banner.PERMISSION
            roundUpNewsQualifies() -> bannerId = Banner.ROUND_UP_NEWS
            gistQualifies() -> bannerId = Banner.GIST
            upvoteQualifies() -> bannerId = Banner.UPVOTE
        }
        bannerId = enforceCampaignLimit(bannerId)

        return run(bannerId, isNewCampaign = true, updateImpression = true)
    }

    fun closeCampaign(bannerId: Int) {
        if (bannerId == Banner.PERMISSION) invalidatePermissionCampaign()
        renewImpression(0)
        bannerId_in_cache = 0
    }

    companion object {
        var bannerId_in_cache: Int by Delegates.observable(0) { _, _, _ -> }
        val APP_SESSIONS = "AppSessions"
    }
}

