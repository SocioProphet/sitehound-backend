package com.hyperiongray.sitehound.backend.service.crawler.dispatcher;

import com.hyperiongray.framework.JsonMapper;
import com.hyperiongray.sitehound.backend.kafka.api.dto.crawler.SubscriberInput;
import com.hyperiongray.sitehound.backend.kafka.submitter.AquariumTaskSubmitter;
import com.hyperiongray.sitehound.backend.repository.impl.mongo.crawler.CrawlJobRepository;
import com.hyperiongray.sitehound.backend.service.crawler.BrokerService;
import com.hyperiongray.sitehound.backend.service.crawler.Constants;
import com.hyperiongray.sitehound.backend.service.crawler.excavator.ExcavatorBrokerService;
import com.hyperiongray.sitehound.backend.service.crawler.excavator.ExcavatorTaskRunnable;
import com.hyperiongray.sitehound.backend.service.crawler.searchengine.bing.BingCrawlerBrokerService;
import com.hyperiongray.sitehound.backend.service.crawler.searchengine.google.GoogleCrawlerBrokerService;
import com.hyperiongray.sitehound.backend.service.events.EventService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by tomas on 10/29/15.
 */
@Service
public class KeywordsCrawlerMessageDispatcher implements BrokerService{//KafkaListenerProcessor<SubscriberInput>{

	private static final Logger LOGGER = LoggerFactory.getLogger(KeywordsCrawlerMessageDispatcher.class);

	@Autowired private GoogleCrawlerBrokerService keywordGoogleCrawlerBrokerService;
	@Autowired private BingCrawlerBrokerService keywordBingCrawlerBrokerService;
	@Autowired private CrawlJobRepository crawlJobRepository;
	@Autowired private AquariumTaskSubmitter aquariumTaskSubmitter;
	@Autowired private EventService eventService;

	@Autowired private ExcavatorBrokerService excavatorBrokerService;

	private ExecutorService executorService = Executors.newFixedThreadPool(3);
	private JsonMapper<SubscriberInput> jsonMapper = new JsonMapper();

	@Override
	public void process(String jsonInput) {
		try{
			SubscriberInput subscriberInput = jsonMapper.toObject(jsonInput, SubscriberInput.class);

			boolean jobQueuedStarted = crawlJobRepository.updateJobStatus(subscriberInput.getJobId(), Constants.JobStatus.STARTED);
			LOGGER.info("Keywords saved job:" +  subscriberInput.getJobId());

			if(!jobQueuedStarted){
				LOGGER.info("SKIPPING PROCESS REQUEST FOR JOB:" + subscriberInput.getJobId());
				return;
			}

			for(String source : subscriberInput.getCrawlSources()){
				switch(source){
					case "SE":
						executorService.submit(new DispatcherWorker(keywordGoogleCrawlerBrokerService, subscriberInput, aquariumTaskSubmitter, Constants.CrawlType.KEYWORDS));
						executorService.submit(new DispatcherWorker(keywordBingCrawlerBrokerService, subscriberInput, aquariumTaskSubmitter, Constants.CrawlType.KEYWORDS));
						break;
					case "TOR":
//						torCrawlerBrokerService.process(subscriberInput, aquariumTaskSubmitter, Constants.CrawlType.KEYWORDS);
//						excavatorBrokerService.process(subscriberInput);
						executorService.submit(new ExcavatorTaskRunnable(excavatorBrokerService, subscriberInput));
						break;
//					case "DD":
////						Metadata metadata = MetadataBuilder.build(subscriberInput, Constants.CrawlType.KEYWORDS, Constants.CrawlEntityType.DD);
//						EventInput eventInput = new EventInput();
//						eventInput.setAction("start");
//						eventInput.setEvent("dd-trainer");
////						eventInput.setMetadata(metadata);
//						eventService.dispatch(eventInput);
//						break;
					default:
						throw new RuntimeException("UNKNOWN SOURCE");
				}
			}
		}
		catch (Exception ex){
			LOGGER.error("Service Failed", ex);
		}
	}
}
