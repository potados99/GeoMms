package com.potados.geomms.feature.failure

import com.potados.geomms.core.exception.Failure

class MessageFailure {
    class QueryFailure : Failure.FeatureFailure()
    class UpdateFailure: Failure.FeatureFailure()
    class SendFailure: Failure.FeatureFailure()
}