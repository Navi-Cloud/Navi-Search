package com.navi.searchservice.repository

import com.navi.searchservice.model.UserCustomWordList
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.core.updateFirst
import org.springframework.stereotype.Repository

@Repository
class UserCustomWordRepository(
    private val mongoTemplate: MongoTemplate
) {
    fun getWordListByUserEmail(userEmail: String): List<String>? {
        val query = Query(Criteria.where("issuer").`is`(userEmail))
        return mongoTemplate.findOne<UserCustomWordList>(query)?.wordList
    }

    fun addWordList(userEmail: String, keyword: String) {
        val query = Query(Criteria.where("issuer").`is`(userEmail))
        val update = Update().push(UserCustomWordList::wordList.name, keyword)
        mongoTemplate.updateFirst<UserCustomWordList>(query, update)
    }
}