package com.navi.searchservice.model

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "searchmodel")
@CompoundIndex(name = "issuerKeyword", def = "{'issuer' : 1, 'searchKeyword' : 1}")
class SearchModel(
    @Id
    var id: ObjectId = ObjectId(),
    var issuer: String, // Compound Indexed
    var searchKeyword: String, // Compound Indexed

    var searchResult: List<FileObject>
)