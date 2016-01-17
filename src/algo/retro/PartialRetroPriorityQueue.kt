package algo.retro

import algo.cartesian.*
import algo.retro.Operation.Add
import algo.retro.Operation.Extract
import java.lang.Math.max
import java.util.*

class PartialRetroPriorityQueue : RetroPriorityQueue {
    private var weightTree: Treap? = null
    private val queueNow = sortedSetOf<Int>()

    // TODO: handle similar keys (add id to operations)
    // TODO: replace with ad-hoc implementation based on HashSet and PriorityQueue
    private val operations = TreeMap<Int, MutableList<Treap>>()

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
        val currentList = operations.getOrPut(time) { arrayListOf() } // TODO: safe one call to operations[time]

        val weight = when (operation) {
            is Add -> {
                val beforeBridge = weightTree?.prevBridge(prevIndex) // TODO: optimize to get index, not Treap
                val bridgeIndex = beforeBridge?.index()
                val maxNode = if (bridgeIndex === null)
                    null
                else
                    weightTree.suffixMax(bridgeIndex)

                val keyToInsert = max(operation.key, (maxNode?.operation as? Add)?.key ?: Int.MIN_VALUE)
                queueNow += keyToInsert

                if (keyToInsert == operation.key)
                    1
                else {
                    maxNode?.weight = 0
                    0
                }
            }
            Extract -> {
                val afterBridge = weightTree?.nextBridge(prevIndex)
                val bridgeIndex = afterBridge?.index()
                val minNode = if (bridgeIndex === null)
                    null
                else
                    weightTree.prefixMin(bridgeIndex)

                val keyToRemove = (minNode?.operation as Add).key // TODO: exception if no keys to delete
                queueNow -= keyToRemove
                minNode?.weight = 1

                -1
            }
        }

        weightTree = insert(weightTree, prevIndex, operation, weight)
        currentList += weightTree!![prevIndex] // TODO: optimize to get inserted node
    }

    override fun deleteAddOperation(time: Int) = deleteOperation(time) { it.operation is Add }
    override fun deleteExtractOperation(time: Int) = deleteOperation(time) { it.operation !is Add }

    private fun deleteOperation(time: Int, opFind: (Treap) -> Boolean) {
        val timeOps = operations[time] ?: return
        val pos = timeOps.indexOfFirst(opFind)
        val opTree = timeOps[pos]
        val opIndex = opTree.index()
        val operation = opTree.operation

        when (operation) {
            is Add -> {
                val afterBridge = weightTree?.nextBridge(opIndex)
                val bridgeIndex = afterBridge?.index()
                val minNode = if (bridgeIndex === null)
                    null
                else
                    weightTree.prefixMin(bridgeIndex)

                val keyToRemove = (minNode?.operation as Add).key // TODO: exception if no keys to delete
                queueNow -= keyToRemove
                minNode?.weight = 1
            }
            Extract -> {
                val beforeBridge = weightTree?.prevBridge(opIndex) // TODO: optimize to get index, not Treap
                val bridgeIndex = beforeBridge?.index()
                val maxNode = if (bridgeIndex === null)
                    null
                else
                    weightTree.suffixMax(bridgeIndex)

                val keyToRemove = (maxNode?.operation as Add).key // TODO: exception?
                queueNow += keyToRemove
                maxNode?.weight = 0
            }
        }

        weightTree = delete(weightTree, opIndex) // TODO: improve to faster delete by reference
        timeOps.removeAt(pos)
        if (timeOps.isEmpty()) operations.remove(time)
    }
}