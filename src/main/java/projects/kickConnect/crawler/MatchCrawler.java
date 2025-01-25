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

    public List<MatchDTO> plab(String sch, String region, String gender, String soldout) {

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
            String url = "https://www.plabfootball.com/api/v2/integrated-matches/?page_size=700&ordering=schedule&sch=" + sch + gender + hide_soldout + "&region=" + region;
            log.info("실제 요청 URL: "+url);

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
}
