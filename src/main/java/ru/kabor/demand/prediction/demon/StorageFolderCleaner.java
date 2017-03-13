package ru.kabor.demand.prediction.demon;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import ru.kabor.demand.prediction.service.DataService;

@Component
public class StorageFolderCleaner {

	@Autowired
	private DataService dataService;
	
	@Value("${storage.shouldMakeCleaning}")
	private Boolean shouldMakeCleaning;

	private static final Logger LOG = LoggerFactory.getLogger(StorageFolderCleaner.class);

	@Scheduled(cron="0 0 0 * * ?")
	public void doSomething() throws ParseException {
		if(shouldMakeCleaning){
    		Long dayInMs = 1000 * 60 * 60 * 24L;
    		Date date = new Date(System.currentTimeMillis() - (3 * dayInMs));
    		List<String> attachmentPath = dataService.getAttachmentPathListByResponseTimeBeforeMoment(date);
    		for (String fileName : attachmentPath) {
    			dataService.deleteFileStorageOutput(fileName);
    		}
    		LOG.debug("attachmentPath:" + attachmentPath.toString());
    		List<String> documentPath = dataService.getDocumentPathListByResponseTimeBeforeMoment(date);
    		for (String fileName : documentPath) {
    			dataService.deleteFileStorageInput(fileName);
    		}
    		LOG.debug("documentPath:" + documentPath.toString());
    		Integer deletedRequests = dataService.deleteRequestByResponseTimeBeforeMoment(date);
    		LOG.debug("deletedRequests:" + deletedRequests);
		}
	}
}
