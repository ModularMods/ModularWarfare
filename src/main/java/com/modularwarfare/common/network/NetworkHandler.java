package com.modularwarfare.common.network;

import com.modularwarfare.ModularWarfare;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.FMLEmbeddedChannel;
import net.minecraftforge.fml.common.network.FMLOutboundHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * ModularWarfare packet handler class. Directs packet data to packet classes.
 *
 * @author ModularWarfare
 * With much inspiration from http://www.minecraftforge.net/wiki/Netty_Packet_Handling
 */
@ChannelHandler.Sharable
public class NetworkHandler extends MessageToMessageCodec<FMLProxyPacket, PacketBase> {
    //Map of channels for each side
    private EnumMap<Side, FMLEmbeddedChannel> channels;
    //The list of registered packets. Should contain no more than 256 packets.
    private LinkedList<Class<? extends PacketBase>> packets = new LinkedList<Class<? extends PacketBase>>();
    //Whether or not ModularWarfare has initialised yet. Once true, no more packets may be registered.
    private boolean modInitialised = false;

    /**
     * Store received packets in these queues and have the main Minecraft threads use these
     */
    private ConcurrentLinkedQueue<PacketBase> receivedPacketsClient = new ConcurrentLinkedQueue<PacketBase>();
    private HashMap<String, ConcurrentLinkedQueue<PacketBase>> receivedPacketsServer = new HashMap<String, ConcurrentLinkedQueue<PacketBase>>();

