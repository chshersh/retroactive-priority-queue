package algo.retro

interface RetroPriorityQueue {
    val isEmpty: Boolean
    val min: Int

    // TODO: return id of added operation
    fun insertAddOperation(time: Int, key: Int)
    fun insertExtractOperation(time: Int)

    // TODO: replace by one function -- removal by id
    fun deleteAddOperation(time: Int)
    fun deleteExtractOperation(time: Int)
}