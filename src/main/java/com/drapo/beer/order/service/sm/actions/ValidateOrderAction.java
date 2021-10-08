package com.drapo.beer.order.service.sm.actions;

import com.drapo.beer.order.service.config.JmsConfig;
import com.drapo.beer.order.service.domain.BeerOrder;
import com.drapo.beer.order.service.domain.BeerOrderEvent;
import com.drapo.beer.order.service.domain.BeerOrderStatus;
import com.drapo.beer.order.service.repositories.BeerOrderRepository;
import com.drapo.beer.order.service.services.BeerOrderManagerImpl;
import com.drapo.beer.order.service.web.mappers.BeerOrderMapper;
import com.drapo.brewery.model.events.ValidateOrderRequestEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.action.Action;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ValidateOrderAction implements Action<BeerOrderStatus, BeerOrderEvent> {
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderMapper beerOrderMapper;
    private final JmsTemplate jmsTemplate;

    @Override
    public void execute(StateContext<BeerOrderStatus, BeerOrderEvent> stateContext) {
        String beerOrderId = (String) stateContext.getMessage().getHeaders().get(BeerOrderManagerImpl.ORDER_ID_HEADER);
        BeerOrder beerOrder = beerOrderRepository.findOneById(UUID.fromString(beerOrderId));

        jmsTemplate.convertAndSend(JmsConfig.VALIDATE_ORDER_QUEUE, ValidateOrderRequestEvent.builder()
                .beerOrder(beerOrderMapper.beerOrderToDto(beerOrder))
                .build());

        log.debug("Send validation request to queue for order id: " + beerOrderId);
    }
}
