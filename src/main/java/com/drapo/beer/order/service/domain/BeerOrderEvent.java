package com.drapo.beer.order.service.domain;

public enum BeerOrderEvent {
    VALIDATE_ORDER,
    VALIDATION_PASSED,
    VALIDATION_FAILED,
    ALLOCATION_SUCCESS,
    ALLOCATION_NO_INVENTORY,
    ALLOCATION_FAILED,
    BEFORE_PICKED_UP
}
