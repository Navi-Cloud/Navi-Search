package com.navi.searchservice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "customwordlist")
class UserCustomWordList(
    @Id
    @Indexed
    var id: ObjectId = ObjectId(),

    @Indexed
    var issuer: String,
    var wordList: List<String>
)