package com.example.FYP.Api.Job;

import com.example.FYP.Api.Repository.FixtureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
@Component
@RequiredArgsConstructor
@Slf4j
public class FixtureResolveJob {

    private final FixtureRepository fixtureRepository;

    @Scheduled(fixedDelay = 10000) // every 10 seconds
    public void resolveFinishedFixtures() {
/*        List<Fixture> finishedFixtures = fixtureRepository.findAllByStatusShort("FT");
        for (Fixture fixture : finishedFixtures) {
            betResolverService.resolveBetsForFixture(fixture.getId());
        }*/
    }

}
