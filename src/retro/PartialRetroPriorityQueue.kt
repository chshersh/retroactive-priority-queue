package retro

import java.util.*

data class Segment(val key: Int, val startLife: Int, val endLife: Int)

class PartialRetroPriorityQueue(
        private val operations: SortedMap<Int, List<Operation>> = sortedMapOf()
) : RetroPriorityQueue {

    constructor(otherQueue: PartialRetroPriorityQueue) : this(otherQueue.operations)

    override val min: Int
        get() {
            val queue = PriorityQueue<Int>()
            operations.values.forEach { ops -> ops.forEach { it.process(queue) } }
            return queue.peek()
        }

    override fun insertAddOperation(time: Int, key: Int) = insertOperation(time, Operation.Add(key))
    override fun insertExtractOperation(time: Int)       = insertOperation(time, Operation.Extract)

    private fun insertOperation(time: Int, operation: Operation) {
        val currentList = operations.getOrPut(time) { arrayListOf() }
        operations.put(time, currentList + operation)
    }

    override fun deleteOperation(time: Int) {
        val timeOps = operations[time] ?: return
        operations.put(time, timeOps.subList(0, timeOps.lastIndex))
        if (timeOps.isEmpty()) operations.remove(time)
    }

    fun createSegments(): List<Segment> {
        val deadSegments = arrayListOf<Segment>()
        val queue = PriorityQueue<Segment>({ s1, s2 -> s1.key.compareTo(s2.key) })

        for ((time, ops) in operations) {
            for (operation in ops) {
                when (operation) {
                    is Operation.Add -> queue.add(Segment(operation.key, time, Int.MAX_VALUE))
                    Operation.Extract -> {
                        val deadMin = queue.poll()
                        deadSegments.add(deadMin.copy(endLife = time))
                    }
                }
            }
        }

        return deadSegments + queue.toList()
    }
}