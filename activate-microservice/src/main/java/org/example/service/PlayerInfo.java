package org.example.service;

public class PlayerInfo {
    private String name;
    private String id;
    private int gamesWon;
    private int gamesLost;
    private String timeSpent;

    private PlayerInfo() {}

    public static PlayerInfo newInstance() {
        PlayerInfo pi = new PlayerInfo();
        pi.name = "John Doe";
        pi.id = "1534349879";
        pi.gamesWon = 10;
        pi.gamesLost = 20;
        pi.timeSpent = "10h28m";
        return pi;
    }
}
