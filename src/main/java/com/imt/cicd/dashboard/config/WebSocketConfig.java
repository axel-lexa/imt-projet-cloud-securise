package com.imt.cicd.dashboard.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Active un broker mémoire simple pour les clients abonnés aux topics commençant par "/topic"
        config.enableSimpleBroker("/topic");
        // Préfixe pour les messages envoyés du client vers le serveur (pas utilisé ici, mais bonne pratique)
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Point d'entrée pour que le frontend React se connecte
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // Important pour autoriser React (localhost:3000) à se connecter
                .withSockJS(); // Active le fallback SockJS si les WebSockets natifs ne passent pas
    }
}
