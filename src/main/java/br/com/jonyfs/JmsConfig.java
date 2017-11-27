package br.com.jonyfs;

import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import javax.annotation.PostConstruct;
import javax.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.support.converter.MappingJackson2MessageConverter;
import org.springframework.jms.support.converter.MessageConverter;
import org.springframework.jms.support.converter.MessageType;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

@Slf4j
@Configuration
@EnableJms
public class JmsConfig {

    @PostConstruct
    public void init() {
        LOGGER.info("Started.");
    }

    private final SQSConnectionFactory connectionFactory;

    public JmsConfig(
        @Value("${cloud.aws.credentials.accessKey}") String awsAccessKey,
        @Value("${cloud.aws.credentials.secretKey}") String awsSecretKey,
        @Value("${cloud.aws.region.static}") String awsRegion) {
        connectionFactory = SQSConnectionFactory.builder()
            .withRegion(Region.getRegion(Regions.fromName(awsRegion)))
            .withAWSCredentialsProvider(new StaticCredentialsProvider(
                new BasicAWSCredentials(awsAccessKey, awsSecretKey)
            ))
            .build();
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory
            = new DefaultJmsListenerContainerFactory();
        factory.setConnectionFactory(this.connectionFactory);
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("3-10");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);
        factory.setMessageConverter(messageConverter());
        return factory;
    }

    @Bean
    public JmsTemplate jmsTemplate() {
        JmsTemplate jmsTemplate = new JmsTemplate(this.connectionFactory);
        jmsTemplate.setMessageConverter(messageConverter());
        return jmsTemplate;
    }

    @Bean
    public MessageConverter messageConverter() {
        Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
        builder.serializationInclusion(JsonInclude.Include.NON_EMPTY);
        builder.dateFormat(new ISO8601DateFormat());

        org.springframework.jms.support.converter.MappingJackson2MessageConverter mappingJackson2MessageConverter = new MappingJackson2MessageConverter();

        mappingJackson2MessageConverter.setObjectMapper(builder.build());
        mappingJackson2MessageConverter.setTargetType(MessageType.TEXT);
        mappingJackson2MessageConverter.setTypeIdPropertyName("documentType");
        return mappingJackson2MessageConverter;
    }
}
