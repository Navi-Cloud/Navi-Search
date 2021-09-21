package com.navi.searchservice.repository

import com.navi.searchservice.model.UserCustomWordList
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.insert
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UserCustomWordRepositoryTest {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var userCustomWordRepository: UserCustomWordRepository

    @DisplayName("getWordListByUserEmail: getWordListByUserEmail should return list of word list if exists.")
    @Test
    fun is_getWordListByUserEmail_Returns_Well() {
        mongoTemplate.insert<UserCustomWordList>(
            UserCustomWordList(
                issuer = "kangdroid",
                wordList = emptyList()
            )
        )

        userCustomWordRepository.getWordListByUserEmail("kangdroid").also {
            assertNotNull(it)
            assertTrue(it!!.isEmpty())
        }
    }

    @DisplayName("getWordListByUserEmail: getWordListByUserEmail should return null if not exists.")
    @Test
    fun is_getWordListByUserEmail_Returns_Null() {
        userCustomWordRepository.getWordListByUserEmail("not-exsts").also {
            assertNull(it)
        }
    }
}