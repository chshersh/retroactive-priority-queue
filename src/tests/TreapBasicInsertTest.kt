package tests

import algo.cartesian.delete
import algo.cartesian.insert
import org.junit.Assert.assertEquals
import org.junit.Test

class TreapBasicInsertTest {
    @Test
    fun testInsertOneSize() {
        val t = insert(null, 0)
        assertEquals(1, t.size)
    }

    @Test
    fun testInsertDifferentSize() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = insert(t1, 2)

        assertEquals(3, t2.size)
    }

    @Test
    fun testInsertAllOnZero() {
        val t0 = insert(insert(null, 0), 0)

        assertEquals(2, t0.size)
    }

    @Test
    fun testOnInsertOnRemove() {
        val t0 = insert(null, 0)
        val t1 = delete(t0, 0)
        assertEquals(null, t1)
    }

    @Test
    fun testMultipleInsertAndRemoves() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = delete(t1, 0)
        val t3 = insert(t2, 1)

        assertEquals(2, t3.size)
    }
}