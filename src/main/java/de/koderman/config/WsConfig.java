package de.koderman.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
class WsConfig implements WebSocketMessageBrokerConfigurer {
    @Override
    public void registerStompEndpoints(@NonNull StompEndpointRegistry registry) {
        // only pure WebSocket endpoint (no SockJS)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
    @Override
    public void configureMessageBroker(@NonNull MessageBrokerRegistry registry) {
        // Enable simple broker with 5-second heartbeat: [server -> client ms, client -> server ms]
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[] {5000L, 5000L}) // 5s keepalive
                .setTaskScheduler(heartbeatTaskScheduler()); // Required for heartbeats
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Bean
    public TaskScheduler heartbeatTaskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.initialize();
        return scheduler;
    }
}