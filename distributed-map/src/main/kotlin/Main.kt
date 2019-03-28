import java.io.BufferedReader
import java.io.InputStreamReader


fun main() {
    System.setProperty("java.net.preferIPv4Stack", "true")

    val distributedMap = DistributedMap("ApplicationCluster")
    val input = InputStreamReader(System.`in`)
    val buffer = BufferedReader(input)
    lateinit var message: String

    while (true) {
        message = buffer.readLine()
        println(run(distributedMap, message))
    }
}

private fun run(distributedMap: DistributedMap, message: String): Any? {
    val (command, key, value) = DistributedMap.parseCommand(message)
    when (command.toString()) {
        "put" ->
            if (value != null && key != null)
                return distributedMap.put(key, value)
        "get" ->
            if (key != null)
                return distributedMap[key]
        "remove" ->
            if (key != null)
                return distributedMap.remove(key)
        "containsKey" ->
            if (key != null)
                return distributedMap.containsKey(key)
    }
    return "Command does not exists!"
}

