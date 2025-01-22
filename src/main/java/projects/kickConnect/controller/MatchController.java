package projects.kickConnect.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import projects.kickConnect.crawler.MatchCrawler;
import projects.kickConnect.dto.MatchDTO;

import java.util.List;

@RestController
@RequestMapping("/api/matchs")
@RequiredArgsConstructor
public class MatchController {

    private final MatchCrawler matchCrawler;

    @GetMapping
    public ResponseEntity<List<MatchDTO>> allMatch() {

        List<MatchDTO> list = matchCrawler.plab();

        return ResponseEntity.ok(list);
    }
}