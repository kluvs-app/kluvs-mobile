package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.model.BookEnrichment

/**
 * Local data source for cached book enrichment responses, keyed by request
 * (see `bookEnrichmentCacheKey`).
 */
interface BookEnrichmentLocalDataSource {
    suspend fun getEnrichment(cacheKey: String): BookEnrichment?
    suspend fun insertEnrichment(cacheKey: String, enrichment: BookEnrichment)
    suspend fun getLastFetchedAt(cacheKey: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [BookEnrichmentLocalDataSource] using Room database.
 */
class BookEnrichmentLocalDataSourceImpl(
    database: KluvsDatabase
) : BookEnrichmentLocalDataSource {

    private val dao = database.bookEnrichmentDao()

    override suspend fun getEnrichment(cacheKey: String): BookEnrichment? {
        return dao.getEnrichment(cacheKey)?.toDomain()
    }

    override suspend fun insertEnrichment(cacheKey: String, enrichment: BookEnrichment) {
        dao.insertEnrichment(enrichment.toEntity(cacheKey))
    }

    override suspend fun getLastFetchedAt(cacheKey: String): Long? {
        return dao.getLastFetchedAt(cacheKey)
    }

    override suspend fun deleteAll() {
        dao.deleteAll()
    }
}
