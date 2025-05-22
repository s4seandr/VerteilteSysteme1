package org.Aufgabe1;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TokenRingProcess {
    public static void main(String[] args) {
        if (args.length < 3) {
            System.out.println("Usage: java org.Aufgabe1.TokenRingProcess <processId> <totalProcesses> <initialProbability>");
            System.exit(1);
        }

        int processId = Integer.parseInt(args[0]);
        int totalProcesses = Integer.parseInt(args[1]);
        double probability = Double.parseDouble(args[2]);

        int basePort = 5000;
        int myPort = basePort + processId;
        int nextPort = basePort + ((processId + 1) % totalProcesses);

        String multicastAddress = "230.0.0.0";
        int multicastPort = 4446;
        int roundsWithoutFirework = 0;
        int terminationThreshold = 5;

        List<Long> roundTimes = new ArrayList<>();
        int totalTokenRounds = 0;
        int totalMulticastsSent = 0;
        long roundStartTime;

        try {
            InetAddress multicastGroup = InetAddress.getByName(multicastAddress);
            MulticastSocket mSocket = new MulticastSocket(multicastPort);
            mSocket.joinGroup(multicastGroup);

            Thread multicastListener = new Thread(() -> {
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        mSocket.receive(packet);
                        String message = new String(packet.getData(), 0, packet.getLength());
                        System.out.println("[Multicast] Empfangene Nachricht: " + message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            multicastListener.start();

            DatagramSocket tokenSocket = new DatagramSocket(myPort);

            if (processId == 0) {
                Thread.sleep(3000);
                byte[] token = "TOKEN".getBytes();
                DatagramPacket tokenPacket = new DatagramPacket(token, token.length, InetAddress.getByName("127.0.0.1"), nextPort);
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

                    boolean fireworkTriggered = Math.random() < probability;

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

                    probability /= 2;
                    System.out.println("Prozess " + processId + " reduziert p auf: " + probability);

                    long roundEndTime = System.currentTimeMillis();
                    roundTimes.add(roundEndTime - roundStartTime);

                    if (roundsWithoutFirework >= terminationThreshold) {
                        long minTime = roundTimes.stream().mapToLong(Long::longValue).min().orElse(0);
                        long maxTime = roundTimes.stream().mapToLong(Long::longValue).max().orElse(0);
                        double avgTime = roundTimes.stream().mapToLong(Long::longValue).average().orElse(0);

                        System.out.println("\n📊 Statistik vor Beendigung:");
                        System.out.println("🔹 Token-Runden: " + totalTokenRounds);
                        System.out.println("🔹 Multicasts gesendet: " + totalMulticastsSent);
                        System.out.println("🔹 Min. Rundenzeit: " + minTime + " ms");
                        System.out.println("🔹 Max. Rundenzeit: " + maxTime + " ms");
                        System.out.println("🔹 Mittlere Rundenzeit: " + avgTime + " ms");
                        System.out.println("⚠️ Keine Feuerwerksraketen mehr. Prozess beendet!");
                        break;
                    }

                    byte[] tokenBytes = "TOKEN".getBytes();
                    DatagramPacket tokenOutPacket = new DatagramPacket(tokenBytes, tokenBytes.length, InetAddress.getByName("127.0.0.1"), nextPort);
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
}
