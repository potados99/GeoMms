package com.potados.geomms.feature.common

import org.koin.core.KoinComponent
import org.koin.core.inject
import java.io.Serializable

class Person(
    val phoneNumber: String
) : Serializable, KoinComponent {
    private val contactRepository: ContactRepository by inject()

    val contactName get() = contactRepository.getContactNameByPhoneNumber(phoneNumber)

    val imageUri get() = contactRepository.getContactPhotoUriByPhoneNumber(phoneNumber)
}