package projects.kickConnect.dto;

public record MatchDTO(
        String appName,
        String matchDate,
        String matchTime,
        String place,
        String area,
        String gender,
        String matchPlayers
) {
}
