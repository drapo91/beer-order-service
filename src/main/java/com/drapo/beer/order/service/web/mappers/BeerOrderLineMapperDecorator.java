package com.drapo.beer.order.service.web.mappers;


import com.drapo.beer.order.service.domain.BeerOrderLine;
import com.drapo.beer.order.service.services.beer.BeerService;
import com.drapo.brewery.model.BeerDto;
import com.drapo.brewery.model.BeerOrderLineDto;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

public class BeerOrderLineMapperDecorator implements BeerOrderLineMapper {
    private BeerOrderLineMapper beerOrderLineMapper;
    private BeerService beerService;

    @Autowired
    public void setBeerOrderLineMapper(BeerOrderLineMapper beerOrderLineMapper) {
        this.beerOrderLineMapper = beerOrderLineMapper;
    }

    @Autowired
    public void setBeerService(BeerService beerService) {
        this.beerService = beerService;
    }

    @Override
    public BeerOrderLineDto beerOrderLineToDto(BeerOrderLine line) {
        BeerOrderLineDto beerOrderLineDto = beerOrderLineMapper.beerOrderLineToDto(line);

        Optional<BeerDto> beerDto = beerService.getBeerByUpc(beerOrderLineDto.getUpc());

        beerDto.ifPresent(dto -> {
            beerOrderLineDto.setBeerName(dto.getBeerName());
            beerOrderLineDto.setBeerStyle(dto.getBeerStyle());
            beerOrderLineDto.setPrice(dto.getPrice());
            beerOrderLineDto.setBeerId(dto.getId());
        });

        return beerOrderLineDto;
    }

    @Override
    public BeerOrderLine dtoToBeerOrderLine(BeerOrderLineDto dto) {
        return beerOrderLineMapper.dtoToBeerOrderLine(dto);
    }
}
