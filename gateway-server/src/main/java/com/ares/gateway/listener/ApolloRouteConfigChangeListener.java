package com.ares.gateway.listener;

import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.BindResult;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.cloud.gateway.config.GatewayProperties;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 基于apollo网关配置动态加载
 */
@Component
public class ApolloRouteConfigChangeListener {

    private static final String ROUTE_PREFIX = "spring.cloud.gateway.routes";
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final Environment environment;
    private final GatewayProperties gatewayProperties;
    private final ApplicationEventPublisher applicationEventPublisher;

    public ApolloRouteConfigChangeListener(Environment environment, GatewayProperties gatewayProperties, ApplicationEventPublisher applicationEventPublisher) {
        this.environment = environment;
        this.gatewayProperties = gatewayProperties;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @ApolloConfigChangeListener
    public void onChangeEvent(ConfigChangeEvent event) {
        BindResult<List<RouteDefinition>> bindResult = Binder.get(environment).bind(ROUTE_PREFIX, Bindable.listOf(RouteDefinition.class));
        if (bindResult.isBound()) {
            List<RouteDefinition> definitions = bindResult.get();
            logger.info("update-routes newRouteSize: {}, newRouteConfig: {}", definitions.size(), definitions);
            gatewayProperties.setRoutes(definitions);
            applicationEventPublisher.publishEvent(new RefreshRoutesEvent(this));
        }
    }
}
