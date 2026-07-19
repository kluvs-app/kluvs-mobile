package com.ivangarzab.kluvs.data.local.source

import com.ivangarzab.kluvs.data.local.mappers.toDomain
import com.ivangarzab.kluvs.data.local.mappers.toEntity
import com.ivangarzab.kluvs.database.KluvsDatabase
import com.ivangarzab.kluvs.model.Session
import com.ivangarzab.bark.Bark

/**
 * Local data source for Session entities.
 * Handles CRUD operations with the local Room database.
 */
interface SessionLocalDataSource {
    suspend fun getSession(sessionId: String): Session?
    suspend fun getSessionsForClub(clubId: String): List<Session>
    suspend fun insertSession(session: Session)
    suspend fun insertSessions(sessions: List<Session>)
    suspend fun deleteSession(sessionId: String)
    suspend fun getLastFetchedAt(sessionId: String): Long?
    suspend fun deleteAll()
}

/**
 * Implementation of [SessionLocalDataSource] using Room database.
 */
class SessionLocalDataSourceImpl(
    private val database: KluvsDatabase
) : SessionLocalDataSource {

    private val sessionDao = database.sessionDao()
    private val bookDao = database.bookDao()

    override suspend fun getSession(sessionId: String): Session? {
        val sessionEntity = sessionDao.getSession(sessionId) ?: return null
        val bookId = sessionEntity.bookId ?: return null
        val bookEntity = bookDao.getBook(bookId) ?: return null
        val members = sessionDao.getSessionMembers(sessionId).map { it.toDomain() }
        return sessionEntity.toDomain(bookEntity.toDomain()).copy(members = members)
    }

    override suspend fun getSessionsForClub(clubId: String): List<Session> {
        return sessionDao.getSessionsForClub(clubId).mapNotNull { sessionEntity ->
            val bookId = sessionEntity.bookId ?: return@mapNotNull null
            val bookEntity = bookDao.getBook(bookId) ?: return@mapNotNull null
            sessionEntity.toDomain(bookEntity.toDomain())
        }
    }

    override suspend fun insertSession(session: Session) {
        Bark.v("Inserting session (ID: ${session.id}) into database")
        try {
            // First insert the book
            bookDao.insertBook(session.book.toEntity())
            // Then insert the session
            sessionDao.insertSession(session.toEntity())
            // Replace the participation list to drop members no longer on the session
            sessionDao.deleteSessionMembers(session.id)
            if (session.members.isNotEmpty()) {
                sessionDao.insertSessionMembers(session.members.map { it.toEntity(session.id) })
            }
            Bark.d("Successfully inserted session (ID: ${session.id}) into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert session (ID: ${session.id}) into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun insertSessions(sessions: List<Session>) {
        Bark.v("Inserting ${sessions.size} sessions into database")
        try {
            // Insert all books first
            val books = sessions.map { it.book }
            bookDao.insertBooks(books.map { it.toEntity() })

            // Then insert all sessions
            sessionDao.insertSessions(sessions.map { it.toEntity() })
            Bark.d("Successfully inserted ${sessions.size} sessions into database")
        } catch (e: Exception) {
            Bark.e("Failed to insert ${sessions.size} sessions into database. Retry on next sync.", e)
            throw e
        }
    }

    override suspend fun deleteSession(sessionId: String) {
        val entity = sessionDao.getSession(sessionId)
        if (entity != null) {
            Bark.d("Deleting session (ID: $sessionId) from database")
            try {
                sessionDao.deleteSessionMembers(sessionId)
                sessionDao.deleteSession(entity)
                Bark.d("Successfully deleted session (ID: $sessionId) from database")
            } catch (e: Exception) {
                Bark.e("Failed to delete session (ID: $sessionId) from database. Retry on next sync.", e)
                throw e
            }
        }
    }

    override suspend fun getLastFetchedAt(sessionId: String): Long? {
        return sessionDao.getLastFetchedAt(sessionId)
    }

    override suspend fun deleteAll() {
        Bark.d("Clearing all sessions from database")
        try {
            sessionDao.deleteAllSessionMembers()
            sessionDao.deleteAll()
            Bark.d("Successfully cleared all sessions from database")
        } catch (e: Exception) {
            Bark.e("Failed to clear all sessions from database. Retry on next sync.", e)
            throw e
        }
    }
}
