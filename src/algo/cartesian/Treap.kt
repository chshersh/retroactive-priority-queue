package algo.cartesian

import algo.retro.Operation
import algo.retro.Operation.Add
import java.lang.Math.max
import java.lang.Math.min
import java.util.*

// this cartesian tree node is really-really fat
class Treap(
        val priority: Int,
        leftSon: Treap? = null,
        rightSon: Treap? = null,
        w: Int = 0,
        var operation: Operation? = null
) {
    var parent: Treap? = null

    var left = leftSon
        set(value) {
            field = value
            field?.parent = this
            update()
        }

    var right = rightSon
        set(value) {
            field = value
            field?.parent = this
            update()
        }

    val inQNow: Boolean
        get() = weight == 0

    var size = 0

    var weight = w
        set(value) {
            field = value

            var cur: Treap? = this
            while (cur != null) {
                cur.update()
                cur = cur.parent
            }
        }

    var minSum = w
    var maxSum = w
    var weightSum = w

    // references to nodes with minimal/maximal values
    var minNotInQNow: Treap? = null  // only on left  subtree and current node
    var maxInQNow: Treap?    = null  // only on right subtree and current node

    init { update() }

    fun update() {
        size = 1 + left.len + right.len

        minSum = min(left.smin, left.sum + right.smin)
        maxSum = max(left.smax, left.sum + right.smax)
        weightSum = weight + left.sum + right.sum

        minNotInQNow = minByQ(left,  this)
        maxInQNow    = maxByQ(right, this)
    }

    fun index(): Int {
        var cur = this
        var pos = left.len

        do {
            val par = cur.parent ?: break
            if (cur === par.right) pos += par.left.len + 1
            cur = par
        } while (true)

        return pos
    }

    operator fun get(i: Int): Treap =
            if (i == left.len)
                this
            else if (i <= left.len)
                left!![i]
            else
                right!![i - left.len - 1]

    fun nextBridge(fromPos: Int): Treap? {
        fun query(pos: Int, currentPrefixSum: Int, t: Treap?): Treap? {
            if (t === null)
                return null

            val newPrefixSum = currentPrefixSum + t.left.sum + t.weight

            if (pos >= left.len)
                return query(pos - t.left.len, newPrefixSum, t.right)

            val leftQuery = query(pos, currentPrefixSum, t.left)
            val curNode = if (newPrefixSum == 0) t else null
            val rightQuery = if (currentPrefixSum + t.right.smin <= 0 && 0 <= currentPrefixSum + t.right.smax)
                query(pos - t.left.len, newPrefixSum, t.right)
            else
                null

            return leftQuery ?: curNode ?: rightQuery
        }

        return query(fromPos, 0, this)
    }

    fun prevBridge(toPos: Int): Treap? {
        fun query(pos: Int, currentPrefixSum: Int, t: Treap?): Treap? {
            if (t === null)
                return null
            else if (pos <= left.len)
                return query(pos, currentPrefixSum, t.left)

            val newPrefixSum = currentPrefixSum + t.left.sum + t.weight

            val leftQuery = if (currentPrefixSum + t.left.smin <= 0 && 0 <= currentPrefixSum + t.left.smax)
                query(pos, currentPrefixSum, t.left)
            else
                null
            val curNode = if (newPrefixSum == 0) t else null
            val rightQuery = query(pos - t.left.len, newPrefixSum, t.right)

            return leftQuery ?: curNode ?: rightQuery
        }

        return query(toPos, 0, this)
    }
}

val Treap?.len: Int
    get() = this?.size ?: 0

val Treap?.smin: Int
    get() = this?.minSum ?: Int.MAX_VALUE / 2

val Treap?.smax: Int
    get() = this?.maxSum ?: Int.MIN_VALUE / 2

val Treap?.sum: Int
    get() = this?.weightSum ?: 0

val Treap?.qmin: Int
    get() = if (this === null || inQNow) Int.MAX_VALUE else (operation as Add).key

val Treap?.qmax: Int
    get() = if (this !== null && inQNow) (operation as Add).key else Int.MIN_VALUE

fun minByQ(t1: Treap?, t2: Treap?) = arrayOf(t1, t2).minBy(Treap?::qmin)?.minNotInQNow
fun maxByQ(t1: Treap?, t2: Treap?) = arrayOf(t1, t2).maxBy(Treap?::qmax)?.maxInQNow

fun Treap?.prefixMin(toPos: Int): Treap? {
    if (this === null)
        return null
    else if (toPos == left.len)
        return minNotInQNow
    else if (toPos < left.len)
        return left.prefixMin(toPos)
    else // toPos > left.len
        return minByQ(right.prefixMin(toPos - left.len), this)
}

fun Treap?.suffixMax(fromPos: Int): Treap? {
    if (this === null)
        return null
    else if (fromPos == left.len)
        return maxInQNow
    else if (fromPos < left.len)
        return maxByQ(left.suffixMax(fromPos), this)
    else // fromPos > left.len
        return right.suffixMax(fromPos - left.len)
}

fun merge(l: Treap?, r: Treap?): Treap? {
    if (l === null) return r
    if (r === null) return l

    if (l.priority > r.priority) {
        l.right = merge(l.right, r)
        return l
    } else {
        r.left = merge(l, r.left)
        return r
    }
}

fun split(t: Treap?, index: Int): Pair<Treap?, Treap?> { // res.first.size = index - 1
    if (t === null) return Pair(null, null)

    val curIndex = 1 + t.left.len

    if (curIndex <= index) {
        val (l, r) = split(t.right, index - curIndex)
        return Pair(Treap(t.priority, t.left, l), r)
    } else {
        val (l, r) = split(t.left, index)
        return Pair(l, Treap(t.priority, r, t.right))
    }
}

val defaultRandomGen = Random(42)

fun insert(t: Treap?, pos: Int, op: Operation? = null, weight: Int = 0): Treap {
    val newPrior = defaultRandomGen.nextInt()

    fun insertTree(t: Treap?, key: Int): Treap {
        if (t === null)
            return Treap(newPrior, w = weight, operation = op)
        else if (newPrior > t.priority) {
            val (l, r) = split(t, key)
            return Treap(newPrior, l, r, w = weight, operation = op)
        } else if (key <= t.left.len) {
            t.left = insertTree(t.left, key)
            return t
        } else { // key > t.left.len
            t.right = insertTree(t.right, key - t.left.len)
            return t
        }
    }

    return insertTree(t, pos)
}

fun delete(t: Treap?, pos: Int): Treap? {
    if (t === null)
        return null
    else if (t.left.len == pos)
        return merge(t.left, t.right)
    else if (pos < t.left.len) {
        t.left = delete(t.left, pos)
        return t
    } else { // pos > t.left.len
        t.right = delete(t.left, pos - t.left.len)
        return t
    }
}