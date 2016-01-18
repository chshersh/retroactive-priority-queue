package algo.cartesian

import algo.retro.Node
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
        val node: Node
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

    var weight = w
        set(value) {
            field = value

            var cur: Treap? = this
            while (cur != null) {
                cur.update()
                cur = cur.parent
            }
        }

    val operation = node.operation

    var size = 0
    var minSum = w
    var maxSum = w
    var weightSum = w

    val inQNow: Boolean
        get() = weight == 0
    val thisIfInQ: Treap?
        get() = if (inQNow) this else null
    val nullIfInQ: Treap?
        get() = if (inQNow) null else this

    // references to nodes with minimal/maximal values
    var minInQ: Treap?    = thisIfInQ
    var maxNotInQ: Treap? = nullIfInQ

    init {
        node.tree = this
        left?.parent = this
        right?.parent = this
        update()
    }

    fun update() {
        size = 1 + left.len + right.len

        minSum = min(left.smin, left.sum + weight + min(0, right.smin))
        maxSum = max(left.smax, left.sum + weight + max(0, right.smax))
        weightSum = weight + left.sum + right.sum

        minInQ    = minByQ(left?.minInQ,    thisIfInQ, right?.minInQ)
        maxNotInQ = maxByQ(left?.maxNotInQ, nullIfInQ, right?.maxNotInQ)
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
        val fromRange = fromPos .. Int.MAX_VALUE

        fun query(l: Int, r: Int, currentPrefixSum: Int, t: Treap?): Treap? {
            if (t === null || fromRange.isEmptyIntersection(l, r) || !t.hasBridge(currentPrefixSum))
                return null

            val curIndex = l + t.left.len
            val leftQuery = query(l, curIndex - 1, currentPrefixSum, t.left)
            if (leftQuery !== null) return leftQuery

            val newPrefixSum = currentPrefixSum + t.left.sum + t.weight
            if (newPrefixSum == 0 && curIndex >= fromRange.start) return t

            return query(curIndex + 1, r, newPrefixSum, t.right)
        }

        return query(0, size - 1, 0, this)
    }

    fun prevBridge(toPos: Int): Treap? {
        val toRange = Int.MIN_VALUE .. toPos

        fun query(l: Int, r: Int, currentPrefixSum: Int, t: Treap?): Treap? {
            if (t === null || toRange.isEmptyIntersection(l, r) || !t.hasBridge(currentPrefixSum))
                return null

            val curIndex = l + t.left.len
            val newPrefixSum = currentPrefixSum + t.left.sum + t.weight
            val rightQuery = query(curIndex + 1, r, newPrefixSum, t.right)
            if (rightQuery !== null) return rightQuery

            if (newPrefixSum == 0 && curIndex <= toRange.endInclusive) return t

            return query(l, curIndex - 1, currentPrefixSum, t.left)
        }

        return query(0, size - 1, 0, this)
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

fun minByQ(vararg t: Treap?) = t.minBy(Treap?::qmin)
fun maxByQ(vararg t: Treap?) = t.maxBy(Treap?::qmax)

fun IntRange.isEmptyIntersection(from: Int, to: Int)
        = (max(start, from) .. min(endInclusive, to)).isEmpty()

fun Treap?.prefixMin(toPos: Int): Treap? {
    val toRange = Int.MIN_VALUE .. toPos

    fun queryMin(l: Int, r: Int, t: Treap?): Treap? {
        if (t === null || toRange.isEmptyIntersection(l, r))
            return null

        val curIndex = l + t.left.len
        if (curIndex <= toRange.endInclusive)
            return t.minInQ

        return minByQ(
                queryMin(l, curIndex - 1, t.left),
                t.thisIfInQ,
                queryMin(curIndex + 1, r, t.right)
        )
    }

    return queryMin(0, len - 1, this)
}

fun Treap?.suffixMax(fromPos: Int): Treap? {
    val fromRange = fromPos .. Int.MAX_VALUE

    fun queryMax(l: Int, r: Int, t: Treap?): Treap? {
        if (t === null || fromRange.isEmptyIntersection(l, r))
            return null

        val curIndex = l + t.left.len
        if (fromRange.start <= curIndex)
            return t.maxNotInQ

        return maxByQ(
                queryMax(l, curIndex - 1, t.left),
                t.nullIfInQ,
                queryMax(curIndex + 1, r, t.right)
        )
    }

    return queryMax(0, len - 1, this)
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
        return Pair(Treap(t.priority, t.left, l, w = t.weight, node = t.node), r)
    } else {
        val (l, r) = split(t.left, index)
        return Pair(l, Treap(t.priority, r, t.right, w = t.weight, node = t.node))
    }
}

val defaultRandomGen = Random(42)
val maxNode = Node(Add(Int.MAX_VALUE))

fun insert(t: Treap?, pos: Int, nd: Node = maxNode, weight: Int = 0): Treap {
    val newPrior = defaultRandomGen.nextInt()

    fun insertTree(t: Treap?, key: Int): Treap {
        if (t === null)
            return Treap(newPrior, w = weight, node = nd)
        else if (newPrior > t.priority) {
            val (l, r) = split(t, key)
            return Treap(newPrior, l, r, w = weight, node = nd)
        } else if (key <= t.left.len) {
            t.left = insertTree(t.left, key)
            return t
        } else { // key > t.left.len
            t.right = insertTree(t.right, key - t.left.len - 1)
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