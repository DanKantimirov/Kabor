package ru.kabor.demand.prediction.demon;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.PostConstruct;
import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.email.EmailSender;
import ru.kabor.demand.prediction.email.EmailSenderException;
import ru.kabor.demand.prediction.service.RequestService;

@Component
public class StorageFolderReader {

	@Value("${storage.readerPoolSize}")
	private Integer readerPoolSize;
	@Value("${storage.delayThreadTimeout}")
	private Integer delayThreadTimeout;

	@Autowired
	RequestService requestService;
	
	@Autowired
	EmailSender emailSender;

	private static final Logger LOG = LoggerFactory.getLogger(StorageFolderReader.class);

	@PostConstruct
	private void getCurrentTime() {
		LOG.debug("demon started");
		ExecutorService es = Executors.newFixedThreadPool(this.readerPoolSize);
		Integer startupThreadDelay = 0;
		if (this.delayThreadTimeout > 0) {
			startupThreadDelay = this.delayThreadTimeout / this.readerPoolSize;
		}
		for (int i = 0; i < this.readerPoolSize; i++) {
			try {
				Thread.sleep(startupThreadDelay);
			} catch (InterruptedException e) {} // nothing bad with that exception
			es.submit(() -> {
				Boolean shouldContinue = true;
				Integer requestId = null;
				while (shouldContinue) {
					try {
						LOG.debug("demon awoke");
						requestId = requestService.importRawRequest();
						if(requestId!=null){
							String resultFileName = requestService.makeRequestPrediction(requestId);
							if (resultFileName != null) {
								System.out.println(resultFileName);
								emailSender.sendMessageWithResult(Long.valueOf(requestId));
							} else{
								emailSender.sendMessageWithError(Long.valueOf(requestId));
							}
						}
						try {Thread.sleep(this.delayThreadTimeout);	} catch (InterruptedException e) {} // nothing bad with that exception
						LOG.debug("demon sleep");
					} catch (Exception e) {
						LOG.error("ERROR in StorageFolderReader: " + e.toString());
						if (requestId != null) {
							try {
								emailSender.sendMessageWithError(Long.valueOf(requestId));
							} catch (MessagingException | EmailSenderException e1) {
								LOG.error("ERROR can't send email: " + e1.toString());
							}
						}
					}
				}
			return;});
		}
	}
}