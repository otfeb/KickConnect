package projects.kickConnect.crawler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Component
public class MatchCrawler {

    public static void main(String[] args) throws IOException {
        try {
            // 요청 URL
            String url = "https://www.plabfootball.com/api/v2/integrated-matches/?page_size=100&ordering=schedule&sch=2025-01-22&hide_soldout=&region=1"; // 실제 API URL로 대체

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

            // 변환된 데이터를 출력
            for (Map<String, Object> match : matchList) {
                System.out.println("ID: " + match.get("id"));
                System.out.println("구장 정보: " + match.get("label_title2"));
                System.out.println("날짜: " + match.get("schedule"));
                System.out.println("지역: " + match.get("area_group_name"));
                System.out.println("성별: " + match.get("display_level"));
                System.out.println("매치 인원: " + match.get("player_cnt") + "vs" + match.get("player_cnt"));
                System.out.println("---------------------------");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
