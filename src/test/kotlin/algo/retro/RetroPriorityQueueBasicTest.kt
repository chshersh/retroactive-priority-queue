package algo.retro

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RetroPriorityQueueBasicTest {
    @Test
    fun testInsert() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 0)

        assertEquals(0, q.min)
    }

    @Test
    fun testInsertTwoKeys() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertAddOperation(1, 10)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertTwoKeysReverseValue() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 10)
        q.insertAddOperation(1, 5)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertTwoKeysReverseTime() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(1, 5)
        q.insertAddOperation(0, 10)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertDelete() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)

        assertTrue(q.isEmpty)
    }

//    TODO: must pass?
//    @Test
//    fun testInsertDeleteReverse() {
//        val q = PartialRetroPriorityQueue()
//        q.insertExtractOperation(1)
//        q.insertAddOperation(0, 5)
//        assertTrue(q.isEmpty)
//    }

    @Test
    fun testInsertInsertExtract() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertAddOperation(1, 10)
        q.insertExtractOperation(2)

        assertEquals(10, q.min)
    }

    @Test
    fun testInsertInsertReverseDelete() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(1, 10)
        q.insertAddOperation(0, 5)
        q.insertExtractOperation(2)

        assertEquals(10, q.min)
    }

    @Test
    fun testInsertExtractInsert() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)
        q.insertAddOperation(2, 10)

        assertEquals(10, q.min)
    }

    @Test
    fun testInsertInsertExtractExtract() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertAddOperation(1, 10)
        q.insertExtractOperation(2)
        q.insertExtractOperation(3)

        assertTrue(q.isEmpty)
    }

    @Test
    fun testInsertExtractInsertExtract() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)
        q.insertAddOperation(2, 10)
        q.insertExtractOperation(3)

        assertTrue(q.isEmpty)
    }

    @Test
    fun testInsertExtractInsertExtractInsert() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)
        q.insertAddOperation(2, 10)
        q.insertExtractOperation(3)
        q.insertAddOperation(4, 20)

        assertEquals(20, q.min)
    }

    /** tests with operations deletion */
    @Test
    fun testInsertRemoveOperation() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.deleteAddOperation(0)

        assertTrue(q.isEmpty)
    }

    @Test
    fun testInsertInsertRemoveFirst() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertAddOperation(1, 10)
        q.deleteAddOperation(0)

        assertEquals(10, q.min)
    }

    @Test
    fun testInsertInsertRemoveSecond() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertAddOperation(1, 10)
        q.deleteAddOperation(1)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertExtractRemoveExtract() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)
        q.deleteExtractOperation(1)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertInsertExtractRemoveExtract() {
        val q = PartialRetroPriorityQueue()
        q.insertAddOperation(0, 5)
        q.insertAddOperation(1, 10)
        q.insertExtractOperation(2)
        q.deleteExtractOperation(2)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertInsertReverseExtractRemoveExtract() {
        val q = PartialRetroPriorityQueue()

        q.insertAddOperation(1, 5)
        q.insertAddOperation(0, 10)
        q.insertExtractOperation(2)

        q.deleteExtractOperation(2)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertExtractInsertRemoveInsert() {
        val q = PartialRetroPriorityQueue()

        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)
        q.insertAddOperation(2, 10)

        q.deleteAddOperation(2)

        assertTrue(q.isEmpty)
    }

    @Test
    fun testInsertExtractInsertRemoveExtract() {
        val q = PartialRetroPriorityQueue()

        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)
        q.insertAddOperation(2, 10)

        q.deleteExtractOperation(1)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertExtractInsertRemoveInsertExtract() {
        val q = PartialRetroPriorityQueue()

        q.insertAddOperation(0, 5)
        q.insertExtractOperation(1)
        q.insertAddOperation(2, 10)

        q.deleteExtractOperation(1)
        q.deleteAddOperation(2)

        assertEquals(5, q.min)
    }

    @Test
    fun testInsertInsertRemoveInsertInsert() {
        val q = PartialRetroPriorityQueue()

        q.insertAddOperation(0, 5)
        q.insertAddOperation(1, 10)

        q.deleteAddOperation(0)
        q.deleteAddOperation(1)

        assertTrue(q.isEmpty)
    }
}