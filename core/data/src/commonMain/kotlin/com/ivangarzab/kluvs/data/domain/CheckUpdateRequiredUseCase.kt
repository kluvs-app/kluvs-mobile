package com.ivangarzab.kluvs.data.domain

import com.ivangarzab.bark.Bark
import com.ivangarzab.kluvs.data.repositories.ConfigRepository

/**
 * UseCase that decides whether the installed app is too old for the backend and must be updated.
 *
 * Fails open: the answer is `false` (let the user in) whenever it can't be decided — the config
 * request fails, the backend sets no minimum, or either version can't be parsed.
 *
 * @param configRepository Repository for the backend's client configuration
 * @param installedVersion This app's release version (MAJOR.MINOR.PATCH)
 */
class CheckUpdateRequiredUseCase(
    private val configRepository: ConfigRepository,
    private val installedVersion: String
) {
    /**
     * @return true only when the installed version is below the backend's minimum supported version
     */
    suspend operator fun invoke(): Boolean {
        val config = configRepository.getConfig().getOrElse { error ->
            Bark.w("Could not check minimum supported version. Letting the user in.", error)
            return false
        }
        val minimum = config.minSupportedVersion ?: return false

        val installed = AppVersion.parse(installedVersion)
        val required = AppVersion.parse(minimum)
        if (installed == null || required == null) {
            Bark.w("Unparseable version (installed: $installedVersion, minimum: $minimum). Letting the user in.")
            return false
        }

        val updateRequired = installed < required
        if (updateRequired) {
            Bark.i("Update required (installed: $installedVersion, minimum: $minimum)")
        }
        return updateRequired
    }
}
