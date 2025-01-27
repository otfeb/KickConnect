package projects.kickConnect.crawler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
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

    public List<MatchDTO> plab(String matchDate, String region) {

        List<MatchDTO> list = new ArrayList<>();

        try {
            // 지역
            if (Integer.parseInt(region) < 3) {
                region = String.valueOf(Integer.parseInt(region) + 1);
            } else {
                region = "6";
            }

            // 요청 URL
            String url = "https://www.plabfootball.com/api/v2/integrated-matches/?page_size=700&ordering=schedule&sch=" + matchDate + "&region=" + region;
            log.info("플랩풋볼 요청 URL: "+url);

            // HttpRequest 생성
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("User-Agent", "Mozilla/5.0")    // 필요 시 헤더 추가
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
                            match.get("player_cnt").toString() + " vs " + match.get("player_cnt").toString(),
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

    public List<MatchDTO> puzzle(String matchDate, String region) {
        List<MatchDTO> list = new ArrayList<>();

        // 퍼즐 지역 코드(서울, 경기, 인천, 부산)
        if (region.equals("0")) {
            region = ",\"region\": [\"5e8190a8bb3b302ce2e03279\"]";
        } else if (region.equals("1")) {
            region = ",\"region\": [\"65126e1929b8b579c68f372e\", \"65126e3929b8b579c68f372f\", \"67079dff33a5b1290b8a3944\",\"67079e0d33a5b1290b8a3945\"]";
        } else if (region.equals("2")) {
            region = ",\"region\": [\"657004ee4cf9a54480c02ecc\"]";
        } else {
            region = ",\"region\": [\"65126e9529b8b579c68f3730\"]";
        }

        try {
            String url = "https://puzzleplay.kr/filter";
            String body = "{\"XHR\":true,\"active_date\":\"" + matchDate + "\",\"match_date\":\"" + matchDate + "\"" + region + "}";
            log.info("퍼즐플레이 요청 body: " + body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "Mozilla/5.0")
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

                String match_gender = "";
                if (match.get("sex").toString().equals("1")) {
                    match_gender = "남자";
                } else if (match.get("sex").toString().equals("2")) {
                    match_gender = "여자";
                } else {
                    match_gender = "남녀모두";
                }

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
                        match_gender,
                        match.get("match_vs").toString() + " vs " + match.get("match_vs").toString(),
                        apply_status
                );
                list.add(dto);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public List<MatchDTO> urban(String matchDate, String region) {
        List<MatchDTO> list = new ArrayList<>();
        String match_gender = "";

        try {
            String url = "https://urbanfootball.co.kr/result/result_get_data.php";
            // form 데이터 형식(기본 String 타입)
            String body = "mode=get_goods_list&date=" + matchDate + "&area=2";
            log.info("어반풋볼 요청: " + body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("User-Agent", "Mozilla/5.0")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Document document = Jsoup.parse(response.body());
            Elements matchList = document.select("ul.goods_table_item");

            for (Element element : matchList) {

                String match_id = element.attr("data_id");
                String match_url = "https://urbanfootball.co.kr/goods/goods_view.html?goods_no=" + match_id;

                Elements findArea = element.select("li.name div");

                if (element.select("span.sex").text().equals("혼성")) {
                    match_gender = "남녀모두";
                } else if (element.select("span.sex").text().equals("남성")) {
                    match_gender = "남자";
                } else {
                    match_gender = "여자";
                }

                Elements findPlayers = element.select("span.sex");

                String isFull = "available";
                if (element.select("li.apply .button > div:nth-child(1)").text().equals("마감 / 대기")) {
                    isFull = "full";
                }

                MatchDTO dto = new MatchDTO(
                        3L,
                        "urban",
                        match_id,
                        match_url,
                        matchDate,
                        element.select("li.time span").text(),
                        element.select("li.name div div").text(),
                        findArea.next().tagName("span").text(),
                        match_gender,
                        findPlayers.next().text(),
                        isFull
                );

                list.add(dto);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<MatchDTO> with(String matchDate, String region) {
        List<MatchDTO> list = new ArrayList<>();

        try {
            String url = "https://withfutsal.com/ajaxProcSocialMatch.php";
            String body = "cmd=search_info&day=" + matchDate + "&area_code=all&member_code=all&match_type=null&pajeon=null&gender=null&game_level=null";
            log.info("위드풋살 요청: " + body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .header("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8")
                    .header("User-Agent", "Mozilla/5.0")
                    .header("accept", "application/json, text/javascript, */*; q=0.01")
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Map<String, Object> wrapList = objectMapper.readValue(
                    response.body(), new TypeReference<>() {}
            );

            if (wrapList.get("return_msg").toString().equals("성공")) {
                List<Map<String, Object>> matchList = (List<Map<String, Object>>) wrapList.get("block_list");

                for (Map<String, Object> match : matchList) {

                    String match_id = match.get("idx").toString();
                    String match_url = "https://withfutsal.com/Sub/ground.php?block_idx=" + match_id;

                    String match_gender = "";
                    if (match.get("gender").equals("0")) {
                        match_gender = "남자";
                    } else if (match.get("gender").equals("1")) {
                        match_gender = "여자";
                    } else {
                        match_gender = "남녀모두";
                    }

                    String players = String.valueOf(Integer.parseInt(match.get("personnel_max").toString()) / 3);

                    String isFull = "available";
                    if (match.get("personnel_max").toString().equals(match.get("buy_count").toString())) {
                        isFull = "full";
                    }

                    MatchDTO dto = new MatchDTO(
                            4L,
                            "with",
                            match_id,
                            match_url,
                            matchDate,
                            match.get("play_time").toString(),
                            match.get("mem_name").toString() + match.get("stadium_name").toString(),
                            null,
                            match_gender,
                            players + " vs " + players,
                            isFull
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
