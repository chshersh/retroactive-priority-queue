package algo.retro

sealed class Operation {
    class  Add(val key: Int) : Operation()
    object Extract           : Operation()
}