package tests.treap

import algo.cartesian.*
import org.junit.Assert.assertEquals
import org.junit.Test

class TreapBasicSmallTest {
    // test split and merge basic cases
    @Test
    fun testSplitOneByZero() {
        val t = Treap(0)
        val (l, r) = split(t, 0)

        assertEquals(0, l.len)
        assertEquals(1, r.len)
    }

    @Test
    fun testSplitOneByOne() {
        val t = Treap(0)
        val (l, r) = split(t, 1)

        assertEquals(1, l.len)
        assertEquals(0, r.len)
    }

    @Test
    fun testMergeTwo() {
        val t0 = Treap(0)
        val t1 = Treap(1)
        val t = merge(t0, t1)

        assertEquals(2, t.len)
    }

    @Test
    fun testMergeSpitMerge() {
        val t0 = Treap(0)
        val t1 = Treap(1)

        val t = merge(t0, t1)
        val (l, r) = split(t, 1)
        val m = merge(l, r)

        assertEquals(2, m.len)
    }

    // test split and delete with insert
    @Test
    fun testOneInsert() {
        val t = insert(null, 0)

        assertEquals(1, t.size)
    }

    @Test
    fun testTwoInserts() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)

        assertEquals(2, t1.size)
    }

    @Test
    fun testThreeInserts() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = insert(t1, 2)

        assertEquals(3, t2.size)
    }

    @Test
    fun testThreeInsertsSplitByZero() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = insert(t1, 2)
        val (l, r) = split(t2, 0)

        assertEquals(0, l.len)
        assertEquals(3, r.len)
    }

    @Test
    fun testThreeInsertsSplitByOne() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = insert(t1, 2)
        val (l, r) = split(t2, 1)

        assertEquals(1, l.len)
        assertEquals(2, r.len)
    }

    @Test
    fun testThreeInsertsSplitByTwo() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = insert(t1, 2)
        val (l, r) = split(t2, 2)

        assertEquals(2, l.len)
        assertEquals(1, r.len)
    }

    @Test
    fun testThreeInsertsSplitByThree() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = insert(t1, 2)
        val (l, r) = split(t2, 3)

        assertEquals(3, l.len)
        assertEquals(0, r.len)
    }

    @Test
    fun testInsertTwoOnZero() {
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
    fun testInsertDeleteInsert() {
        val t0 = insert(null, 0)
        val t1 = delete(t0, 0)
        val t2 = insert(t1, 0)

        assertEquals(1, t2.len)
    }

    @Test
    fun testInsertInsertDelete() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = delete(t1, 0)

        assertEquals(1, t2.len)
    }

    @Test
    fun testInsertInsertDeleteInsert() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)
        val t2 = delete(t1, 0)
        val t3 = insert(t2, 1)

        assertEquals(2, t3.size)
    }

    // test correct indices
    @Test
    fun testIndexOneElement() {
        val t = insert(null, 0)

        assertEquals(0, t.index())
    }

    @Test
    fun testTwoElementIndices() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 1)

        assertEquals(0, t1[0].index())
        assertEquals(1, t1[1].index())
    }

    @Test
    fun testThreeIndexZero() {
        val t0 = insert(null, 0)
        val t1 = insert(t0, 0)
        val t2 = insert(t1, 0)

        assertEquals(0, t2[0].index())
        assertEquals(1, t2[1].index())
        assertEquals(2, t2[2].index())
    }
}