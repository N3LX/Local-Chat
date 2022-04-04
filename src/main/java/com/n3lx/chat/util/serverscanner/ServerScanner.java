package com.n3lx.chat.util.serverscanner;

import com.n3lx.chat.server.Server;
import com.n3lx.chat.util.Settings;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServerScanner {

    private static final Logger LOGGER = Logger.getLogger(Server.class.getName());

    public static List<String> getListOfOnlineServers() {
        //Get the IP of our machine
        String localhostIp = getLocalhostIP();

        //Ensure that the machine is connected to a network
        if (localhostIp == null) {
            return new ArrayList<>();
        }

        //Check if the address is in a private address space
        if (!validatePrivateIPAddress(localhostIp)) {
            return new ArrayList<>();
        }

        //Scan network
        List<String> availableServers = new ArrayList<>();
        Map<String, Boolean> scanResults = scanNetwork(localhostIp, Settings.getPort());

        for (String ip : scanResults.keySet()) {
            if (scanResults.get(ip)) {
                availableServers.add(ip);
            }
        }

        return availableServers;
    }

    private static Map<String, Boolean> scanNetwork(String ipAddress, int port) {
        String[] ipArray = ipAddress.split("\\.");

        ExecutorService executorService = Executors.newFixedThreadPool(256);
        Map<String, Future<Boolean>> results = new HashMap<>();

        for (int i = 0; i < 256; i++) {
            String ip = String.join(".", ipArray[0], ipArray[1],
                    ipArray[2], String.valueOf(i));
            results.put(ip, executorService.submit(new SocketScanner(ip, port)));
        }

        executorService.shutdown();

        Map<String, Boolean> availableServers = new HashMap<>();

        for (String ip : results.keySet()) {
            try {
                availableServers.put(ip, results.get(ip).get());
            } catch (InterruptedException | ExecutionException e) {
                LOGGER.log(Level.WARNING, "An error has occurred while scanning " + ip, e);
            }
        }

        return availableServers;
    }

    public static String getLocalhostIP() {
        String ip = null;

        //This code checks for a valid ip address on all interfaces,
        //much more practical especially on devices with multiple network cards.
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface inter = interfaces.nextElement();
                Enumeration<InetAddress> addresses = inter.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress address = addresses.nextElement();
                    if (address.getClass() == Inet4Address.class
                            && validatePrivateIPAddress(address.getHostAddress())) {
                        ip = address.getHostAddress();
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            LOGGER.log(Level.SEVERE, "Could not obtain the IP address", e);
        }

        return ip;
    }

    /**
     * Checks if IP is compliant with https://www.rfc-editor.org/rfc/rfc1918
     *
     * @param ipAddress
     * @return true - if compliant with RFC, false otherwise
     */
    private static boolean validatePrivateIPAddress(String ipAddress) {
        return ipAddress.startsWith("192.168.") ||
                ipAddress.startsWith("10.") ||
                (ipAddress.startsWith("172.") &&
                        Integer.parseInt(ipAddress.split("\\.")[1]) >= 16
                        && Integer.parseInt(ipAddress.split("\\.")[1]) <= 31);
    }

}
