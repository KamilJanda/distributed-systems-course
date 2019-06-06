import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

fun main() {

    val hostPort = "localhost:2181,localhost:2182,localhost:2183"
    val znodeToObserve = "/z"
    val exec = System.getProperty("user.dir") + "/test.sh"

    val executor = Executor(hostPort, znodeToObserve, "/Applications/TextEdit.app/Contents/MacOS/TextEdit")

    try {
        executor.run()
    } catch (e: Exception) {
        e.printStackTrace()
    }

    val br = BufferedReader(InputStreamReader(System.`in`))

    while (true) {

        println()

        try {
            when (br.readLine().trim()) {
                "exit" -> {
                    executor.close()
                    return
                }
                "ls" -> Printer.printTree(executor.zooKeeper, znodeToObserve)
            }
        } catch (e: IOException) {
            println("Error reading input line")
        }

    }

}