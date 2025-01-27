package projects.kickConnect.crawler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import projects.kickConnect.dto.MatchDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MatchCrawler {

    private static final HttpClient client = HttpClient.newHttpClient();
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public List<MatchDTO> plab(String matchDate, String region, String gender, String soldout) {

        List<MatchDTO> list = new ArrayList<>();

        try {
            // 지역
            if (Integer.parseInt(region) < 3) {
                region = String.valueOf(Integer.parseInt(region) + 1);
            } else {
                region = "6";
            }

            // 성별
            if (gender.equals("0")) {
                gender = "&sex=0";
            } else if (gender.equals("1")) {
                gender = "&sex=1";
            } else if (gender.equals("-1")) {
                gender = "&sex=-1";
            }

            // 마감 가리기
            String hide_soldout = "";
            if (soldout.equals("true")) {
                hide_soldout = "&hide_soldout=";
            }

            // 요청 URL
            String url = "https://www.plabfootball.com/api/v2/integrated-matches/?page_size=700&ordering=schedule&sch=" + matchDate + gender + hide_soldout + "&region=" + region;
            log.info("플랩풋볼 요청 URL: "+url);

            // HttpRequest 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")// 필요 시 헤더 추가
                    .build();

            // 요청 보내기
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            List<Map<String, Object>> matchList = objectMapper.readValue(
                    response.body(), new TypeReference<>() {}
            );

            for (Map<String, Object> match : matchList) {
                // 소셜 경기만
                if (match.get("product_type").toString().equals("social")) {

                    String match_id = match.get("id").toString();
                    String match_url = "https://www.plabfootball.com/match/" + match_id;

                    String match_date = match.get("schedule").toString().substring(0, 10);

                    String match_time_before_process = match.get("label_schedule9").toString();
                    String match_time = match_time_before_process.substring(match_time_before_process.length() - 5);

                    MatchDTO dto = new MatchDTO(
                            1L,
                            "plab",
                            match_id,
                            match_url,
                            match_date,
                            match_time,
                            match.get("label_title2").toString(),
                            match.get("area_group_name").toString(),
                            match.get("display_level").toString(),
                            match.get("player_cnt").toString() + "vs" + match.get("player_cnt").toString(),
                            match.get("apply_status").toString()
                    );
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<MatchDTO> puzzle(String matchDate, String region, String gender, String soldout) {
        List<MatchDTO> list = new ArrayList<>();

        try {
            String url = "https://puzzleplay.kr/filter";
            String body = "{\"XHR\":true,\"active_date\":\"" + matchDate + "\",\"match_date\":\"" + matchDate + "\"}";
            log.info("퍼즐플레이 요청 body: " + body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Map<String, Object> extractBody = objectMapper.readValue(
                    response.body(), new TypeReference<>() {
                    }
            );

            List<Map<String, Object>> matchList = (List<Map<String, Object>>) extractBody.get("list");

            for (Map<String, Object> match : matchList) {

                Map<String, Object> groundInfo = (Map<String, Object>) match.get("ground_info");
                Map<String, Object> personnel = (Map<String, Object>) match.get("personnel");

                String match_id = match.get("_id").toString();
                String match_url = "https://puzzleplay.kr/social/" + match_id;

                String groundName = groundInfo.get("groundName").toString();
                String groundRegion = groundInfo.get("region").toString();

                int max_cnt = (int) personnel.get("max");
                int player_cnt = (int) match.get("player_cnt");
                String apply_status = "available";

                if (max_cnt == player_cnt) {
                    apply_status = "full";
                } else if (max_cnt - player_cnt < 10) {
                    apply_status = "hurry";
                }

                MatchDTO dto = new MatchDTO(
                        2L,
                        "puzzle",
                        match_id,
                        match_url,
                        match.get("match_date").toString(),
                        match.get("match_time").toString(),
                        groundName,
                        groundRegion,
                        match.get("sex").toString(),
                        match.get("match_vs").toString() + "vs" + match.get("match_vs").toString(),
                        apply_status
                );
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
