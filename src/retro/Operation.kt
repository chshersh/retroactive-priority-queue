package retro

import java.util.*

sealed class Operation {
    class  Add(val key: Int) : Operation()
    object Extract           : Operation()

    fun process(queue: PriorityQueue<Int>): Any = when (this) {
        is Add  -> queue.add(key)
        Extract -> queue.poll()
    }
}