import org.apache.zookeeper.AsyncCallback.StatCallback
import org.apache.zookeeper.KeeperException
import org.apache.zookeeper.KeeperException.Code
import org.apache.zookeeper.KeeperException.Code.*
import org.apache.zookeeper.WatchedEvent
import org.apache.zookeeper.Watcher
import org.apache.zookeeper.ZooKeeper
import org.apache.zookeeper.data.Stat
import java.util.*


class DataMonitor(
    private var zk: ZooKeeper,
    private var znode: String,
    private var chainedWatcher: Watcher?,
    private var listener: DataMonitorListener
) : Watcher, StatCallback {

    internal var dead: Boolean = false
    private var prevData: ByteArray? = null

    init {
        // Get things started by checking if the node exists. We are going
        // to be completely event driven
        zk.exists(znode, true, this, null)
    }

    override fun process(event: WatchedEvent) {
        val path = event.path
        if (event.type == Watcher.Event.EventType.None) {
            // We are are being told that the state of the
            // connection has changed
            when (event.state) {
                Watcher.Event.KeeperState.SyncConnected -> {
                }
                Watcher.Event.KeeperState.Expired -> {
                    // It's all over
                    dead = true
                    listener.closing(Code.SessionExpired)
                }
            }// In this particular example we don't need to do anything
            // here - watches are automatically re-registered with
            // server and any watches triggered while the client was
            // disconnected will be delivered (in order of course)
        } else {
            if (path != null && path == znode) {
                // Something has changed on the node, let's find out
                zk.exists(znode, true, this, null)
            }
        }
        if (chainedWatcher != null) {
            chainedWatcher!!.process(event)
        }
    }

    override fun processResult(rc: Int, path: String, ctx: Any, stat: Stat) {
        val exists: Boolean
        when (get(rc)) {
            OK -> exists = true
            NONODE -> exists = false
            SESSIONEXPIRED, NOAUTH -> {
                dead = true
                listener.closing(rc)
                return
            }
            else -> {
                zk.exists(znode, true, this, null)
                return
            }
        }

        var b: ByteArray? = null
        if (exists) {
            try {
                b = zk.getData(znode, false, null)
            } catch (e: KeeperException) {
                e.printStackTrace()
            } catch (e: InterruptedException) {
                return
            }

        }

        if (b == null && null != prevData || b != null && !Arrays.equals(prevData, b)) {
            listener.exists(b)
            prevData = b
        }
    }
}

/**
 * Other classes use the DataMonitor by implementing this method
 */
interface DataMonitorListener {
    /**
     * The existence status of the node has changed.
     */
    fun exists(data: ByteArray?)

    /**
     * The ZooKeeper session is no longer valid.
     *
     * @param rc
     * the ZooKeeper reason code
     */
    fun closing(rc: Int)
}