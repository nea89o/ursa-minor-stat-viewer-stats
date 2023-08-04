package moe.nea.ursa.statviewer

import java.time.Instant

object GraphTest {
    @JvmStatic
    fun main(Arg: Array<String>) {
        GraphCreator.displayGraph(
            GraphCreator.createGraphFromPoints(
                "a",
                mapOf("c" to GraphCreator.queryGraphPoints("hypixel:accumulated:player", Instant.ofEpochMilli(0L))),
                "b"
            )
        )
    }
}