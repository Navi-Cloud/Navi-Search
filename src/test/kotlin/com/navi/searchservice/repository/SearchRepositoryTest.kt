package com.navi.searchservice.repository

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import com.navi.searchservice.model.FileObject
import com.navi.searchservice.model.SearchModel
import org.bson.types.ObjectId
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import java.io.ByteArrayInputStream
import java.io.InputStream

@SpringBootTest
class SearchRepositoryTest {
    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var gridFsTemplate: GridFsTemplate

    @Autowired
    private lateinit var searchRepository: SearchRepository

    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    // Test Data
    private val testFileObject: FileObject = FileObject(
        userEmail = "kangdroid",
        fileName = "testFileName",
        currFolderName = "root",
        isFile = true,
        isFavorites = false,
        isTrash = false
    )

    private fun convertFileObjectToMetaData(fileObject: FileObject): BasicDBObject {
        val jsonString = objectMapper.writeValueAsString(fileObject)

        return BasicDBObject.parse(jsonString)
    }


    private fun saveToGridFS(fileObject: FileObject, inputStream: InputStream) {
        val dbMetaData: DBObject = convertFileObjectToMetaData(fileObject)

        val id: ObjectId = gridFsTemplate.store(
            inputStream, fileObject.fileName, dbMetaData
        )
    }

    @BeforeEach
    fun setupDatabase() {
        mongoTemplate.remove<SearchModel>(Query())
        gridFsTemplate.delete(Query())
    }

    @AfterEach
    fun clearDatabase() {
        mongoTemplate.remove<SearchModel>(Query())
        gridFsTemplate.delete(Query())
    }

    @DisplayName("searchRepository: searchRepository should return indexed result.")
    @Test
    fun is_searchRepository_returns_index_result() {
        // Let
        val mockSearchResult = SearchModel(
            issuer = "kangdroid",
            searchKeyword = "test",
            searchResult = listOf(testFileObject)
        )

        mongoTemplate.insert(mockSearchResult)

        // Do
        searchRepository.searchRepository(mockSearchResult.issuer, mockSearchResult.searchKeyword).also {
            assertNotNull(it)
            assertEquals(mockSearchResult.issuer, it.issuer)
            assertEquals(mockSearchResult.searchKeyword, it.searchKeyword)
            assertEquals(1, mockSearchResult.searchResult.size)
            assertEquals(testFileObject.fileName, mockSearchResult.searchResult[0].fileName)
        }
    }

    @DisplayName("searchRepository: searchRepository should return storage's result")
    @Test
    fun is_searchRepository_returns_storage_result() {
        // Let
        saveToGridFS(testFileObject, ByteArrayInputStream("".toByteArray()))

        // Do
        searchRepository.searchRepository(testFileObject.userEmail, "test").also {
            assertNotNull(it)
            assertEquals(testFileObject.userEmail, it.issuer)
        }
    }

    @DisplayName("searchRepository: searchRepository should return empty object if not found.")
    @Test
    fun is_searchRepository_returns_empty_result() {
        // Do
        searchRepository.searchRepository(testFileObject.userEmail, "test").also {
            assertNotNull(it)
            assertEquals(0, it.searchResult.size)
        }
    }
}