import org.jgroups.JChannel
import org.jgroups.Message
import org.jgroups.protocols.*
import org.jgroups.protocols.pbcast.*
import org.jgroups.stack.ProtocolStack

import java.net.InetAddress

class Channel: JChannel(false) {

    init {
        val stack = ProtocolStack()
        this.protocolStack = stack
        stack.addProtocol(UDP().setValue("mcast_group_addr", InetAddress.getByName("230.100.213.7")))
            .addProtocol(PING())
            .addProtocol(MERGE3())
            .addProtocol(FD_SOCK())
            .addProtocol(
                FD_ALL()
                    .setValue("timeout", 12000)
                    .setValue("interval", 3000)
            )
            .addProtocol(VERIFY_SUSPECT())
            .addProtocol(BARRIER())
            .addProtocol(NAKACK2())
            .addProtocol(UNICAST3())
            .addProtocol(STABLE())
            .addProtocol(GMS())
            .addProtocol(UFC())
            .addProtocol(MFC())
            .addProtocol(FRAG2())
            .addProtocol(STATE())
            .addProtocol(SEQUENCER())
            .addProtocol(FLUSH())

        stack.init()
    }

    fun init(cluster: String, map: DistributedMap) {
        this.setReceiver(Receiver(map, this))
        this.connect(cluster)
        this.getState(null, 0)
    }

    fun send(s: String) {
        val msg = Message(null, null, s)
        this.send(msg)
    }

}
