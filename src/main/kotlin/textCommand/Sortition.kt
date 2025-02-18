package top.wyh2004.group.manager.plugin.textCommand

import kotlinx.coroutines.*
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.Member
import net.mamoe.mirai.event.EventHandler
import net.mamoe.mirai.event.SimpleListenerHost
import net.mamoe.mirai.event.events.GroupMessageEvent
import net.mamoe.mirai.message.data.At
import net.mamoe.mirai.message.data.PlainText
import net.mamoe.mirai.message.data.buildMessageChain
import net.mamoe.mirai.utils.ExternalResource
import net.mamoe.mirai.utils.ExternalResource.Companion.toExternalResource
import top.wyh2004.group.manager.plugin.PluginMain
import java.util.*

/**
 * @author WYH2004
 * @date 2022/7/21
 **/
class Sortition : SimpleListenerHost() {

    @EventHandler
    suspend fun GroupMessageEvent.onEvent() {
        if (message.toString().contains("#抽签")) {
            val times = resetMap[sender.id]
            if (times != null && !qqSortitionList.contains(sender.id)) {
                if (times > 3) {
                    group.sendMessage(At(sender.id) + PlainText("\n") + "你今天已经抽过签了!")
                    return
                }
                val imgList = listOf(
                    "/img/sortition/---.jpg",
                    "/img/sortition/----.jpg",
                    "/img/sortition/-----.jpg"
                )
                val ex = PluginMain::class.java.getResourceAsStream(imgList[times - 1])!!.toExternalResource()
                sendImageMsg(group,sender,ex)
                return
            }
            if (!qqSortitionList.contains(sender.id)) {
                val imgList = listOf(
                    "/img/sortition/--.jpg",
                    "/img/sortition/-.jpg",
                    "/img/sortition/-+.jpg",
                    "/img/sortition/+.jpg",
                    "/img/sortition/++.jpg"
                )
                val ex = PluginMain::class.java.getResourceAsStream(imgList.random())!!.toExternalResource()
                sendImageMsg(group,sender,ex)
                return
            } else {
                group.sendMessage(At(sender.id) + PlainText("\n") + "你今天已经抽过签了!")
                return
            }
        }
    }

    private suspend fun sendImageMsg(group: Group, sender : Member, ex: ExternalResource) {
        val img = group.uploadImage(ex)
        withContext(Dispatchers.IO) {
            ex.close()
        }
        val msg = buildMessageChain {
            +At(sender.id)
            +PlainText("\n")
            +img
        }
        group.sendMessage(msg)
        qqSortitionList.add(sender.id)
        return
    }

    companion object {
        val resetMap = mutableMapOf<Long, Int>()
        var qqSortitionList: MutableList<Long> = mutableListOf()

        fun timer(interval: Long) {
            PluginMain.launch(Dispatchers.IO) {
                while (isActive) {
                    try {
                        val c = Calendar.getInstance()
                        val hour = c.get(Calendar.HOUR_OF_DAY)
                        val minute = c.get(Calendar.MINUTE)
                        if (hour == 0 && minute == 0) {
                            qqSortitionList.clear()
                            resetMap.clear()
                        }
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e
                        e.printStackTrace()
                    }
                    delay(interval)
                }
            }
        }
    }
}