import java.util.HashMap

class DistributedMap(cluster: String) : SimpleStringMap {
    val hashMap = HashMap<String, Int>()
    private val channel: Channel = Channel()

    init {
        channel.init(cluster, this)
    }

    override fun containsKey(key: String): Boolean {
        return hashMap.containsKey(key)
    }

    override fun get(key: String): Int? {
        return hashMap[key]
    }

    override fun put(key: String, value: Int) {
        try {
            channel.send("put $key $value")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        hashMap[key] = value
    }

    override fun remove(key: String): Int? {
        try {
            channel.send("remove $key")
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return hashMap.remove(key)
    }

    fun setHashMap(newMap: Map<String, Int>) {
        hashMap.clear()
        hashMap.putAll(newMap)
    }

    companion object {

        fun parseCommand(message: String): Triple<String?, String?, Int?> {
            var command: String? = null
            var key: String? = null
            var value: Int? = null
            val commandLine = message.split("\\s+".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (commandLine.size >= 2) {
                command = commandLine[0]
                key = commandLine[1]
            }
            if (commandLine.size >= 3)
                value = Integer.parseInt(commandLine[2])
            return Triple(command, key, value)
        }
    }
}
