package projects.kickConnect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import projects.kickConnect.crawler.MatchCrawler;
import projects.kickConnect.dto.MatchDTO;

import java.util.List;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchCrawler matchCrawler;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> allMatch(
            @RequestParam("matchDate") String sch,
            @RequestParam("region") String region,
            @RequestParam("gender") String gender,
            @RequestParam("soldout") String soldout
    ) {

        List<MatchDTO> list = matchCrawler.plab(sch, region, gender, soldout);

        return ResponseEntity.ok(list);
    }
}