import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.ZooKeeper
import watchers.NodeWatcher
import java.io.IOException


class Executor @Throws(KeeperException::class, IOException::class)
constructor(
    private val hostPort: String,
    private val znodeToWatch: String,
    private val exec: String
) {

    init {
        Runtime.getRuntime().addShutdownHook(Thread {
            close()
        })
    }

    val zooKeeper: ZooKeeper = ZooKeeper(hostPort, 3000, null)
    private val nodeWatcher = NodeWatcher(zooKeeper, znodeToWatch, exec)


    fun run() {
        try {
            zooKeeper.exists(znodeToWatch, nodeWatcher)
        } catch (e: KeeperException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    fun close() {
        try {
            zooKeeper.close()
        } catch (e: InterruptedException) {
            println("Error while closing ZooKeeper instance!")
        }

    }


}