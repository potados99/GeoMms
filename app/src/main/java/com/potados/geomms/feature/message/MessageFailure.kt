package com.potados.geomms.feature.message

import com.potados.geomms.core.exception.Failure

class MessageFailure {
    class QueryFailure : Failure.FeatureFailure()
    class UpdateFailure: Failure.FeatureFailure()
    class DeleteFailure: Failure.FeatureFailure()
    class SendFailure: Failure.FeatureFailure()
}