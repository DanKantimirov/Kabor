package ru.kabor.demand.prediction.demon;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.service.DataService;

@Component
public class StorageFolderReader {

	@Value("${storage.readerPoolSize}")
	private Integer readerPoolSize;
	@Value("${storage.delayThreadTimeout}")
	private Integer delayThreadTimeout;
	@Autowired
	private DataService dataService;

	private static final Logger LOG = LoggerFactory.getLogger(StorageFolderReader.class);

	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

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
				while (true) {
					try {
						System.out.println(this.dataService.getAvailableDocument());
						System.out.println(("The time is now:" + dateFormat.format(new Date())));
						//try {Thread.sleep(this.delayThreadTimeout);	} catch (InterruptedException e) {} // nothing bad with that exception TODO: decomment
						try {Thread.sleep(10000000);	} catch (InterruptedException e) {} // nothing bad with that exception
					} catch (Exception e) {
						LOG.error("ERROR in StorageFolderReader" + e.toString());
					}
				}
			});
		}
	}
}