package algo.cartesian

import java.util.*

data class Treap(
        val priority: Int,
        val left: Treap? = null,
        val right: Treap? = null
) {
    val size: Int

    init {
        size = 1 + left.len + right.len // TODO: replace with size of left tree
    }
}

val Treap?.len: Int
    get() = if (this == null) 0 else size

fun merge(l: Treap?, r: Treap?): Treap? {
    if (l == null) return r
    if (r == null) return l

    if (l.priority > r.priority)
        return l.copy(right = merge(l.right, r))
    else
        return r.copy(left = merge(l, r.left))
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
    fun insertTree(t: Treap?, key: Int, newNode: Treap): Treap {
        if (t == null)
            return newNode
        else if (newNode.priority < t.priority) {
            val (l, r) = split(t, key)
            return newNode.copy(left = l, right = r)
        } else if (key <= t.left.len)
            return t.copy(left = insertTree(t.left, key, newNode))
        else // key > t.left.len
            return t.copy(right = insertTree(t.right, key - t.left.len, newNode))
    }

    val newPrior = defaultRandomGen.nextInt()
    return insertTree(t, pos, Treap(newPrior))
}

fun delete(t: Treap?, pos: Int): Treap? {
    if (t == null)
        return null
    else if (t.left.len == pos) {
        return merge(t.left, t.right)
    } else if (pos < t.left.len)
        return t.copy(left = delete(t.left, pos))
    else // pos > t.left.len
        return t.copy(right = delete(t.left, pos - t.left.len))
}