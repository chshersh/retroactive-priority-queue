package algo.retro

import algo.cartesian.Treap
import java.util.*

class PartialRetroPriorityQueue : RetroPriorityQueue {
    private var prefixTree: Treap? = null
    private val operations: SortedMap<Int, List<Operation>> = sortedMapOf()

    override val min: Int
        get() = throw UnsupportedOperationException()

    override fun insertAddOperation(time: Int, key: Int) {
        throw UnsupportedOperationException()
    }

    override fun insertExtractOperation(time: Int) {
        throw UnsupportedOperationException()
    }

    override fun deleteAddOperation(time: Int) {
        throw UnsupportedOperationException()
    }

    override fun deleteExtractOperation(time: Int) {
        throw UnsupportedOperationException()
    }
}