package com.pixelfitquest.local.deletion

/**
 * Device storage the deletion service can clear. Android implements this;
 * tests use a fake.
 */
interface LocalDataWipe {
    suspend fun clearDatabase()
    fun clearPreferences(names: List<String>)
    fun deletePrivateFiles(names: List<String>)
    fun deleteCacheDirectory(name: String)
}
