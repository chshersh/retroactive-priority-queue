package retro

interface RetroPriorityQueue {
    val min: Int

    fun insertAddOperation(time: Int, key: Int)
    fun insertExtractOperation(time: Int)

    fun deleteAddOperation(time: Int)
    fun deleteExtractOperation(time: Int)
}