package com.potados.geomms.common.extension

import java.text.Normalizer

/**
 * Strip the accents from a string
 */
fun CharSequence.removeAccents() = Normalizer.normalize(this, Normalizer.Form.NFD)
