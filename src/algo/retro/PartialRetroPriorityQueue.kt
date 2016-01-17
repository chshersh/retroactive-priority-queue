package algo.retro

import algo.cartesian.Treap
import algo.retro.Operation.Add
import algo.retro.Operation.Extract
import java.util.*

class PartialRetroPriorityQueue : RetroPriorityQueue {
    private var weightTree: Treap? = null
    private val queueNow = sortedSetOf<Int>()
    private val operations: TreeMap<Int, MutableList<Treap>> = TreeMap()

    override val min: Int
        get() = queueNow.first() // TODO: improve from O(log n) to O(1)

    override fun insertAddOperation(time: Int, key: Int) = insertOperation(time, Add(key))
    override fun insertExtractOperation(time: Int)       = insertOperation(time, Extract)

    private fun insertOperation(time: Int, operation: Operation) {
        val prevOperationList = if (time in operations)
            operations[time]
        else
            operations.lowerEntry(time)?.value

        val prevIndex = prevOperationList?.last()?.index() ?: 0
        val currentList = prevOperationList ?: operations.getOrPut(time) { arrayListOf() }

        when (operation) {
            is Add -> {
                // val newK = max(operation.key, newTreap.nextBridge)
                // queueNow += newK
            }
            Extract -> {
                // val k' = min(Q_bridge')
            }
        }
    }

    override fun deleteAddOperation(time: Int) {
        throw UnsupportedOperationException()
    }

    override fun deleteExtractOperation(time: Int) {
        throw UnsupportedOperationException()
    }
}