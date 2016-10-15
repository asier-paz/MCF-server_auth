package net.siberos.mcrealkingdoms.serverauthmod

import net.minecraft.init.Blocks
import net.minecraftforge.fml.common.{Mod, SidedProxy}
import net.minecraftforge.fml.common.Mod.EventHandler
import net.minecraftforge.fml.common.event.{FMLInitializationEvent, FMLPostInitializationEvent, FMLPreInitializationEvent}
import net.siberos.mcrealkingdoms.serverauthmod.proxy.CommonProxy

/**
  * Created by Asier on 15/10/2016.
  */
@Mod(modid = ServerAuthMod.MODID, version = ServerAuthMod.VERSION, name = ServerAuthMod.NAME, modLanguage = "scala")
object ServerAuthMod {
  final val MODID = "serverauthmod"
  final val VERSION = "1.0"
  final val NAME = "Server Auth Mod"

  @SidedProxy(clientSide = "net.siberos.mcrealkingdoms.serverauthmod.proxy.ClientProxy", serverSide = "net.siberos.mcrealkingdoms.serverauthmod.proxy.CommonProxy")
  var proxy: CommonProxy = null

  @EventHandler
  def preInitialization(event: FMLPreInitializationEvent): Unit = {
    proxy.preInitialization(event)
  }

  @EventHandler
  def initialization(event: FMLInitializationEvent): Unit = {
    proxy.initialization(event)
    // some example code
    println("DIRT BLOCK >> " + Blocks.DIRT.getUnlocalizedName)
  }

  @EventHandler
  def postInitialization(event: FMLPostInitializationEvent): Unit = {
    proxy.postInitialization(event)
  }

}
