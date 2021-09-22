package com.navi.searchservice.repository

import com.navi.searchservice.model.SearchModel
import com.navi.searchservice.model.UserCustomWordList
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.findOne
import org.springframework.data.mongodb.core.insert
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.remove
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest
@ExtendWith(SpringExtension::class)
class UserCustomWordRepositoryTest {

    @Autowired
    private lateinit var mongoTemplate: MongoTemplate

    @Autowired
    private lateinit var userCustomWordRepository: UserCustomWordRepository

    @BeforeEach
    fun setupDatabase() {
        mongoTemplate.remove<UserCustomWordList>(Query())
    }

    @AfterEach
    fun clearDatabase() {
        mongoTemplate.remove<UserCustomWordList>(Query())
    }

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

    @DisplayName("addWordList: addWordlist should add word list on document.")
    @Test
    fun is_addWordList_works_well() {
        mongoTemplate.insert<UserCustomWordList>(
            UserCustomWordList(
                issuer = "kangdroid",
                wordList = emptyList()
            )
        )

        // do
        userCustomWordRepository.addWordList("kangdroid", "testKeyword")

        // Check
        val query = Query(Criteria.where("issuer").`is`("kangdroid"))
        mongoTemplate.findOne<UserCustomWordList>(query).also {
            assertNotNull(it)
            assertEquals(1, it!!.wordList.size)
            assertEquals("testKeyword", it.wordList[0])
        }
    }
}