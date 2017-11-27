package br.com.jonyfs;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class Consumer {

    @JmsListener(destination = "${queue.a}")
    public void processMessageA(@Payload final Message<MyMessage> message) {
        LOGGER.info("Processing {} in queue a", message.getPayload());
    }

    @JmsListener(destination = "${queue.b}")
    public void processMessageB(@Payload String message) {
        LOGGER.info("Processing {}  in queue b", message);
    }

}
