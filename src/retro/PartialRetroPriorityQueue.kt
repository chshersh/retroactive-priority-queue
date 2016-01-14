package retro

import geom.Segment
import java.util.*

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

    override fun deleteAddOperation(time: Int) = deleteOperation(time) { it is Operation.Add }
    override fun deleteExtractOperation(time: Int) = deleteOperation(time) { it !is Operation.Add }

    inline private fun deleteOperation(time: Int, opFind: (Operation) -> Boolean) {
        val timeOps = operations[time] ?: return
        val pos = timeOps.indexOfFirst(opFind)

        operations.put(time, timeOps.subList(0, pos) + timeOps.subList(pos + 1, timeOps.size))

        if (timeOps.isEmpty()) operations.remove(time)
    }

    fun createSegments(maxLifeTime: Int): List<Segment> {
        var curId = 0
        val deadSegments = arrayListOf<Segment>()
        val queue = PriorityQueue<Segment>({ s1, s2 -> s1.y1.compareTo(s2.y1) })

        var prevAddSegment: Segment? = null

        for ((time, ops) in operations) {
            for (operation in ops) {
                when (operation) {
                    is Operation.Add -> queue.add(Segment(curId++, time, operation.key, maxLifeTime, operation.key))
                    Operation.Extract ->
                        if (queue.isEmpty()) {
                            val extractRay = Segment(curId++, time, 0, time, Int.MAX_VALUE)

                            prevAddSegment?.nextOnAdd = extractRay
                            prevAddSegment = null

                            deadSegments.add(extractRay)
                        } else {
                            val addSegment = queue.poll()
                            val extractSegment = Segment(curId++, time, 0, time, addSegment.y1)

                            addSegment.x2 = time
                            addSegment.nextOnExtract = extractSegment

                            extractSegment.nextOnAdd = addSegment
                            extractSegment.nextOnExtract = queue.peek()

                            prevAddSegment?.nextOnAdd = extractSegment
                            prevAddSegment = addSegment

                            deadSegments.add(extractSegment)
                            deadSegments.add(addSegment)
                        }
                }
            }
        }

        return deadSegments + queue.toList()
    }
}