package br.com.jonyfs;

import com.amazon.sqs.javamessaging.SQSMessagingClientConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;
import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.jms.JMSException;
import javax.jms.Session;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Producer {

    @Resource
    protected JmsTemplate jmsTemplate;

    @Value("${queue.a}")
    String queueA;

    @Value("${queue.b}")
    String queueB;

    @Resource
    ObjectMapper objectMapper;

    public void sendToQueueA(MyMessage message) {
        LOGGER.info("Sending {} to queue {}", message, queueA);
        send(queueA, message);
    }

    public void sendToQueueB(String message) {
        LOGGER.info("Sending {} to queue {}", message, queueB);
        send(queueB, message);
    }

    public <MESSAGE extends Serializable> void send(String queue, MESSAGE payload) {

        jmsTemplate.send(queue, new MessageCreator() {

            public javax.jms.Message createMessage(Session session) throws JMSException {
                try {
                    javax.jms.Message createMessage = session.createTextMessage(objectMapper.writeValueAsString(payload));
                    createMessage.setStringProperty(SQSMessagingClientConstants.JMSX_GROUP_ID, "messageGroup1");
                    createMessage.setStringProperty(SQSMessagingClientConstants.JMS_SQS_DEDUPLICATION_ID, "1" + System.currentTimeMillis());
                    createMessage.setStringProperty("documentType", payload.getClass().getName());
                    return createMessage;
                } catch (Exception | Error e) {
                    LOGGER.error("Fail to send message {}", payload);
                    throw new RuntimeException(e);
                }
            }
        });

    }

    @PostConstruct
    public void sendMessages() {
        MyMessage a = MyMessage.builder().id(UUID.randomUUID().toString()).content("HELLO QUEUE A!").date(new Date()).build();
        sendToQueueA(a);
        sendToQueueB("HELLO QUEUE B!");
    }

}
