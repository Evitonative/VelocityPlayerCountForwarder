package de.evitonative.velocityPlayerCountForwarder;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyPingEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerPing;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Plugin(id = "velocityplayercountforwarder", name = "VelocityPlayerCountForwarder", version = BuildConstants.VERSION, authors = {"Evito"})
public class VelocityPlayerCountForwarder {

    @Inject private ProxyServer proxy;

    @Subscribe
    public void onPingEvent(ProxyPingEvent event) {
        Optional<InetSocketAddress> serverAddress = event.getConnection().getVirtualHost();

        if (serverAddress.isEmpty()) return;

        List<String> hosts = proxy.getConfiguration().getForcedHosts().get(serverAddress.get().getHostString());
        if (hosts == null || hosts.isEmpty()) hosts = proxy.getConfiguration().getAttemptConnectionOrder();

        for (String host : hosts) {
            Optional<RegisteredServer> matchingServer =proxy.getAllServers().stream()
                    .filter(registeredServer -> registeredServer.getServerInfo().getName().equalsIgnoreCase(host)).findFirst();
            if (matchingServer.isEmpty()) continue;

            ServerPing ping;
            try {
                ping = matchingServer.get().ping().get(250, TimeUnit.MILLISECONDS);
            } catch (TimeoutException| InterruptedException | ExecutionException e) {
                continue;
            }
            if (ping == null) continue;

            Optional<ServerPing.Players> players = ping.getPlayers();
            if (players.isEmpty()) continue;

            int maxPlayers = players.get().getMax();
            int connectedPlayers = players.get().getOnline();

            ServerPing proxyPing = event.getPing().asBuilder()
                    .maximumPlayers(maxPlayers)
                    .onlinePlayers(connectedPlayers)
                    .build();

            event.setPing(proxyPing);
            return;
        }
    }
}
