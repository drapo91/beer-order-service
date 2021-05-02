package guru.sfg.beer.order.service.services.beer;

import guru.sfg.beer.order.service.web.model.BeerDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@ConfigurationProperties(value = "drapo.beerservice", ignoreUnknownFields = false)
public class BeerServiceRestTemplateImpl implements BeerService {

    private final String BEER_ID_SERVICE_PATH = "/api/v1/beer/";
    private final String BEER_UPC_SERVICE_PATH = "/api/v1/beerUpc/";
    private String beerServiceHost;
    private RestTemplate restTemplate;

    public BeerServiceRestTemplateImpl(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    public void setBeerServiceHost(String beerServiceHost) {
        this.beerServiceHost = beerServiceHost;
    }

    @Override
    public Optional<BeerDto> getBeerById(UUID beerId) {
        log.debug("Calling Beer Service...");

        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + BEER_ID_SERVICE_PATH + beerId, BeerDto.class));
    }

    @Override
    public Optional<BeerDto> getBeerByUpc(String beerUpc) {
        log.debug("Calling Beer Service...");

        return Optional.ofNullable(restTemplate.getForObject(beerServiceHost + BEER_UPC_SERVICE_PATH + beerUpc, BeerDto.class));
    }
}
