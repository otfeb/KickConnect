package projects.kickConnect.dto;

public record MatchDTO(
        Long app_id,
        String app_name,
        String match_id,
        String match_url,
        String match_date,
        String match_time,
        String place,
        String area,
        String gender,
        String match_players,

        // 플랩: 'apply_status' -> 신청 가능(available), 마감 임박(hurry), 마감(full)
        String apply_status
) {
}