    /**
     * Registers a packet with the handler
     */
    public boolean registerPacket(Class<? extends PacketBase> cl) {
        if (packets.size() > 256) {
            ModularWarfare.LOGGER.warn("Packet limit exceeded in Modular Warfare packet handler by packet " + cl.getCanonicalName() + ".");
            return false;
        }
        if (packets.contains(cl)) {
            ModularWarfare.LOGGER.warn("Tried to register " + cl.getCanonicalName() + " packet class twice.");
            return false;
        }
        if (modInitialised) {
            ModularWarfare.LOGGER.warn("Tried to register packet " + cl.getCanonicalName() + " after mod initialisation.");
            return false;
        }

        packets.add(cl);
        return true;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, PacketBase msg, List<Object> out) throws Exception {
        try {
            //Define a new buffer to store our data upon encoding
            ByteBuf encodedData = Unpooled.buffer();
            //Get the packet class
            Class<? extends PacketBase> cl = msg.getClass();

            //If this packet has not been registered by our handler, reject it
            if (!packets.contains(cl))
                throw new NullPointerException("Packet not registered : " + cl.getCanonicalName());

            //Like a packet ID. Stored as the first entry in the packet code for recognition
            byte discriminator = (byte) packets.indexOf(cl);
            encodedData.writeByte(discriminator);
            //Get the packet class to encode our packet
            msg.encodeInto(ctx, encodedData);

            //Convert our packet into a Forge packet to get it through the Netty system
            FMLProxyPacket proxyPacket = new FMLProxyPacket(new PacketBuffer(encodedData.copy()), ctx.channel().attr(NetworkRegistry.FML_CHANNEL).get());
            //Add our packet to the outgoing packet queue
            out.add(proxyPacket);
        } catch (Exception e) {
            ModularWarfare.LOGGER.error("ERROR encoding packet");
            ModularWarfare.LOGGER.throwing(e);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FMLProxyPacket msg, List<Object> out) throws Exception {
        try {
            //Get the encoded data from the incoming packet
            ByteBuf encodedData = msg.payload();
            //Get the class for interpreting this packet
            byte discriminator = encodedData.readByte();
            Class<? extends PacketBase> cl = packets.get(discriminator);

            //If this discriminator returns no class, reject it
            if (cl == null)
                throw new NullPointerException("Packet not registered for discriminator : " + discriminator);

            //Create an empty packet and decode our packet data into it
            PacketBase packet = cl.newInstance();
            packet.decodeInto(ctx, encodedData.slice());
            //Check the side and handle our packet accordingly
            switch (FMLCommonHandler.instance().getEffectiveSide()) {
                case CLIENT: {
                    receivedPacketsClient.offer(packet);
                    //packet.handleClientSide(getClientPlayer());
                    break;
                }
                case SERVER: {
                    INetHandler netHandler = ctx.channel().attr(NetworkRegistry.NET_HANDLER).get();
                    EntityPlayer player = ((NetHandlerPlayServer) netHandler).player;
                    if (!receivedPacketsServer.containsKey(player.getName()))
                        receivedPacketsServer.put(player.getName(), new ConcurrentLinkedQueue<PacketBase>());
                    receivedPacketsServer.get(player.getName()).offer(packet);
                    //packet.handleServerSide();
                    break;
                }
            }
        } catch (Exception e) {
            ModularWarfare.LOGGER.error("ERROR decoding packet");
            ModularWarfare.LOGGER.throwing(e);
        }
    }

    public void handleClientPackets() {
        for (PacketBase packet = receivedPacketsClient.poll(); packet != null; packet = receivedPacketsClient.poll()) {
            packet.handleClientSide(getClientPlayer());
        }
    }

    public void handleServerPackets() {
        for (String playerName : receivedPacketsServer.keySet()) {
            ConcurrentLinkedQueue<PacketBase> receivedPacketsFromPlayer = receivedPacketsServer.get(playerName);
            EntityPlayerMP player = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().getPlayerByUsername(playerName);
            for (PacketBase packet = receivedPacketsFromPlayer.poll(); packet != null; packet = receivedPacketsFromPlayer.poll()) {
                packet.handleServerSide(player);
            }
        }
    }

    /**
     * Initialisation method called from FMLInitializationEvent in ModularWarfare
     */
    public void initialise() {
        channels = NetworkRegistry.INSTANCE.newChannel("ModularWarfare", this);

        registerPacket(PacketGunFire.class);
        registerPacket(PacketPlaySound.class);
        registerPacket(PacketPlayHitmarker.class);
        registerPacket(PacketGunSwitchMode.class);
        registerPacket(PacketGunReload.class);
        registerPacket(PacketGunReloadSound.class);
        registerPacket(PacketGunAddAttachment.class);
        registerPacket(PacketGunUnloadAttachment.class);
        registerPacket(PacketClientAnimation.class);
        registerPacket(PacketGunTrail.class);
        registerPacket(PacketAimingRequest.class);
        registerPacket(PacketAimingReponse.class);
        registerPacket(PacketDecal.class);
        registerPacket(PacketOpenNormalInventory.class);
        registerPacket(PacketOpenExtraArmorInventory.class);
        registerPacket(PacketSyncBackWeapons.class);
        registerPacket(PacketBulletSnap.class);
        registerPacket(PacketPlayerHit.class);

        registerPacket(PacketSyncExtraSlot.class);
        registerPacket(PacketOpenGui.class);
        registerPacket(PacketExplosion.class);
        registerPacket(PacketClientKillFeedEntry.class);

        registerPacket(PacketFlashClient.class);
        registerPacket(PacketExpGunFire.class);
        registerPacket(PacketGunTrailAskServer.class);
        registerPacket(PacketExpShot.class);

        registerPacket(PacketBulletHoleDespawnTime.class);
    }

    /**
     * Post-Initialisation method called from FMLPostInitializationEvent in ModularWarfare
     * Logically sorts the packets client and server side to ensure a matching ordering
     */
    public void postInitialise() {
        if (modInitialised)
            return;

        modInitialised = true;
        //Define our comparator on the fly and apply it to our list
        Collections.sort(packets,
                new Comparator<Class<? extends PacketBase>>() {
                    @Override
                    public int compare(Class<? extends PacketBase> c1, Class<? extends PacketBase> c2) {
                        int com = String.CASE_INSENSITIVE_ORDER.compare(c1.getCanonicalName(), c2.getCanonicalName());
                        if (com == 0)
                            com = c1.getCanonicalName().compareTo(c2.getCanonicalName());
                        return com;
                    }
                });
    }

    @SideOnly(Side.CLIENT)
    private EntityPlayer getClientPlayer() {
        return Minecraft.getMinecraft().player;
    }

    /**
     * Send a packet to all players
     */
    public void sendToAll(PacketBase packet) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALL);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /**
     * Send a packet to a player
     */
    public void sendTo(PacketBase packet, EntityPlayerMP player) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.PLAYER);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(player);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /**
     * Send a packet to all around a point
     */
    public void sendToAllAround(PacketBase packet, NetworkRegistry.TargetPoint point) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.ALLAROUNDPOINT);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(point);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /**
     * Send a packet to all in a dimension
     */
    public void sendToDimension(PacketBase packet, int dimensionID) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.DIMENSION);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(dimensionID);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    /**
     * Send a packet to the server
     */
    public void sendToServer(PacketBase packet) {
        channels.get(Side.CLIENT).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TOSERVER);
        channels.get(Side.CLIENT).writeAndFlush(packet);
    }


    /**
     * Sends this message to everyone tracking an entity.
     * The {@link IMessageHandler} for this message type should be on the CLIENT side.
     * This is not equivalent to {@link #(IMessage, NetworkRegistry.TargetPoint)}
     * because entities have different tracking distances based on their type.
     */
    public void sendToAllTracking(PacketBase packet, Entity entity) {
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGET).set(FMLOutboundHandler.OutboundTarget.TRACKING_ENTITY);
        channels.get(Side.SERVER).attr(FMLOutboundHandler.FML_MESSAGETARGETARGS).set(entity);
        channels.get(Side.SERVER).writeAndFlush(packet);
    }

    //Vanilla packets follow

    /**
     * Send a packet to all players
     */
    public void sendToAll(Packet packet) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayers(packet);
    }

    /**
     * Send a packet to a player
     */
    public void sendTo(Packet packet, EntityPlayerMP player) {
        player.connection.sendPacket(packet);
    }

    /**
     * Send a packet to all around a point
     */
    public void sendToAllAround(Packet packet, NetworkRegistry.TargetPoint point) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendToAllNearExcept(null, point.x, point.y, point.z, point.range, point.dimension, packet);
    }

    /**
     * Send a packet to all in a dimension
     */
    public void sendToDimension(Packet packet, int dimensionID) {
        FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList().sendPacketToAllPlayersInDimension(packet, dimensionID);
    }

    /**
     * Send a packet to the server
     */
    public void sendToServer(Packet packet) {
        Minecraft.getMinecraft().player.connection.sendPacket(packet);
    }

    /**
     * Send a packet to all around a point without having to create one's own TargetPoint
     */
    public void sendToAllAround(PacketBase packet, double x, double y, double z, float range, int dimension) {
        sendToAllAround(packet, new NetworkRegistry.TargetPoint(dimension, x, y, z, range));
    }
}