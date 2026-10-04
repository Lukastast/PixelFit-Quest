package com.pixelfitquest.local.deletion

import android.content.Context
import com.pixelfitquest.local.db.PixelFitDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AndroidLocalDataWipe @Inject constructor(
    @ApplicationContext private val context: Context,
    private val db: PixelFitDatabase,
) : LocalDataWipe {

    override suspend fun clearDatabase() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
        }
    }

    override fun clearPreferences(names: List<String>) {
        for (name in names) {
            context.getSharedPreferences(name, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .commit()
        }
    }

    override fun deletePrivateFiles(names: List<String>) {
        for (name in names) {
            File(context.filesDir, name).delete()
        }
    }

    override fun deleteCacheDirectory(name: String) {
        File(context.cacheDir, name).deleteRecursively()
    }
}
