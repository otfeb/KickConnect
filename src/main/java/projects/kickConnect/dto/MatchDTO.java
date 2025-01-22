package projects.kickConnect.dto;

public record MatchDTO(
        Long appId,
        String appName,
        String matchId,
        String matchDate,
        String matchTime,
        String place,
        String area,
        String gender,
        String matchPlayers
) {
}
