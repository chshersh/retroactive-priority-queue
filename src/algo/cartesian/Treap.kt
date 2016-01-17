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
        w: Int,
        val operation: Operation
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

    val thisIfInQ: Treap?
        get() = if (inQNow) this else null
    val nullIfInQ: Treap?
        get() = if (inQNow) null else this

    // references to nodes with minimal/maximal values
    var minInQ: Treap?    = thisIfInQ // only on left  subtree and current node
    var maxNotInQ: Treap? = nullIfInQ // only on right subtree and current node

    init { update() }

    fun update() {
        size = 1 + left.len + right.len

        minSum = min(left.smin, left.sum + weight + min(0, right.smin))
        maxSum = max(left.smax, left.sum + weight + max(0, right.smax))
        weightSum = weight + left.sum + right.sum

        minInQ    = minByQ(left?.minInQ,     thisIfInQ)
        maxNotInQ = maxByQ(right?.maxNotInQ, nullIfInQ)
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
            if (pos > t.size - 1)
                return null

            val leftQuery = query(pos, currentPrefixSum, t.left)
            if (leftQuery !== null) return leftQuery

            val newPrefixSum = currentPrefixSum + t.left.sum + t.weight
            // minimal sum is achieved on current position
            if (newPrefixSum == 0) return t

            return if (t.right.hasBridge(currentPrefixSum))
                query(pos - t.left.len - 1, newPrefixSum, t.right)
            else
                null
        }

        return query(fromPos, 0, this)
    }

    fun prevBridge(toPos: Int): Treap? {
        fun query(pos: Int, currentPrefixSum: Int, t: Treap?): Treap? {
            if (t === null)
                return null
            if (pos < 0)
                return null

            val newPrefixSum = currentPrefixSum + t.left.sum + t.weight
            val rightQuery = query(pos - t.left.len - 1, newPrefixSum, t.right)
            if (rightQuery !== null) return rightQuery

            // minimal sum is achieved on current position
            if (newPrefixSum == 0) return t

            return if (t.left.hasBridge(currentPrefixSum))
                query(pos, currentPrefixSum, t.left)
            else
                null
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

private fun Treap?.hasBridge(prefixSum: Int)
        = 0 in prefixSum + smin .. prefixSum + smax

val Treap?.sum: Int
    get() = this?.weightSum ?: 0

val Treap?.qmin: Int  // minimum if in queueNow
    get() = if (this !== null &&  inQNow) (operation as Add).key else Int.MAX_VALUE

val Treap?.qmax: Int // maximum if not in queueNow
    get() = if (this !== null && !inQNow && operation is Add) operation.key else Int.MIN_VALUE

fun minByQ(t1: Treap?, t2: Treap?) = arrayOf(t1, t2).minBy(Treap?::qmin)
fun maxByQ(t1: Treap?, t2: Treap?) = arrayOf(t1, t2).maxBy(Treap?::qmax)

fun Treap?.prefixMin(toPos: Int): Treap? {
    if (this === null)
        return null
    else if (toPos == left.len)
        return minInQ
    else if (toPos < left.len)
        return left.prefixMin(toPos)
    else // toPos > left.len
        return minByQ(right.prefixMin(toPos - left.len), this)
}

fun Treap?.suffixMax(fromPos: Int): Treap? {
    if (this === null)
        return null
    else if (fromPos == left.len)
        return maxNotInQ
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
        return Pair(Treap(t.priority, t.left, l, w = t.weight, operation = t.operation), r)
    } else {
        val (l, r) = split(t.left, index)
        return Pair(l, Treap(t.priority, r, t.right, w = t.weight, operation = t.operation))
    }
}

val defaultRandomGen = Random(42)
val maxAdd = Add(Int.MAX_VALUE)

fun insert(t: Treap?, pos: Int, op: Operation = maxAdd, weight: Int = 0): Treap {
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