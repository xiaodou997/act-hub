package com.xiaodou.config.rabbitmq;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据埋点相关的 RabbitMQ 配置
 */
@Configuration
public class TrackingRabbitMQConfig {

    public static final String EXCHANGE_NAME = "tracking.exchange";
    public static final String QUEUE_NAME = "tracking.event.queue";
    public static final String ROUTING_KEY = "tracking.event.record";

    @Bean
    public TopicExchange trackingExchange() {
        return new TopicExchange(EXCHANGE_NAME, true, false);
    }

    @Bean
    public Queue trackingQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    @Bean
    public Binding trackingBinding() {
        return BindingBuilder.bind(trackingQueue()).to(trackingExchange()).with(ROUTING_KEY);
    }
}
