package com.ivangarzab.kluvs.data.local.mappers

import com.ivangarzab.kluvs.database.entities.BookEnrichmentEntity
import com.ivangarzab.kluvs.model.Author
import com.ivangarzab.kluvs.model.Book
import com.ivangarzab.kluvs.model.BookEnrichment
import com.ivangarzab.kluvs.model.BookVolumeInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

// Domain models stay serialization-free; these private mirrors define the stored JSON shape.
// Defaults on every field keep older rows decodable if a field is added later.
private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class StoredVolumeInfo(
    val subtitle: String? = null,
    val publisher: String? = null,
    val description: String? = null,
    val categories: List<String> = emptyList(),
    val language: String? = null,
    val isbn13: String? = null,
    val isbn10: String? = null
)

@Serializable
private data class StoredAuthor(
    val name: String? = null,
    val description: String? = null,
    val imageUrl: String? = null,
    val bio: String? = null
)

@Serializable
private data class StoredBook(
    val id: String,
    val title: String,
    val author: String,
    val edition: String? = null,
    val year: Int? = null,
    val isbn: String? = null,
    val pageCount: Int? = null,
    val imageUrl: String? = null,
    val externalGoogleId: String? = null
)

/**
 * Builds the cache key for an enrichment request. Requests with only an author (or only a
 * Google id) are valid, so both parts are always included.
 */
fun bookEnrichmentCacheKey(externalGoogleId: String?, author: String?): String =
    "${externalGoogleId.orEmpty()}|${author.orEmpty()}"

/**
 * Maps a [BookEnrichmentEntity] to a [BookEnrichment] domain model.
 */
fun BookEnrichmentEntity.toDomain(): BookEnrichment {
    return BookEnrichment(
        volumeInfo = volumeInfoJson?.let { json.decodeFromString<StoredVolumeInfo>(it) }?.let {
            BookVolumeInfo(
                subtitle = it.subtitle,
                publisher = it.publisher,
                description = it.description,
                categories = it.categories,
                language = it.language,
                isbn13 = it.isbn13,
                isbn10 = it.isbn10
            )
        },
        author = authorJson?.let { json.decodeFromString<StoredAuthor>(it) }?.let {
            Author(
                name = it.name,
                description = it.description,
                imageUrl = it.imageUrl,
                bio = it.bio
            )
        },
        authorBooks = json.decodeFromString(ListSerializer(StoredBook.serializer()), authorBooksJson)
            .map {
                Book(
                    id = it.id,
                    title = it.title,
                    author = it.author,
                    edition = it.edition,
                    year = it.year,
                    isbn = it.isbn,
                    pageCount = it.pageCount,
                    imageUrl = it.imageUrl,
                    externalGoogleId = it.externalGoogleId
                )
            }
    )
}

/**
 * Maps a [BookEnrichment] to a [BookEnrichmentEntity] under [cacheKey].
 * Sets lastFetchedAt to current time.
 */
@OptIn(ExperimentalTime::class)
fun BookEnrichment.toEntity(cacheKey: String): BookEnrichmentEntity {
    return BookEnrichmentEntity(
        cacheKey = cacheKey,
        volumeInfoJson = volumeInfo?.let {
            json.encodeToString(
                StoredVolumeInfo(
                    it.subtitle, it.publisher, it.description, it.categories,
                    it.language, it.isbn13, it.isbn10
                )
            )
        },
        authorJson = author?.let {
            json.encodeToString(StoredAuthor(it.name, it.description, it.imageUrl, it.bio))
        },
        authorBooksJson = json.encodeToString(
            ListSerializer(StoredBook.serializer()),
            authorBooks.map {
                StoredBook(
                    it.id, it.title, it.author, it.edition, it.year,
                    it.isbn, it.pageCount, it.imageUrl, it.externalGoogleId
                )
            }
        ),
        lastFetchedAt = Clock.System.now().toEpochMilliseconds()
    )
}
