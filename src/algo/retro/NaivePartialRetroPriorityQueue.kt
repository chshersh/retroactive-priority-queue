package algo.retro

import algo.retro.Operation.Add
import algo.retro.Operation.Extract
import java.util.*

class NaivePartialRetroPriorityQueue(
        override val operations: SortedMap<Int, List<Operation>> = sortedMapOf()
) : RetroPriorityQueue {

    constructor(otherQueueNaive: NaivePartialRetroPriorityQueue) : this(otherQueueNaive.operations)

    override val isEmpty: Boolean
        get() = throw UnsupportedOperationException()

    override val min: Int
        get() {
            val queue = PriorityQueue<Int>()

            operations.values.forEach {
                ops -> ops.forEach {
                    when (it) {
                        is Add -> queue.add(it.key)
                        Extract -> queue.poll()
                    }
                }
            }

            return queue.peek()
        }

    override fun insertAddOperation(time: Int, key: Int) = insertOperation(time, Add(key))
    override fun insertExtractOperation(time: Int)       = insertOperation(time, Extract)

    private fun insertOperation(time: Int, operation: Operation) {
        val currentList = operations.getOrPut(time) { arrayListOf() }
        operations.put(time, currentList + operation)
    }

    override fun deleteAddOperation(time: Int) = deleteOperation(time) { it is Add }
    override fun deleteExtractOperation(time: Int) = deleteOperation(time) { it !is Add }

    inline private fun deleteOperation(time: Int, opFind: (Operation) -> Boolean) {
        val timeOps = operations[time] ?: return
        val pos = timeOps.indexOfFirst(opFind)

        operations.put(time, timeOps.subList(0, pos) + timeOps.subList(pos + 1, timeOps.size))

        if (timeOps.isEmpty()) operations.remove(time)
    }
}