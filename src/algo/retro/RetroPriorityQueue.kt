package algo.retro

import java.util.*

interface RetroPriorityQueue {
    val isEmpty: Boolean
    val min: Int
    val operations: SortedMap<Int, List<Operation>>

    // TODO: return id of added operation
    fun insertAddOperation(time: Int, key: Int)
    fun insertExtractOperation(time: Int)

    // TODO: replace by one function -- removal by id
    fun deleteAddOperation(time: Int)
    fun deleteExtractOperation(time: Int)
}