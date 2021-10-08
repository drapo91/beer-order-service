package com.drapo.beer.order.service.services.listeners;

import com.drapo.beer.order.service.config.JmsConfig;
import com.drapo.beer.order.service.services.BeerOrderManager;
import com.drapo.brewery.model.events.AllocateOrderResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BeerOrderAllocationResultListener {
    private BeerOrderManager beerOrderManager;

    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listerAllocationResult(AllocateOrderResultEvent allocateResult) {
        if (!allocateResult.getAllocationError() && !allocateResult.getPendingInventory()) {
            beerOrderManager.beerOrderAllocationPassed(allocateResult.getBeerOrderDto());
        } else if (!allocateResult.getAllocationError() && allocateResult.getPendingInventory()) {
            beerOrderManager.beerOrderAllocationPendingInventory(allocateResult.getBeerOrderDto());
        } else if (allocateResult.getAllocationError()) {
            beerOrderManager.beerOrderAllocationFailed(allocateResult.getBeerOrderDto());
        }
    }
}
