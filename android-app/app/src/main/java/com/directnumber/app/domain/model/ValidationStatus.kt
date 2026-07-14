package com.directnumber.app.domain.model

enum class ValidationStatus {
    VALID,
    INCOMPLETE,
    INVALID,
    NEEDS_REVIEW,
}

enum class NumberType {
    MOBILE,
    LANDLINE,
    UNKNOWN,
}
