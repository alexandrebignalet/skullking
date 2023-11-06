package org.skull.king.integration

import io.mockk.every
import io.quarkiverse.test.junit.mockk.InjectMock
import io.quarkus.test.junit.QuarkusTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.application.infrastructure.authentication.AUTH_COOKIE_NAME
import org.skull.king.application.infrastructure.authentication.User
import org.skull.king.helpers.ApiHelper

@QuarkusTest
class RegisterUserTest {

    @InjectMock
    lateinit var idGenerator: IdGenerator

    @Test
    fun `should attach cookie to client on register success`() {
        val userId = IdGenerator().userId()
        every { idGenerator.userId() } returns userId

        val response = ApiHelper().authentication.register("francis")

        assertThat(response.statusCode).isEqualTo(302)
        assertThat(response.headers["Location"].value).isEqualTo(
            "http://localhost:8081/skullking/game_rooms"
        )
        assertThat(response.headers["Set-Cookie"].value).isEqualTo("skullking-auth=\"$userId:francis\";Version=1")
    }

    @Test
    fun `should be able to hit an authenticated path`() {
        val userId = IdGenerator().userId()
        val gameRoomId = IdGenerator().gameRoomId()
        every { idGenerator.userId() } returns userId
        every { idGenerator.gameRoomId() } returns gameRoomId
        val api = ApiHelper()

        api.authentication.register("francis").getCookie(AUTH_COOKIE_NAME)
        val response = api.gameRoom.create(User(userId, "francis"))

        assertThat(response.statusCode).isEqualTo(200)
    }
}