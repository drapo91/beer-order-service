package com.drapo.beer.order.service.services.listeners;

import com.drapo.beer.order.service.config.JmsConfig;
import com.drapo.beer.order.service.services.BeerOrderManager;
import com.drapo.brewery.model.events.ValidateOrderResultEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class ValidationResultListener {

    private final BeerOrderManager beerOrderManager;


    @JmsListener(destination = JmsConfig.VALIDATE_ORDER_RESPONSE_QUEUE)
    public void listen(ValidateOrderResultEvent resultEvent) {

        final UUID beerOrderId=resultEvent.getOrderId();

        log.debug("Validation Result for Order id: "+beerOrderId);

        beerOrderManager.processValidationResult(beerOrderId, resultEvent.getIsValid());
    }
}
