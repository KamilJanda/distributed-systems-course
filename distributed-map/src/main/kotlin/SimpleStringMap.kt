interface SimpleStringMap {
    fun containsKey(key: String): Boolean

    operator fun get(key: String): Int?

    fun put(key: String, value: Int)

    fun remove(key: String): Int?
}