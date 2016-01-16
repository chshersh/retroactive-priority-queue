package algo.cartesian

import java.util.*

class Treap(
        val priority: Int,
        leftSon: Treap? = null,
        rightSon: Treap? = null
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

    var size = 0
    var weight = 0

    init { update() }

    fun update() {
        size = 1 + left.len + right.len // TODO: replace with size of left tree?
    }
}

val Treap?.len: Int
    get() = if (this == null) 0 else size

fun merge(l: Treap?, r: Treap?): Treap? {
    if (l == null) return r
    if (r == null) return l

    if (l.priority > r.priority) {
        l.right = merge(l.right, r)
        return l
    } else {
        r.left = merge(l, r.left)
        return r
    }
}

fun split(t: Treap?, index: Int): Pair<Treap?, Treap?> { // res.first.size = index - 1
    if (t == null) return Pair(null, null)

    val curIndex = 1 + t.left.len

    if (curIndex <= index) {
        val (l, r) = split(t.right, index - curIndex)
        return Pair(Treap(t.priority, t.left, l), r)
    } else {
        val (l, r) = split(t.left, index)
        return Pair(l, Treap(t.priority, r, t.right))
    }
}

val defaultRandomGen = Random()

fun insert(t: Treap?, pos: Int): Treap {
    fun insertTree(t: Treap?, key: Int, newPrior: Int): Treap {
        if (t == null)
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
    if (t == null)
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