import org.apache.zookeeper.ZooKeeper

object Printer {

    fun printTree(zooKeeper: ZooKeeper, znode: String) {
        if (zooKeeper.exists(znode, false) == null) {
            System.out.printf("Node %s does not exist.%n", znode)
        } else {
            val children = zooKeeper.getChildren(znode, false)
            println(znode)
            for (child in children) {
                printTree(zooKeeper, String.format("%s/%s", znode, child))
            }
        }
    }
}