import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.Watcher.Event.EventType.NodeChildrenChanged
import org.apache.zookeeper.ZooKeeper

class DescendantWatcher(
    private val zk: ZooKeeper,
    private val watchedNode: String
) : Watcher {

    override fun process(event: WatchedEvent) {
        if (event.type == NodeChildrenChanged) {
            setupChildrenWatch()
            val childrenCount = countChildren(watchedNode)
            System.out.printf("The %s node has currently: %d descendants.%n", watchedNode, childrenCount)
        }
    }

    fun setupChildrenWatch() {
        setupChildrenWatch(watchedNode)
    }

    private fun setupChildrenWatch(path: String) {
        try {
            for (child in zk.getChildren(path, this)) {
                setupChildrenWatch(String.format("%s/%s", path, child))
            }
        } catch (e: KeeperException) {
        } catch (e: InterruptedException) {
        }

    }

    private fun countChildren(path: String): Int {
        var childrenCount = 0
        val children = zk.getChildren(path, false)
        for (child in children) {
            val childrenPath = String.format("%s/%s", path, child)
            childrenCount += countChildren(childrenPath)
        }
        childrenCount += children.size
        return childrenCount
    }
}