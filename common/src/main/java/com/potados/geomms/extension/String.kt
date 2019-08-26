package com.potados.geomms.extension

import java.text.Normalizer

/**
 * Strip the accents from a string
 */
fun CharSequence.removeAccents() = Normalizer.normalize(this, Normalizer.Form.NFD)
