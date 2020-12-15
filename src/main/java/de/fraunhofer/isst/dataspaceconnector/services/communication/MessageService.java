package de.fraunhofer.isst.dataspaceconnector.services.communication;

import de.fraunhofer.iais.eis.Message;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MessageBuilderException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MessageException;
import de.fraunhofer.isst.dataspaceconnector.exceptions.MessageNotSentException;
import de.fraunhofer.isst.ids.framework.messages.InfomodelMessageBuilder;
import de.fraunhofer.isst.ids.framework.spring.starter.IDSHttpService;
import java.net.URI;
import okhttp3.MultipartBody;
import okhttp3.Response;

import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Abstract class for building and sending ids messages.
 */
@Service
public abstract class MessageService {

    public static final Logger LOGGER = LoggerFactory.getLogger(MessageService.class);

    private final IDSHttpService idsHttpService;

    @Autowired
    public MessageService(IDSHttpService idsHttpService) {
        if (idsHttpService == null)
            throw new IllegalArgumentException("The IDSHttpService cannot be null.");

        this.idsHttpService = idsHttpService;
    }

    public abstract Message buildHeader() throws MessageException;

    public abstract URI getRecipient();

    public Response sendMessage(MessageService service, String payload) throws MessageException {
        Message message;
        try {
            message = service.buildHeader();
        } catch (MessageBuilderException e) {
            LOGGER.info("Message could not be built. " + e.getMessage());
            return null;
        }

        try {
            MultipartBody body = InfomodelMessageBuilder.messageWithString(message, payload);
            return idsHttpService.send(body, service.getRecipient());
        } catch (MessageNotSentException | IOException e) {
            LOGGER.info("Message could not be sent. " + e.getMessage());
            return null;
        }
    }
}
