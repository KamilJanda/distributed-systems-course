import org.jgroups.*
import org.jgroups.util.Util

import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

class Receiver(private val map: DistributedMap, private val channel: JChannel) : ReceiverAdapter() {

    override fun receive(message: Message?) {
        val msg: String = (message?.getObject() as? String) ?: "Null msg"
        println(message?.src.toString() + " >> " + msg)

        val (command, key, value) = DistributedMap.parseCommand(msg)
        when (command.toString()) {
            "put" -> map.hashMap[key.toString()] = value as Int
            "remove" -> map.hashMap.remove(key)
        }

    }

    override fun viewAccepted(newView: View?) {
        println("** view: " + newView!!)
    }

    override fun getState(outputStream: OutputStream?) {
        synchronized(map) {
            Util.objectToStream(map.hashMap, DataOutputStream(outputStream))
        }
    }

    override fun setState(inputStream: InputStream?) {
        val newMap: Map<String, Int> = Util.objectFromStream(DataInputStream(inputStream!!)) as Map<String, Int>
        synchronized(map) {
            map.setHashMap(newMap)
        }
    }
}
