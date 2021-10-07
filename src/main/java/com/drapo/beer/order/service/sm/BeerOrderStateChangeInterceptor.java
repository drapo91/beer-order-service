package com.drapo.beer.order.service.sm;

import com.drapo.beer.order.service.domain.BeerOrder;
import com.drapo.beer.order.service.domain.BeerOrderEvent;
import com.drapo.beer.order.service.domain.BeerOrderStatus;
import com.drapo.beer.order.service.repositories.BeerOrderRepository;
import com.drapo.beer.order.service.services.BeerOrderManagerImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptorAdapter;
import org.springframework.statemachine.transition.Transition;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class BeerOrderStateChangeInterceptor extends StateMachineInterceptorAdapter<BeerOrderStatus, BeerOrderEvent> {
    private final BeerOrderRepository beerOrderRepository;

    @Override
    public void preStateChange(State<BeerOrderStatus, BeerOrderEvent> state, Message<BeerOrderEvent> message, Transition<BeerOrderStatus, BeerOrderEvent> transition, StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine) {
        Optional.ofNullable(message)
                .flatMap(msg -> Optional.ofNullable((String) msg.getHeaders().getOrDefault(BeerOrderManagerImpl.ORDER_ID_HEADER, "")))
                .ifPresent(orderId -> {
                    log.debug(String.format("Saving state for order id: %s. Status: %s", orderId, state.getId()));

                    BeerOrder beerOrder = beerOrderRepository.getOne(UUID.fromString(orderId));
                    beerOrder.setBeerOrderStatus(state.getId());

                    beerOrderRepository.saveAndFlush(beerOrder);
                });
    }
}
