package com.navi.searchservice.service

import com.navi.searchservice.model.FileObject
import com.navi.searchservice.model.SearchModel
import com.navi.searchservice.repository.SearchRepository
import org.springframework.stereotype.Service

@Service
class IndexerService(
    private val searchRepository: SearchRepository
) {
    fun fileNameIndex(fileObject: FileObject, issuer: String) {
        if (!searchRepository.keywordExists(issuer, fileObject.fileName)) {
            // Since keyword with fileName does not exist at all.
            searchRepository.addNewSearchableIndex(
                SearchModel(
                    issuer = issuer,
                    searchKeyword = fileObject.fileName,
                    searchResult = listOf(fileObject)
                )
            )
            return
        }
        // Get keyword
        val searchResult = searchRepository.searchRepository(issuer, fileObject.fileName)

        // if target fileObject exists somehow, do nothing or else, index it.
        searchResult.searchResult.find {
            findSearchResult(it, fileObject)
        } ?: run {
            searchRepository.updateSearchIndex(issuer, fileObject.fileName, fileObject)
        }
    }

    private fun findSearchResult(input: FileObject, target: FileObject): Boolean {
        return input.fileName == target.fileName && input.currFolderName == target.currFolderName
    }
}