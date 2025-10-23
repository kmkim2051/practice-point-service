package io.hhplus.tdd.point;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        log.info("GET /point/{} - 포인트 조회", id);
        return pointService.getUserPoint(id);
    }

    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        log.info("GET /point/{}/histories - 포인트 이력 조회", id);
        return pointService.getUserPointHistories(id);
    }

    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("PATCH /point/{}/charge - 포인트 충전, amount: {}", id, amount);
        return pointService.chargeUserPoint(id, amount);
    }

    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        log.info("PATCH /point/{}/use - 포인트 사용, amount: {}", id, amount);
        return pointService.useUserPoint(id, amount);
    }
}
