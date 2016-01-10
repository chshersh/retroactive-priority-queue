package retro

import geom.Segment
import java.util.*

data class KeyWithTime(val key: Int, val time: Int)

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

    fun createSegments(lowestPoint: Int, maxLifeTime: Int): List<Segment> {
        val deadSegments = arrayListOf<Segment>()
        val queue = PriorityQueue<KeyWithTime>({ kt1, kt2 -> kt1.key.compareTo(kt2.key) })

        for ((time, ops) in operations) {
            for (operation in ops) {
                when (operation) {
                    is Operation.Add -> queue.add(KeyWithTime(operation.key, time))
                    Operation.Extract -> {
                        val deadMin = queue.poll()

                        deadSegments.add(Segment(deadMin.time, deadMin.key, time, deadMin.key))
                        deadSegments.add(Segment(time, lowestPoint, time, deadMin.key))
                    }
                }
            }
        }

        return deadSegments + queue.toList().map { Segment(it.time, it.key, maxLifeTime, it.key) }
    }
}