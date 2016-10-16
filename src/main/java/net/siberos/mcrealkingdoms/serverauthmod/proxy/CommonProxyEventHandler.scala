package net.siberos.mcrealkingdoms.serverauthmod.proxy

import java.util.logging.{Level, Logger}

import scala.collection.mutable.ListBuffer
import net.minecraft.entity.player.{EntityPlayer, InventoryPlayer}
import net.minecraft.util.math.BlockPos
import net.minecraft.util.text.{Style, TextComponentTranslation, TextFormatting}
import net.minecraftforge.event.entity.player.PlayerInteractEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.PlayerEvent.{PlayerLoggedInEvent, PlayerLoggedOutEvent}
import net.minecraftforge.fml.common.gameevent.TickEvent.PlayerTickEvent
import net.minecraftforge.fml.relauncher.Side
import net.siberos.mcrealkingdoms.serverauthmod.ServerAuthMod

/**
  * Created by Asier on 15/10/2016.
  */
class CommonProxyEventHandler {

  val log: Logger = Logger.getLogger(s"[${ServerAuthMod.NAME}] - ${getClass.getName}")
  val tobeAuthenticated: ListBuffer[(EntityPlayer, BlockPos, InventoryPlayer)] = new ListBuffer[(EntityPlayer, BlockPos, InventoryPlayer)]()

  /**
    * Fires when a player logs into the server.
    * @param event
    */
  @SubscribeEvent
  def playerLoggedIn(event: PlayerLoggedInEvent): Unit = {
    if (event.player.getServer.isDedicatedServer) {
      log.info(s"Adding ${event.player.getName} to the unauthentificated player list.")
      // Adding the player to the "To be authenticated" list
      tobeAuthenticated.append((event.player, new BlockPos(event.player.getPosition.getX, event.player.getPosition.getY, event.player.getPosition.getZ), event.player.inventory))

      // Replacing its inventory with an empty one
      // This inventory can't get any dropped item since its size is 0
      event.player.inventory = new InventoryPlayer(event.player) {
        override def getInventoryStackLimit: Int = 0
        override def getSizeInventory: Int = 0
      }


      // Setting the player invulnerable
      event.player.setEntityInvulnerable(true)

      // Sending authentication message
      event.player.addChatComponentMessage(new TextComponentTranslation("login.auth_instructions_msg", TextFormatting.YELLOW, TextFormatting.GOLD))
    }
  }

  /**
    * Fires when a player logs out from the server
    * @param event
    */
  @SubscribeEvent
  def playerLoggedOut(event: PlayerLoggedOutEvent): Unit = {
    if (event.player.getServer.isDedicatedServer) {
      if (tobeAuthenticated.exists(t => t._1.getUniqueID == event.player.getUniqueID)) {
        // Removing player from unauthentication list since it is logging out.
        val t = tobeAuthenticated.find(t => t._1.getUniqueID == event.player.getUniqueID).get
        tobeAuthenticated.remove(tobeAuthenticated.indexOf(t))
        log.info(s"Removing ${event.player.getName} from the unauthenticated player list. (Reason: Logout)")
      }
    }
  }

  /**
    * Fires every game tick
    * @param event
    */
  @SubscribeEvent
  def playerTick(event: PlayerTickEvent): Unit = {
    if (event.side == Side.SERVER) {
      if (tobeAuthenticated.exists(t => t._1 == event.player)) {
        // Fixing the player to the initial position so it can't move.
        val pos = tobeAuthenticated.find(t => t._1 == event.player).get._2
        event.player.getServer.getEntityFromUuid(event.player.getUniqueID).setPositionAndUpdate(pos.getX, pos.getY, pos.getZ)
      }
    }
  }

  /**
    * Fires when a player interacts (mouse button) with something. (Like blocks)
    * @param event
    */
  @SubscribeEvent
  def playerInteractEvent(event: PlayerInteractEvent): Unit = {
    if (event.getSide == Side.SERVER) {
      if (tobeAuthenticated.exists(t => t._1.getUniqueID == event.getEntity.getUniqueID)) {
        // Cancel the event so the player can't use items or break blocks.
        event.setCanceled(true)
      }
    }
  }

}
