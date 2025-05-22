package org.Aufgabe2;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TokenRingProcess {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java org.Aufgabe2.TokenRingProcess <processId>");
            System.exit(1);
        }

        int processId = Integer.parseInt(args[0]);

        List<String> ips = loadIPsFromConfig();
        if (ips.isEmpty() || processId >= ips.size()) {
            System.out.println("Fehler: Prozess-ID außerhalb des gültigen Bereichs!");
            System.exit(1);
        }

        String myIP = ips.get(processId);
        String nextIP = ips.get((processId + 1) % ips.size());

        int basePort = 5000;
        int myPort = basePort + processId;
        int nextPort = basePort + ((processId + 1) % ips.size());

        String multicastAddress = "230.0.0.0";
        int multicastPort = 4446;
        int roundsWithoutFirework = 0;
        int terminationThreshold = 5;

        List<Long> roundTimes = new ArrayList<>();
        int totalTokenRounds = 0;
        int totalMulticastsSent = 0;
        long roundStartTime;

        waitForPeer(nextIP);

        try {
            InetAddress multicastGroup = InetAddress.getByName(multicastAddress);
            MulticastSocket mSocket = new MulticastSocket(multicastPort);
            mSocket.joinGroup(multicastGroup);

            DatagramSocket tokenSocket = new DatagramSocket(myPort);

            if (processId == 0) {
                Thread.sleep(3000);
                byte[] token = "TOKEN".getBytes();
                DatagramPacket tokenPacket = new DatagramPacket(token, token.length, InetAddress.getByName(nextIP), nextPort);
                tokenSocket.send(tokenPacket);
                System.out.println("Prozess 0 startet den Token-Kreis.");
            }

            while (true) {
                roundStartTime = System.currentTimeMillis();
                byte[] buffer = new byte[1024];
                DatagramPacket incomingPacket = new DatagramPacket(buffer, buffer.length);
                tokenSocket.receive(incomingPacket);

                String incomingMessage = new String(incomingPacket.getData(), 0, incomingPacket.getLength());

                if ("TOKEN".equals(incomingMessage)) {
                    totalTokenRounds++;
                    System.out.println("Prozess " + processId + " hat den TOKEN erhalten (Runde " + totalTokenRounds + ").");

                    boolean fireworkTriggered = Math.random() < 0.5;  // Standardwahrscheinlichkeit

                    if (fireworkTriggered) {
                        String fireworkMessage = "Feuerwerksrakete gezündet von Prozess " + processId;
                        byte[] fwBytes = fireworkMessage.getBytes();
                        DatagramPacket fwPacket = new DatagramPacket(fwBytes, fwBytes.length, multicastGroup, multicastPort);
                        try (DatagramSocket fwSocket = new DatagramSocket()) {
                            fwSocket.send(fwPacket);
                        }
                        totalMulticastsSent++;
                        System.out.println("Prozess " + processId + " hat die Feuerwerksrakete gezündet!");
                        roundsWithoutFirework = 0;
                    } else {
                        roundsWithoutFirework++;
                    }

                    long roundEndTime = System.currentTimeMillis();
                    roundTimes.add(roundEndTime - roundStartTime);

                    if (roundsWithoutFirework >= terminationThreshold) {
                        System.out.println("\n📊 Statistik vor Beendigung:");
                        System.out.println("🔹 Token-Runden: " + totalTokenRounds);
                        System.out.println("🔹 Multicasts gesendet: " + totalMulticastsSent);
                        System.out.println("🔹 Min. Rundenzeit: " + roundTimes.stream().mapToLong(Long::longValue).min().orElse(0) + " ms");
                        System.out.println("🔹 Max. Rundenzeit: " + roundTimes.stream().mapToLong(Long::longValue).max().orElse(0) + " ms");
                        System.out.println("🔹 Mittlere Rundenzeit: " + roundTimes.stream().mapToLong(Long::longValue).average().orElse(0) + " ms");
                        System.out.println("⚠️ Keine Feuerwerksraketen mehr. Prozess beendet!");
                        break;
                    }

                    byte[] tokenBytes = "TOKEN".getBytes();
                    DatagramPacket tokenOutPacket = new DatagramPacket(tokenBytes, tokenBytes.length, InetAddress.getByName(nextIP), nextPort);
                    tokenSocket.send(tokenOutPacket);
                }
            }

            tokenSocket.close();
            mSocket.leaveGroup(multicastGroup);
            mSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static List<String> loadIPsFromConfig() {
        List<String> ips = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/org/Aufgabe2/config.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                ips.add(line.trim());
            }
        } catch (IOException e) {
            System.out.println("Fehler beim Laden der Konfigurationsdatei!");
            System.exit(1);
        }
        return ips;
    }

    private static void waitForPeer(String nextIP) {
        while (true) {
            try {
                if (InetAddress.getByName(nextIP).isReachable(2000)) {
                    System.out.println("✅ Rechner " + nextIP + " erreichbar!");
                    return;
                } else {
                    System.out.println("⏳ Warte auf Rechner " + nextIP + "...");
                    Thread.sleep(3000);
                }
            } catch (Exception e) {
                System.out.println("❌ Fehler beim Erreichen von " + nextIP);
            }
        }
    }
}
