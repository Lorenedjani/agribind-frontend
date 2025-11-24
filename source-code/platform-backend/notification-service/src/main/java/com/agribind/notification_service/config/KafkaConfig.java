package com.agribind.notification_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

//@Configuration
//@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true", matchIfMissing = true)
//public class KafkaConfig {
   // @Value("${app.kafka.topic}")
   // private String topicName;

   // @Bean
  //  public NewTopic notificationTopic() {
     //   return TopicBuilder.name(topicName)
      //          .partitions(3)
      //          .replicas(1)
     //           .build();
  //  }
//}