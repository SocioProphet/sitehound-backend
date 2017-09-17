package com.hyperiongray.sitehound.backend.service.dd.trainer.output;

import com.hyperiongray.sitehound.backend.kafka.api.dto.Metadata;
import com.hyperiongray.sitehound.backend.kafka.api.dto.aquarium.AquariumInput;
import com.hyperiongray.sitehound.backend.kafka.api.dto.dd.PageSample;
import com.hyperiongray.sitehound.backend.kafka.api.dto.dd.trainer.output.DdTrainerOutputPages;
import com.hyperiongray.sitehound.backend.service.JsonMapper;
import com.hyperiongray.sitehound.backend.service.aquarium.AquariumAsyncClient;
import com.hyperiongray.sitehound.backend.service.aquarium.AquariumBrokerService;
import com.hyperiongray.sitehound.backend.service.aquarium.callback.service.impl.DdTrainerOutputPagesAquariumCallbackService;
import com.hyperiongray.sitehound.backend.service.aquarium.callback.service.impl.KeywordsAquariumCallbackService;
import com.hyperiongray.sitehound.backend.service.aquarium.callback.wrapper.DdTrainerOutputPagesCallbackServiceWrapper;
import com.hyperiongray.sitehound.backend.service.crawler.BrokerService;
import com.hyperiongray.sitehound.backend.service.crawler.searchengine.MetadataBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by tomas on 28/09/16.
 */
@Service
public class DdTrainerOutputPagesBrokerService implements BrokerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(DdTrainerOutputPagesBrokerService.class);

//    @Autowired private AquariumProducer producer;
    @Autowired private MetadataBuilder metadataBuilder;
    @Autowired private AquariumAsyncClient aquariumClient;

//    @Autowired private KeywordsAquariumCallbackService keywordsAquariumCallbackService;
    @Autowired private DdTrainerOutputPagesAquariumCallbackService ddTrainerOutputPagesAquariumCallbackService;

    @Override
    public void process(String jsonInput){

        try{
            LOGGER.debug("Receiving response: " + jsonInput);
            JsonMapper<DdTrainerOutputPages> jsonMapper= new JsonMapper();
            DdTrainerOutputPages ddTrainerOutputPages = jsonMapper.toObject(jsonInput, DdTrainerOutputPages.class);

            Metadata metadata = metadataBuilder.buildFromTrainerOutputPages(ddTrainerOutputPages.getWorkspaceId());

            for (PageSample pageSample : ddTrainerOutputPages.getPageSamples()){
                DdTrainerOutputPagesCallbackServiceWrapper callbackServiceWrapper = new DdTrainerOutputPagesCallbackServiceWrapper(pageSample, metadata, ddTrainerOutputPagesAquariumCallbackService);
//                AquariumInput aquariumInput = new AquariumInput(metadata);
//                aquariumInput.setUrl(pageSample.getUrl());
//                aquariumInput.setIndex(100);
//                DefaultCallbackServiceWrapper callbackServiceWrapper = new DefaultCallbackServiceWrapper(aquariumInput, keywordsAquariumCallbackService);
//                producer.submit(aquariumInput);
//                aquariumBrokerService.process(aquariumInput);
                aquariumClient.fetch(pageSample.getUrl(), callbackServiceWrapper);
            }
        }
        catch(Exception e){
            LOGGER.error("ERROR:" + jsonInput, e);
        }
    }
}
