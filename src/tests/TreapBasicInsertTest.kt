package tests

import algo.cartesian.insert
import org.junit.Assert.assertEquals
import org.junit.Test

class TreapBasicInsertTest {
    @Test
    fun testInsertOneSize() {
        val t = insert(null, 0)
        assertEquals(1, t.size)
    }
}