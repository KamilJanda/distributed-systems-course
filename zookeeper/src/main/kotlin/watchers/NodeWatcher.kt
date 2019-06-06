package watchers

import DescendantWatcher
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.Watcher.Event.EventType.NodeCreated
import org.apache.zookeeper.Watcher.Event.EventType.NodeDeleted
import org.apache.zookeeper.ZooKeeper
import java.io.IOException


@Suppress("NON_EXHAUSTIVE_WHEN")
class NodeWatcher(
    private var zooKeeper: ZooKeeper,
    private var znodeToWatch: String,
    private val exec: String
) : Watcher {

    init {
        zooKeeper.register(this)
    }

    var app: Process? = null
    private val childrenWatcher = DescendantWatcher(zooKeeper, znodeToWatch)


    override fun process(event: WatchedEvent) {
        when (event.type) {
            NodeCreated -> {
                childrenWatcher.setupChildrenWatch()
                runApp()
            }
            NodeDeleted -> stopApp()
        }
        zooKeeper.exists(znodeToWatch, this)
    }

    private fun runApp() {
        if (app == null) {
            try {
                app = Runtime.getRuntime().exec(exec)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            println("App started!")
        } else if (app!!.isAlive) {
            println("app is working")
        }
    }

    private fun stopApp() {
        if (app != null && app!!.isAlive) {
            app!!.destroy()
            println("Stopped custom app.")
            app = null
        } else {
            println("No app to stop.")
        }
    }

    @Throws(KeeperException::class, InterruptedException::class)
    private fun countChildren(path: String): Int {
        var childrenCount = 0
        val children = zooKeeper.getChildren(path, false)
        for (child in children) {
            val childrenPath = String.format("%s/%s", path, child)
            childrenCount += countChildren(childrenPath)
        }
        childrenCount += children.size
        return childrenCount
    }
}