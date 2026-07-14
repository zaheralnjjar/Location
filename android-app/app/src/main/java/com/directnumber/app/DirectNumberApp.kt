package com.directnumber.app

import android.app.Application
import com.directnumber.app.data.repository.CountryRepository
import com.directnumber.app.data.repository.RecentsRepository
import com.directnumber.app.data.repository.SettingsRepository
import com.directnumber.app.domain.usecase.BuildWhatsAppLinkUseCase
import com.directnumber.app.domain.usecase.GenerateVCardUseCase
import com.directnumber.app.domain.usecase.ValidatePhoneNumberUseCase
import com.directnumber.app.domain.validator.PhoneNumberValidator

/**
 * Small hand-rolled service locator — this app has too few dependencies to justify pulling
 * in a DI framework. Everything is created once and reused for the process lifetime.
 */
class DirectNumberApp : Application() {

    val settingsRepository: SettingsRepository by lazy { SettingsRepository(this) }
    val recentsRepository: RecentsRepository by lazy { RecentsRepository(this) }

    private val phoneNumberValidator: PhoneNumberValidator by lazy { PhoneNumberValidator() }
    val countryRepository: CountryRepository by lazy { CountryRepository(phoneNumberValidator) }

    val validatePhoneNumberUseCase: ValidatePhoneNumberUseCase by lazy { ValidatePhoneNumberUseCase(phoneNumberValidator) }
    val buildWhatsAppLinkUseCase: BuildWhatsAppLinkUseCase by lazy { BuildWhatsAppLinkUseCase() }
    val generateVCardUseCase: GenerateVCardUseCase by lazy { GenerateVCardUseCase() }
}
