package com.navi.searchservice.service

import com.navi.searchservice.model.FileObject
import com.navi.searchservice.model.SearchModel
import com.navi.searchservice.repository.SearchRepository
import com.nhaarman.mockitokotlin2.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import java.util.*

@SpringBootTest
class IndexerServiceTest {
    @MockBean
    private lateinit var searchRepository: SearchRepository

    @Autowired
    private lateinit var indexerService: IndexerService

    // Test Data
    private val testFileObject: FileObject = FileObject(
        userEmail = "kangdroid",
        fileName = "testFileName",
        currFolderName = "root",
        isFile = true,
        isFavorites = false,
        isTrash = false
    )

    private val randomFileObject: FileObject get() {
        return FileObject(
            userEmail = UUID.randomUUID().toString(),
            fileName = UUID.randomUUID().toString()
        )
    }

    @DisplayName("fileNameIndex: fileNameIndex should index new search model if db not contains search result.")
    @Test
    fun is_fileNameIndex_creates_new_index() {
        // Setup keywordExists
        whenever(searchRepository.keywordExists(any(), any()))
            .thenReturn(false)

        // Setup addNewSearchableIndex
        doNothing().whenever(searchRepository).addNewSearchableIndex(any())

        // Do
        indexerService.fileNameIndex(testFileObject, "kangdroid")

        // Check
        verify(searchRepository).keywordExists(any(), any())
        verify(searchRepository).addNewSearchableIndex(any())
        verify(searchRepository, never()).searchRepository(any(), any())
        verify(searchRepository, never()).updateSearchIndex(any(), any(), any())
    }

    @DisplayName("fileNameIndex: fileNameIndex should not update any searchIndex if searchResult exists.")
    @Test
    fun is_fileNameIndex_not_update_anything() {
        // Setup keywordExists
        whenever(searchRepository.keywordExists(any(), any())).thenReturn(true)

        // Setup searchRepository
        whenever(searchRepository.searchRepository(any(), any()))
            .thenReturn(SearchModel(
                issuer = "kangdroid",
                searchKeyword = "test",
                searchResult = listOf(testFileObject)
            ))

        // Do
        indexerService.fileNameIndex(testFileObject, "kangdroid")

        // Check
        verify(searchRepository).keywordExists(any(), any())
        verify(searchRepository, never()).addNewSearchableIndex(any())
        verify(searchRepository).searchRepository(any(), any())
        verify(searchRepository, never()).updateSearchIndex(any(), any(), any())
    }

    @DisplayName("fileNameIndex: fileNameIndex should update its index result if they have to.")
    @Test
    fun is_fileNameIndex_updates_index() {
        // Setup keywordExists
        whenever(searchRepository.keywordExists(any(), any())).thenReturn(true)

        // Setup searchRepository
        whenever(searchRepository.searchRepository(any(), any()))
            .thenReturn(SearchModel(
                issuer = "kangdroid",
                searchKeyword = "test",
                searchResult = listOf(randomFileObject, randomFileObject, randomFileObject)
            ))

        // Do
        indexerService.fileNameIndex(testFileObject, "kangdroid")

        // Check
        verify(searchRepository).keywordExists(any(), any())
        verify(searchRepository, never()).addNewSearchableIndex(any())
        verify(searchRepository).searchRepository(any(), any())
        verify(searchRepository).updateSearchIndex(any(), any(), any())
    }
}