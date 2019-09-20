package com.potados.geomms.feature.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.R
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.databinding.ConnectionDetailFragmentBinding

class ConnectionDetailFragment : BaseFragment() {

    private lateinit var viewDataBinding: ConnectionDetailFragmentBinding
    private lateinit var connectionDetailViewModel: ConnectionDetailViewModel

    init {
        failables += this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionDetailViewModel = getViewModel {
            startWithArguments(arguments)
        }
        failables += connectionDetailViewModel.failables
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return ConnectionDetailFragmentBinding
            .inflate(inflater)
            .apply { lifecycleOwner = this@ConnectionDetailFragment }
            .apply { vm = connectionDetailViewModel }
            .apply { viewDataBinding = this }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {

    }

    companion object {
        const val ARG_CONNECTION_ID = "connection_id"

        fun ofConnection(connectionId: Long): ConnectionDetailFragment {
            return ConnectionDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_CONNECTION_ID, connectionId)
                }
            }
        }
    }
}