package com.potados.geomms.feature.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.potados.geomms.common.base.BaseFragment
import com.potados.geomms.common.extension.getViewModel
import com.potados.geomms.databinding.ConnectionDetailFragmentBinding
import com.potados.geomms.databinding.RequestDetailFragmentBinding
import kotlinx.android.synthetic.main.request_detail_fragment.view.*

class RequestDetailFragment : BaseFragment() {

    private lateinit var viewDataBinding: RequestDetailFragmentBinding
    private lateinit var requestDetailViewModel: RequestDetailViewModel

    init {
        failables += this
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestDetailViewModel = getViewModel {
            startWithArguments(this@RequestDetailFragment, arguments)
        }
        failables += requestDetailViewModel.failables
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return RequestDetailFragmentBinding
            .inflate(inflater)
            .apply { lifecycleOwner = this@RequestDetailFragment }
            .apply { vm = requestDetailViewModel }
            .apply { viewDataBinding = this }
            .apply { initializeView(root) }
            .root
    }

    private fun initializeView(view: View) {
        with(view.accept) {
            setOnClickListener {
                requestDetailViewModel.onAccept()
            }
        }

        with(view.refuse) {
            setOnClickListener {
                requestDetailViewModel.onRefuse()
            }
        }
    }

    companion object {
        const val ARG_REQUEST_ID = "request_id"

        fun ofIncomingRequest(requestId: Long): RequestDetailFragment {
            return RequestDetailFragment().apply {
                arguments = Bundle().apply {
                    putLong(ARG_REQUEST_ID, requestId)
                }
            }
        }
    }
}