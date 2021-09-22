package com.navi.searchservice.repository

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.navi.searchservice.model.FileObject
import com.navi.searchservice.model.SearchModel
import org.bson.Document
import org.bson.json.JsonMode
import org.bson.json.JsonWriterSettings
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.updateFirst
import org.springframework.data.mongodb.gridfs.GridFsTemplate
import org.springframework.stereotype.Repository

@Repository
class SearchRepository(
    private val mongoTemplate: MongoTemplate,
    private val gridFsTemplate: GridFsTemplate
) {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun searchRepository(issuer: String, searchKeyword: String): SearchModel {
        // Search for indexed result
        val query = Query().addCriteria(
            Criteria().andOperator(
                Criteria.where(SearchModel::searchKeyword.name).`is`(searchKeyword),
                Criteria.where(SearchModel::issuer.name).`is`(issuer)
            )
        )
        return mongoTemplate.findOne(query) ?: searchOverStorage(issuer, searchKeyword)
    }

    fun keywordExists(issuer: String, searchKeyword: String): Boolean {
        val query = Query().addCriteria(
            Criteria().andOperator(
                Criteria.where(SearchModel::searchKeyword.name).`is`(searchKeyword),
                Criteria.where(SearchModel::issuer.name).`is`(issuer)
            )
        )

        return mongoTemplate.findOne<SearchModel>(query) != null
    }

    fun addNewSearchableIndex(searchModel: SearchModel) {
        mongoTemplate.insert<SearchModel>(searchModel)
    }

    fun updateSearchIndex(issuer: String, searchKeyword: String, fileObject: FileObject) {
        val query = Query().addCriteria(
            Criteria().andOperator(
                Criteria.where(SearchModel::searchKeyword.name).`is`(searchKeyword),
                Criteria.where(SearchModel::issuer.name).`is`(issuer)
            )
        )
        val update = Update().push(SearchModel::searchResult.name, fileObject)

        mongoTemplate.updateFirst<SearchModel>(query, update)
    }

    private fun searchOverStorage(issuer: String, searchKeyword: String): SearchModel {
        // Search Query
        val fullSearchQuery: Query = Query().apply {
            addCriteria(
                Criteria().andOperator(
                    Criteria.where("metadata.${FileObject::userEmail.name}").`is`(issuer),
                    Criteria.where("metadata.${FileObject::fileName.name}").regex(searchKeyword)
                )
            )
        }
        // Search through storage, and if null, return just empty searchModel
        val rawResult = gridFsTemplate.find(fullSearchQuery)
        rawResult.first()
            ?: return SearchModel(issuer = issuer, searchKeyword = searchKeyword, searchResult = emptyList())

        // Convert to FileObject and Return it
        return SearchModel(
            issuer = issuer,
            searchKeyword = searchKeyword,
            searchResult = rawResult.map {
                convertMetaDataToFileObject(it.metadata!!)
            }.toList()
        )
    }

    private fun convertMetaDataToFileObject(metadata: Document): FileObject {
        return objectMapper.readValue(
            metadata.toJson(
                JsonWriterSettings
                    .builder()
                    .outputMode(JsonMode.RELAXED)
                    .build()
            )
        )
    }
}