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
        var weight: Int = 0,
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

    var minSum = weight
    var maxSum = weight
    var weightSum = weight

    var minNotInQNow = weight  // only on left  subtree and current node
    var maxInQNow = weight     // only on right subtree and current node

    init { update() }

    fun update() {
        size = 1 + left.len + right.len // TODO: replace with size of left tree?

        minSum = min(left.smin, left.sum + right.smin)
        maxSum = max(left.smax, left.sum + right.smax)
        weightSum = weight + left.sum + right.sum

        minNotInQNow = min(left.qmin,  this.qmin)
        maxInQNow    = max(right.qmax, this.qmax)
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

    fun nextBridge(pos: Int): Treap? {
        return null
    }

    fun prevBridge(pos: Int): Treap? {
        return null
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

fun Treap?.prefixMin(toPos: Int): Int {
    if (this === null)
        return Int.MAX_VALUE
    else if (toPos == left.len)
        return minNotInQNow
    else if (toPos < left.len)
        return left.prefixMin(toPos)
    else // toPos > left.len
        return min(right.prefixMin(toPos - left.len), minNotInQNow)
}

fun Treap?.suffixMax(fromPos: Int): Int {
    if (this === null)
        return Int.MIN_VALUE
    else if (fromPos == left.len)
        return maxInQNow
    else if (fromPos < left.len)
        return min(left.suffixMax(fromPos), maxInQNow)
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

fun insert(t: Treap?, pos: Int): Treap {
    fun insertTree(t: Treap?, key: Int, newPrior: Int): Treap {
        if (t === null)
            return Treap(newPrior)
        else if (newPrior > t.priority) {
            val (l, r) = split(t, key)
            return Treap(newPrior, l, r)
        } else if (key <= t.left.len) {
            t.left = insertTree(t.left, key, newPrior)
            return t
        } else { // key > t.left.len
            t.right = insertTree(t.right, key - t.left.len, newPrior)
            return t
        }
    }

    val newPrior = defaultRandomGen.nextInt()
    return insertTree(t, pos, newPrior)
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