package service;

import java.util.List;

public record ListGamesResult(List<GameSummary> games) {
    public record GameSummary(Integer gameID, String gameName, String whiteUsername, String blackUsername) {
    }
}
