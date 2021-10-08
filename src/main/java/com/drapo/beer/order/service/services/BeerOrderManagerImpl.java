package com.drapo.beer.order.service.services;

import com.drapo.beer.order.service.domain.BeerOrder;
import com.drapo.beer.order.service.domain.BeerOrderEvent;
import com.drapo.beer.order.service.domain.BeerOrderStatus;
import com.drapo.beer.order.service.repositories.BeerOrderRepository;
import com.drapo.beer.order.service.sm.BeerOrderStateChangeInterceptor;
import com.drapo.brewery.model.BeerOrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.config.StateMachineFactory;
import org.springframework.statemachine.support.DefaultStateMachineContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BeerOrderManagerImpl implements BeerOrderManager {

    public static final String ORDER_ID_HEADER = "ORDER_ID_HEADER";
    private final StateMachineFactory<BeerOrderStatus, BeerOrderEvent> stateMachineFactory;
    private final BeerOrderRepository beerOrderRepository;
    private final BeerOrderStateChangeInterceptor interceptor;

    @Override
    @Transactional
    public BeerOrder newBeerOrder(BeerOrder beerOrder) {
        beerOrder.setId(null);
        beerOrder.setBeerOrderStatus(BeerOrderStatus.NEW);

        BeerOrder savedBeerOrder = beerOrderRepository.save(beerOrder);

        sendBeerOrderEvent(savedBeerOrder, BeerOrderEvent.VALIDATE_ORDER);

        return savedBeerOrder;
    }

    @Override
    public void processValidationResult(UUID beerOrderId, Boolean isValid) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderId);

        if(isValid){
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_PASSED);

            BeerOrder validatedBeerOrder = beerOrderRepository.getOne(beerOrderId);

            sendBeerOrderEvent(validatedBeerOrder, BeerOrderEvent.ALLOCATE_ORDER);
        }else{
            sendBeerOrderEvent(beerOrder, BeerOrderEvent.VALIDATION_FAILED);
        }
    }

    @Override
    public void beerOrderAllocationPassed(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_SUCCESS);
        updateAllocatedQty(beerOrderDto, beerOrder);
    }

    @Override
    public void beerOrderAllocationPendingInventory(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_NO_INVENTORY);
        updateAllocatedQty(beerOrderDto, beerOrder);
    }

    private void updateAllocatedQty(BeerOrderDto beerOrderDto, BeerOrder beerOrder){
        BeerOrder allocatedOrder = beerOrderRepository.getOne(beerOrderDto.getId());

        allocatedOrder.getBeerOrderLines().forEach(beerOrderLine -> {
            beerOrderDto.getBeerOrderLines().forEach(beerOrderLineDto -> {
                if(beerOrderLine.getId().equals(beerOrderLineDto.getId())){
                    beerOrderLine.setQuantityAllocated(beerOrderLineDto.getQuantityAllocated());
                }
            });
        });

        beerOrderRepository.saveAndFlush(beerOrder);
    }

    @Override
    public void beerOrderAllocationFailed(BeerOrderDto beerOrderDto) {
        BeerOrder beerOrder = beerOrderRepository.getOne(beerOrderDto.getId());
        sendBeerOrderEvent(beerOrder, BeerOrderEvent.ALLOCATION_FAILED);
    }

    private void sendBeerOrderEvent(BeerOrder beerOrder, BeerOrderEvent event) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine = build(beerOrder);

        Message msg = MessageBuilder.withPayload(event)
                .setHeader(ORDER_ID_HEADER, beerOrder.getId().toString())
                .build();

        stateMachine.sendEvent(msg);
    }

    private StateMachine<BeerOrderStatus, BeerOrderEvent> build(BeerOrder beerOrder) {
        StateMachine<BeerOrderStatus, BeerOrderEvent> stateMachine = stateMachineFactory.getStateMachine(beerOrder.getId());

        stateMachine.stop();

        stateMachine.getStateMachineAccessor()
                .doWithAllRegions(sma -> {
                    sma.addStateMachineInterceptor(interceptor);
                    sma.resetStateMachine(new DefaultStateMachineContext<>(beerOrder.getBeerOrderStatus(), null, null, null));
                });

        return stateMachine;
    }
}
