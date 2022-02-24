package com.otakuhuang.springhateoasclientdemo;

import com.otakuhuang.springhateoasclientdemo.model.Coffee;
import com.otakuhuang.springhateoasclientdemo.model.CoffeeOrder;
import com.otakuhuang.springhateoasclientdemo.model.OrderState;
import lombok.extern.log4j.Log4j2;
import org.joda.money.CurrencyUnit;
import org.joda.money.Money;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;

/**
 * @author otaku
 * @version 1.0
 * @date 2022/2/24 23:01
 * @description description
 */
@Component
@Log4j2
public class ClientRunner implements ApplicationRunner {
    private static final URI ROOT_URI = URI.create("http://localhost:8080/");

    @Autowired
    private RestTemplate restTemplate;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Optional<Link> coffeeLink = getLink(ROOT_URI, "coffees");

        if (coffeeLink.isPresent()) {
            readCoffeeMenu(coffeeLink.get());
            EntityModel<Coffee> americano = addCoffee(coffeeLink.get());

            Optional<Link> orderLink = getLink(ROOT_URI, "coffeeOrders");
            if (orderLink.isPresent()) {
                addOrder(orderLink.get(), americano);
                queryOrder(orderLink.get());
            }
        }
    }

    private Optional<Link> getLink(URI uri, String rel) {
        ResponseEntity<EntityModel<Optional<Link>>> rootResp =
                restTemplate.exchange(
                        uri,
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<EntityModel<Optional<Link>>>() {
                        });
        Optional<Link> link = rootResp.getBody().getLink(rel);
        log.info("Link: {}", link);
        return link;
    }

    private void readCoffeeMenu(Link coffeeLink) {
        ResponseEntity<PagedModel<EntityModel<Coffee>>> coffeeResp =
                restTemplate.exchange(coffeeLink.getTemplate().expand(),
                        HttpMethod.GET,
                        null,
                        new ParameterizedTypeReference<PagedModel<EntityModel<Coffee>>>() {
                        });
        log.info("Menu: Response: {}", coffeeResp.getBody());
    }

    private EntityModel<Coffee> addCoffee(Link link) {
        Coffee americano = Coffee.builder()
                .name("americano")
                .price(Money.of(CurrencyUnit.of("CNY"), 25.0))
                .build();
        RequestEntity<Coffee> req =
                RequestEntity.post(link.getTemplate().expand()).body(americano);
        ResponseEntity<EntityModel<Coffee>> resp =
                restTemplate.exchange(req,
                        new ParameterizedTypeReference<EntityModel<Coffee>>() {
                        });
        log.info("Add Coffee Response: {}", resp);
        return resp.getBody();
    }

    private void addOrder(Link link, EntityModel<Coffee> coffee) {
        CoffeeOrder newOrder = CoffeeOrder.builder()
                .customer("Li Lei")
                .state(OrderState.INIT)
                .build();
        RequestEntity<?> req =
                RequestEntity.post(link.getTemplate().expand()).body(newOrder);
        ResponseEntity<EntityModel<CoffeeOrder>> resp =
                restTemplate.exchange(req,
                        new ParameterizedTypeReference<EntityModel<CoffeeOrder>>() {
                        });
        log.info("Add Order Response: {}", resp);

        EntityModel<CoffeeOrder> order = resp.getBody();
        Optional<Link> items = order.getLink("items");

        if (items.isPresent()) {
            req = RequestEntity.post(
                            items.get().getTemplate().expand())
                    .body(Collections.singletonMap("_links", coffee.getLink("self")));
            ResponseEntity<String> itemResp = restTemplate.exchange(req, String.class);
            log.info("Add Order Items Response: {}", itemResp);
        }
    }

    private void queryOrder(Link link) {
        ResponseEntity<String> resp =
                restTemplate.getForEntity(link.getTemplate().expand(), String.class);
        log.info("Query Order Response: {}", resp);
    }
}
