package org.skull.king.domain.core.query.sync

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.skull.king.application.infrastructure.IdGenerator
import org.skull.king.core.domain.state.FoldSettled
import org.skull.king.core.usecases.captor.*

class ProjectOnFoldSettledTest {

    private val repository = ReadSkullKingRepository()

    @Test
    fun `should project butinAllies correctly`() {
        val skullKingId = IdGenerator().skullKingId()

        setupFold(skullKingId)

        val captor = ProjectOnFoldSettled(repository)

        captor.execute(
            FoldSettled(
                skullKingId,
                "1",
                20,
                true,
                listOf("2"),
                0
            )
        )

        val game = repository[skullKingId]
        Assertions.assertThat(game?.scoreBoard).isEqualTo(
            mapOf(
                "1" to listOf(RoundScore(announced = 1, done = 1, roundNb = 1, potentialBonus = 20)),
                "2" to listOf(RoundScore(announced = 0, done = 0, roundNb = 1, potentialBonus = 20))
            )
        )
    }

    private fun setupFold(gameId: String) {
        ReadSkullKing(
            gameId,
            listOf(
                ReadPlayer("1", gameId, listOf()),
                ReadPlayer("2", gameId, listOf())
            ),
            1,
            listOf(),
            false,
            SkullKingPhase.CARDS,
            "1",
            mapOf(
                "1" to listOf(RoundScore(announced = 1, roundNb = 1)),
                "2" to listOf(RoundScore(announced = 0, roundNb = 1))
            )
        ).let(repository::save)
    }
}