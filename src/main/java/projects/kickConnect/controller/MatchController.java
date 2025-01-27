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

        List<MatchDTO> plabMatchList = matchCrawler.plab(matchDate, region, gender, soldout);
        List<MatchDTO> puzzleMatchList = matchCrawler.puzzle(matchDate, region, gender, soldout);

//         각 어플의 매치 경기 합치기
        totalMatchList.addAll(plabMatchList);
        totalMatchList.addAll(puzzleMatchList);

        totalMatchList.sort(
                Comparator.comparing(MatchDTO::match_date)  // 1순위: match_date
                        .thenComparing(MatchDTO::match_time) // 2순위: match_time
                        .thenComparing(MatchDTO::app_name)   // 3순위: app_name
        );

        return ResponseEntity.ok(totalMatchList);
    }
}