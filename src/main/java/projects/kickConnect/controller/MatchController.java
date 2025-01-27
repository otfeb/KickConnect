package projects.kickConnect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import projects.kickConnect.crawler.MatchCrawler;
import projects.kickConnect.dto.MatchDTO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchCrawler matchCrawler;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> allMatch(
            @RequestParam("matchDate") String matchDate,
            @RequestParam("region") String region,
            @RequestParam("gender") String gender,
            @RequestParam("soldout") String soldout
    ) {

        List<MatchDTO> totalMatchList = new ArrayList<>();

        List<MatchDTO> plabMatchList = matchCrawler.plab(matchDate, region);
        List<MatchDTO> puzzleMatchList = matchCrawler.puzzle(matchDate, region);

        // only 부산
        if (region.equals("3")) {
            List<MatchDTO> urbanMatchList = matchCrawler.urban(matchDate, region);
            totalMatchList.addAll(urbanMatchList);
        } else if (region.equals("0")) {    // only 서울
            List<MatchDTO> withMatchList = matchCrawler.with(matchDate, region);
            totalMatchList.addAll(withMatchList);
        }

        // 각 어플의 매치 경기 합치기
        totalMatchList.addAll(plabMatchList);
        totalMatchList.addAll(puzzleMatchList);

        totalMatchList.sort(
                Comparator.comparing(MatchDTO::match_date)  // 1순위: match_date
                        .thenComparing(MatchDTO::match_time) // 2순위: match_time
                        .thenComparing(MatchDTO::app_name)   // 3순위: app_name
        );

        // 성별 필터링
        if (!gender.equals("")) {
            totalMatchList = totalMatchList.stream()
                    .filter(match -> {
                        if (gender.equals("0")) {
                            return match.gender().equals("남녀모두");
                        } else if (gender.equals("1")) {
                            return match.gender().equals("남자");
                        } else if (gender.equals("-1")) {
                            return match.gender().equals("여자");
                        }
                        return false; // 잘못된 값은 필터링 제외
                    })
                    .collect(Collectors.toList());
        }

        if (soldout.equals("true")) {
            List<MatchDTO> hideSoldoutMatchList = totalMatchList.stream()
                    .filter((item) -> !item.apply_status().equals("full"))
                    .toList();

            return ResponseEntity.ok(hideSoldoutMatchList);
        }

        return ResponseEntity.ok(totalMatchList);
    }
}