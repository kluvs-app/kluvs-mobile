package com.ivangarzab.kluvs.appfunctions

import androidx.annotation.RequiresApi
import androidx.appfunctions.AppFunctionDeclaration
import androidx.appfunctions.AppFunctionService
import androidx.appfunctions.AppFunctionServiceEntryPoint
import com.ivangarzab.kluvs.appfunctions.clubs.MyClub
import com.ivangarzab.kluvs.appfunctions.clubs.listMyClubs
import org.koin.android.ext.android.get

/**
 * The app's single AppFunctions entry point — Android allows only one service per app to
 * handle AppFunctions, so every `@AppFunctionDeclaration` lives here as a thin delegate into
 * its own file under this package. Wrap each body in [appFunction].
 */
@RequiresApi(36)
@AppFunctionServiceEntryPoint(
    serviceName = "KluvsAppFunctionService",
    appFunctionXmlFileName = "kluvs_app_function_service",
)
abstract class BaseKluvsAppFunctionService : AppFunctionService() {

    internal val session: AppFunctionSession by lazy { AppFunctionSession(get(), get()) }

    /**
     * Lists the book clubs the signed-in user belongs to, with each club's ID and the user's role in it.
     *
     * @return The user's clubs; empty if they haven't joined any.
     */
    @AppFunctionDeclaration(isDescribedByKDoc = true)
    suspend fun listMyClubs(): List<MyClub> = appFunction("listMyClubs") { listMyClubs(session) }
}
