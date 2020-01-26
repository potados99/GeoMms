/*
 * Copyright (C) 2019 Song Byeong Jun <potados99@gmail.com>
 *
 * This file is part of GeoMms.
 *
 * GeoMms is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * GeoMms is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with GeoMms.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.potados.geomms.feature.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.github.paolorotolo.appintro.AppIntro
import com.github.paolorotolo.appintro.AppIntroFragment
import com.github.paolorotolo.appintro.model.SliderPagerBuilder
import com.potados.geomms.R
import com.potados.geomms.common.extension.resolveColor
import com.potados.geomms.common.extension.resolveThemeColor

class OnboardingActivity : AppIntro() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = resolveThemeColor(R.attr.tintPrimary)

        addSlide(
            AppIntroFragment.newInstance(SliderPagerBuilder()
                .title(getString(R.string.title_geomms_is))
                .description(getString(R.string.description_geomms_is))
                .imageDrawable(R.drawable.ic_geomms_bw)
                .bgColor(resolveThemeColor(R.attr.tintPrimary))
                .titleColor(resolveColor(R.color.white))
                .descColor(resolveColor(R.color.white))
                .build())
        )

        addSlide(
            AppIntroFragment.newInstance(SliderPagerBuilder()
                .title(getString(R.string.title_dont_ask_where))
                .description(getString(R.string.description_dont_ask_where))
                .imageDrawable(R.drawable.onboard_1)
                .bgColor(resolveThemeColor(R.attr.tintPrimary))
                .titleColor(resolveColor(R.color.white))
                .descColor(resolveColor(R.color.white))
                .build())
        )

        addSlide(
            AppIntroFragment.newInstance(SliderPagerBuilder()
                .title(getString(R.string.title_in_app_message))
                .description(getString(R.string.description_in_app_message))
                .imageDrawable(R.drawable.onboard_3)
                .bgColor(resolveThemeColor(R.attr.tintPrimary))
                .titleColor(resolveColor(R.color.white))
                .descColor(resolveColor(R.color.white))
                .build())
        )

        addSlide(
            AppIntroFragment.newInstance(SliderPagerBuilder()
                .title(getString(R.string.title_privacy))
                .description(getString(R.string.description_privacy))
                .imageDrawable(R.drawable.ic_privacy)
                .bgColor(resolveThemeColor(R.attr.tintPrimary))
                .titleColor(resolveColor(R.color.white))
                .descColor(resolveColor(R.color.white))
                .build())
        )
    }

    override fun onSkipPressed(currentFragment: Fragment?) {
        super.onSkipPressed(currentFragment)
        finish()
    }

    override fun onDonePressed(currentFragment: Fragment?) {
        super.onDonePressed(currentFragment)
        finish()
    }

    override fun onStop() {
        super.onStop()

        PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
            putBoolean(ALREADY_SHOWN, true)
            apply()
        }
    }

    companion object {
        fun callingIntent(context: Context) = Intent(context, OnboardingActivity::class.java)

        val ALREADY_SHOWN = "onboarding_activity_has_been_shown"
    }
}