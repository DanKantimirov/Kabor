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
import ru.kabor.demand.prediction.entity.Request;
import ru.kabor.demand.prediction.service.RequestService;
import ru.kabor.demand.prediction.utils.ConstantUtils;

/** It takes new request and executes it. */
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
				Request request = null;
				while (shouldContinue) {
					try {
						request = requestService.importRawRequest();
						if (request != null) {
							LOG.info("StorageFolderReader got request. Request_id:" + request.getId());
							String resultFileName = null;
							if (request.getRequestType().equals(ConstantUtils.REQUEST_TYPE_FORECAST)) {
								resultFileName = requestService.makeRequestPrediction(request.getId());
								if (resultFileName != null) {
									LOG.info("StorageFolderReader created file:" + resultFileName);
									emailSender.sendMessageWithForecastResult(Long.valueOf(request.getId()));
								} else {
									emailSender.sendMessageWithError(Long.valueOf(request.getId()));
								}
							} else if (request.getRequestType().equals(ConstantUtils.REQUEST_TYPE_ELASTICITY)) {
								resultFileName = requestService.makeRequestElasticity(request.getId());
								if (resultFileName != null) {
									LOG.info("StorageFolderReader created file:" + resultFileName);
									emailSender.sendMessageWithElasticityResult(Long.valueOf(request.getId()));
								} else {
									emailSender.sendMessageWithError(Long.valueOf(request.getId()));
								}
							} else if (request.getRequestType().equals(ConstantUtils.REQUEST_TYPE_FORECASTANDELASTICITY)) {
								resultFileName = requestService.makeRequestAndElasticityPrediction(request.getId());
								if (resultFileName != null) {
									LOG.info("StorageFolderReader created file:" + resultFileName);
									emailSender.sendMessageWithForecastResult(Long.valueOf(request.getId()));
								} else {
									emailSender.sendMessageWithError(Long.valueOf(request.getId()));
								}
							}
							request = null;
						}
						try {
							Thread.sleep(this.delayThreadTimeout);
						} catch (InterruptedException e) {} // nothing bad with that exception
					} catch (Exception e) {
						LOG.error("StorageFolderReader error.", e);
						if (request != null) {
							try {
								emailSender.sendMessageWithError(Long.valueOf(request.getId()));
								request = null;
							} catch (MessagingException | EmailSenderException e1) {
								LOG.error("ERROR can't send email",e1);
								request = null;
							}
						}
					}
				}
				return;
			});
		}
	}
}