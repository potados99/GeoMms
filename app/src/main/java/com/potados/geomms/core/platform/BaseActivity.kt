/**
 * BaseActivity.kt
 *
 * Credits to Fernando Cejas.
 * https://github.com/android10/Android-CleanArchitecture-Kotlin
 */
package com.potados.geomms.core.platform

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.potados.geomms.R
import com.potados.geomms.core.extension.inTransaction

abstract class BaseActivity : AppCompatActivity() {

    abstract fun toolBar(): Toolbar?

    abstract fun fragments(): Collection<BaseFragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.base_activity_layout)
        setSupportActionBar(toolBar())
        addFragments(savedInstanceState)
    }

    private fun addFragments(savedInstanceState: Bundle?) =
        savedInstanceState ?:
        supportFragmentManager.inTransaction {
            fragments().forEach {
                add(R.id.fragment_container, it)
            }
            this
        }
}