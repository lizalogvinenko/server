package io.github.lizalogvinenko.yavshoke.test

import io.github.lizalogvinenko.yavshoke.test.client.YavshokeClient
import io.github.lizalogvinenko.yavshoke.test.client.dto.register.RegisterRequest
import io.github.lizalogvinenko.yavshoke.test.client.dto.register.RegisterResponse
import io.github.lizalogvinenko.yavshoke.test.util.StubUser
import io.ktor.client.call.body
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions
import java.util.Base64
import java.util.UUID
import kotlin.test.Test

class RegisterTest {
    val client = YavshokeClient.create()

    @Test
    fun `REGISTER user`() = runTest {
        val email = "${randomString()}@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "12345678",
                age = StubUser.AGE
            )
        )

        Assertions.assertEquals(HttpStatusCode.OK, response.status)

        val body = response.body<RegisterResponse>()

        Assertions.assertEquals(
            RegisterResponse.User(
                id = body.user.id,
                email = email,
                name = body.user.name,
                age = StubUser.AGE
            ),
            body.user
        )

        Assertions.assertTrue(body.token.isNotBlank()) {
            "Token should not be empty or blank"
        }
    }


    @Test
    fun `REGISTER user already exists`() = runTest {
        val response = client.register(
            RegisterRequest(
                email = "liza@mail.ru",
                password = "12345678",
                age = StubUser.AGE
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user without email local part`() = runTest {
        val email = "@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "12345678",
                age = StubUser.AGE
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user without email domain part`() = runTest {
        val email = "${randomString()}@.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "12345678",
                age = StubUser.AGE
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user invalid email domain part`() = runTest {
        val email = "${randomString()}@mail"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "12345678",
                age = StubUser.AGE
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user too short password`() = runTest {
        val email = "${randomString()}@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "1234",
                age = StubUser.AGE
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user too long password`() = runTest {
        val email = "${randomString()}@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "password21characters!",
                age = StubUser.AGE
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user 0 years old`() = runTest {
        val email = "${randomString()}@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "123456",
                age = 0
            )
        )

        Assertions.assertEquals(HttpStatusCode.OK, response.status)
    }

    @Test
    fun `REGISTER negative user age`() = runTest {
        val email = "${randomString()}@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "123456",
                age = -1
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user 100 years`() = runTest {
        val email = "${randomString()}@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "123456",
                age = 100
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    @Test
    fun `REGISTER user very old`() = runTest {
        val email = "${randomString()}@yavshoke.ru"
        val response = client.register(
            RegisterRequest(
                email = email,
                password = "123456",
                age = 101
            )
        )

        Assertions.assertEquals(HttpStatusCode.UnprocessableEntity, response.status)
    }

    private fun randomString(): String {
        val uuid = UUID.randomUUID().toString()
        return Base64.getEncoder().encode(uuid.toByteArray())
            .toString(Charsets.UTF_8)
            .substring(0, 20)
    }
}