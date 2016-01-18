package algo.retro

import algo.cartesian.*
import algo.retro.Operation.Add
import algo.retro.Operation.Extract
import java.lang.Math.max
import java.util.*

class Node(val operation: Operation) {
    lateinit var tree: Treap
}

class PartialRetroPriorityQueue : RetroPriorityQueue {
    private var weightTree: Treap
    private val queueNow = sortedSetOf<Int>()

    // TODO: handle similar keys (add id to operations)
    // TODO: replace with ad-hoc implementation based on HashSet and PriorityQueue
    private val operationNodes = TreeMap<Int, MutableList<Node>>()

    init {
        // there is always a bridge in -inf and +inf times
        val negInfNode = Node(Add(Int.MAX_VALUE - 1)) // TODO: remove dirty hack
        weightTree = insert(null, 0, nd = negInfNode, weight = 0)
        operationNodes.put(Int.MIN_VALUE, arrayListOf(negInfNode))

        val posInfNode = Node(Add(Int.MAX_VALUE - 1)) // TODO: remove dirty hack
        weightTree = insert(weightTree, 1, nd = posInfNode, weight = 0)
    }

    override val isEmpty: Boolean
        get() = queueNow.isEmpty()

    override val min: Int
        get() = queueNow.first() // TODO: improve from O(log n) to O(1)

    override val operations: SortedMap<Int, List<Operation>>
        get() = operationNodes.mapValues { it.value.map(Node::operation) }.toSortedMap()

    override fun insertAddOperation(time: Int, key: Int) = insertOperation(time, Add(key))
    override fun insertExtractOperation(time: Int)       = insertOperation(time, Extract)

    private fun insertOperation(time: Int, operation: Operation) {
        val prevOperationList = if (time in operationNodes)
            operationNodes[time]
        else
            operationNodes.lowerEntry(time)?.value

        val prevIndex = prevOperationList?.last()?.tree?.index() ?: 0
        val currentList = operationNodes.getOrPut(time) { arrayListOf() } // TODO: safe one call to operations[time]

        val weight = when (operation) {
            is Add -> {
                val beforeBridge = weightTree.prevBridge(prevIndex) // TODO: optimize to get index, not Treap
                val bridgeIndex = beforeBridge?.index()
                val maxNode = if (bridgeIndex === null)
                    null
                else
                    weightTree.suffixMax(bridgeIndex)

                val keyToInsert = max(operation.key, (maxNode?.operation as? Add)?.key ?: Int.MIN_VALUE)
                queueNow += keyToInsert

                if (keyToInsert == operation.key)
                    0
                else {
                    maxNode?.weight = 0
                    1
                }
            }
            Extract -> {
                val afterBridge = weightTree.nextBridge(prevIndex + 1)
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

        val newPos = prevIndex + 1
        val newNode = Node(operation)
        weightTree = insert(weightTree, newPos, newNode, weight)
        currentList += newNode
    }

    override fun deleteAddOperation(time: Int) = deleteOperation(time) { it.operation is Add }
    override fun deleteExtractOperation(time: Int) = deleteOperation(time) { it.operation !is Add }

    private inline fun deleteOperation(time: Int, opFind: (Node) -> Boolean) {
        val timeOps = operationNodes[time] ?: return
        val pos = timeOps.indexOfFirst(opFind)
        val opNode = timeOps[pos]
        val opTree = opNode.tree
        val opIndex = opTree.index()
        val operation = opTree.operation

        when (operation) {
            is Add ->
                if (operation.key in queueNow)
                    queueNow -= operation.key
                else {
                    val afterBridge = weightTree.nextBridge(opIndex + 1)
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
                val beforeBridge = weightTree.prevBridge(opIndex - 1) // TODO: optimize to get index, not Treap
                val bridgeIndex = beforeBridge?.index()
                val maxNode = if (bridgeIndex === null)
                    null
                else
                    weightTree.suffixMax(bridgeIndex)

                val keyToInsert = (maxNode?.operation as Add).key // TODO: exception?
                queueNow += keyToInsert
                maxNode?.weight = 0
            }
        }

        weightTree = delete(weightTree, opIndex)!! // TODO: improve to faster delete by reference
        timeOps.removeAt(pos)
        if (timeOps.isEmpty()) operationNodes.remove(time)
    }
}