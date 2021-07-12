package com.hover.stax.inapp_banner

import com.hover.stax.R

class Banner
private constructor(val title:Int, val desc:Int?, val cta: Int, val iconRes: Int, val primaryColor: Int, val secondaryColor: Int, val url:Int)  {
    companion  object {
       fun permissions() : Banner {
           return Banner(
                   R.string.banner_permission_title,
                   R.string.banner_permission_desc,
                   R.string.banner_permission_cta,
                   R.drawable.banner_permission,
                   R.color.banner_permission_primary,
                   R.color.banner_secondary,
                   R.string.banner_permission_url )
       }

       fun roundupNews() : Banner {
           return Banner(
                   R.string.banner_roundupnews_title,
                   null,
                   R.string.banner_roundupnews_cta,
                   R.drawable.banner_roundupnews,
                   R.color.offWhite,
                   R.color.banner_secondary,
                   R.string.banner_roundupnews_url
                )
       }

       fun gist() : Banner {
           return Banner(
                   R.string.banner_gist_title,
                   null,
                   R.string.banner_gist_cta,
                   R.drawable.banner_gist,
                   R.color.banner_gist_primary,
                   R.color.banner_secondary,
                   R.string.banner_gist_url
           )
       }

       fun upvote() : Banner {
           return Banner(
                   R.string.banner_upvote_title,
                   null,
                   R.string.banner_upvote_cta,
                   R.drawable.banner_upvote,
                   R.color.banner_secondary,
                   R.color.offWhite,
                   R.string.banner_upvoteurl
           )
       }

    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Banner

        if (title != other.title) return false
        if (desc != other.desc) return false
        if (cta != other.cta) return false
        if (iconRes != other.iconRes) return false
        if (primaryColor != other.primaryColor) return false
        if (secondaryColor != other.secondaryColor) return false
        if (url != other.url) return false

        return true
    }

    override fun hashCode(): Int {
        var result = title
        result = 31 * result + (desc ?: 0)
        result = 31 * result + cta
        result = 31 * result + iconRes
        result = 31 * result + primaryColor
        result = 31 * result + secondaryColor
        result = 31 * result + url
        return result
    }


}

