package com.example.auction;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.scheduler.Scheduled;


@ApplicationScoped
public class EnchereScheduler {

    @Inject
    private EnchereService es;
    @Inject
    private EnchereManager em;
    @Scheduled(every="5m")
    public void ensureMinimumEnchere(){
        int activeEnchere= em.countActiveAuctions();

        while (activeEnchere<5){
            es.createAuctionAutomatically();
            activeEnchere++;
        }

    }

    @Scheduled(every="30m")
    public void closeExpiredEnchere(){
        es.closeExpiredEnchere();
    }
}
