package projects.kickConnect.crawler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import projects.kickConnect.dto.MatchDTO;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class MatchCrawler {

    @Bean
    public List<MatchDTO> plab() {

        List<MatchDTO> list = new ArrayList<>();

        try {
            // 요청 URL
            String url = "https://www.plabfootball.com/api/v2/integrated-matches/?page_size=200&ordering=schedule&sch=2025-01-22&hide_soldout=&region=1";

            // HttpClient 생성
            HttpClient client = HttpClient.newHttpClient();

            // HttpRequest 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0") // 필요 시 헤더 추가
                    .build();

            // 요청 보내기
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            ObjectMapper objectMapper = new ObjectMapper();
            List<Map<String, Object>> matchList = objectMapper.readValue(
                    response.body(), new TypeReference<List<Map<String, Object>>>() {}
            );

            for (Map<String, Object> match : matchList) {
                // 소셜 경기만
                if (match.get("product_type").toString().equals("social")) {
                    MatchDTO dto = new MatchDTO(
                            1L,
                            "plab",
                            match.get("id").toString(),
                            match.get("schedule").toString(),
                            match.get("label_schedule9").toString(),
                            match.get("label_title2").toString(),
                            match.get("area_group_name").toString(),
                            match.get("display_level").toString(),
                            match.get("player_cnt").toString() + "vs" + match.get("player_cnt").toString()
                    );
                    list.add(dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }
}
