package algo.retro

import algo.retro.Operation.Add
import algo.retro.Operation.Extract
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.*

const val RND_SEED = 42L
const val MAX_RANGE = 100000
const val MAX_KEY = 1234567890

class RetroPriorityQueueAdvancedTests {
    @Test
    fun testSerialInserts() {
        val q = PartialRetroPriorityQueue()

        for (i in 1..MAX_RANGE)
            q.insertAddOperation(i, i)

        assertEquals(1, q.min)
    }

    @Test
    fun testSerialInsertsReverse() {
        val q = PartialRetroPriorityQueue()

        for (i in 1..MAX_RANGE)
            q.insertAddOperation(i, MAX_RANGE - i)

        assertEquals(0, q.min)
    }

    @Test
    fun testSerialInsertsSerialExtracts() {
        val q = PartialRetroPriorityQueue()

        for (i in 1..MAX_RANGE)
            q.insertAddOperation(i, i)

        for (i in 1..MAX_RANGE)
            q.insertExtractOperation(MAX_RANGE + i)

        assertTrue(q.isEmpty)
    }

    @Test
    fun testSerialInsertsSerialExtractsWithoutLast() {
        val q = PartialRetroPriorityQueue()

        for (i in 1..MAX_RANGE)
            q.insertAddOperation(i, i)

        for (i in 1..MAX_RANGE - 1)
            q.insertExtractOperation(MAX_RANGE + i)

        assertEquals(MAX_RANGE, q.min)
    }

    @Test
    fun testInterleaveInsertsAndExtracts() {
        val q = PartialRetroPriorityQueue()

        for (i in 1..MAX_RANGE) {
            q.insertAddOperation(i, i)
            q.insertExtractOperation(i)
        }

        assertTrue(q.isEmpty)
    }

    @Test
    fun testRandomTimeInserts() {
        val q = PartialRetroPriorityQueue()
        val rnd = Random(RND_SEED)
        val times = (1..MAX_RANGE).map { rnd.nextInt(MAX_KEY) }

        for (i in times.indices)
            q.insertAddOperation(times[i], i)

        assertEquals(0, q.min)
    }

    @Test
    fun testRandomTimeInsertsInterleaveExtracts() {
        val q = PartialRetroPriorityQueue()
        val rnd = Random(RND_SEED)
        val times = (1..MAX_RANGE).map { rnd.nextInt(MAX_KEY) }

        for (i in times.indices)
            q.insertAddOperation(times[i], i)

        for (i in times.indices)
            q.insertExtractOperation(times[i] + 1)

        assertTrue(q.isEmpty)
    }

    @Test
    fun testRandomInsertsAndExtractsCompareWithQueue() {
        val q = PartialRetroPriorityQueue()
        val pq = PriorityQueue<Int>()
        val rnd = Random(RND_SEED)

        val rndKeys = (1..MAX_RANGE).map { rnd.nextInt(MAX_KEY) }.distinct()

        for (i in rndKeys.indices) {
            val newOperation = if (pq.isEmpty() || rnd.nextInt(10) > 3)
                Add(rndKeys[i])
            else
                Extract

            when (newOperation) {
                is Add -> {
                    pq.add(newOperation.key)
                    q.insertAddOperation(i, newOperation.key)
                }
                Extract -> {
                    q.insertExtractOperation(i)
                    pq.poll()
                }
            }

            if (pq.isEmpty())
                assertTrue(q.isEmpty)
            else
                assertEquals(pq.peek(), q.min)
        }
    }
}