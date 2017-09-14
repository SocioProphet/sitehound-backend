package com.hyperiongray.sitehound.backend.test.kafka.modeler;

import com.hyperiongray.sitehound.backend.kafka.api.dto.dd.modeler.output.DdModelerProgress;
import com.hyperiongray.sitehound.backend.repository.impl.mongo.dd.DdModelerProgressRepository;
import com.hyperiongray.sitehound.backend.service.dd.modeler.output.DdModelerProgressBrokerService;
import com.hyperiongray.sitehound.backend.test.kafka.KafkaTestConfiguration;
import com.hyperiongray.sitehound.backend.test.kafka.SyncProducer;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.rule.KafkaEmbedded;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;

import static org.mockito.Mockito.verify;

/**
 * Created by tomas on 14/06/17.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {KafkaTestConfiguration.class})
@SpringBootTest
public class ModelerProgressKafkaTemplateTests {

    private static final String TEMPLATE_TOPIC = "dd-modeler-output-progress";

    @ClassRule
    public static KafkaEmbedded embeddedKafka = new KafkaEmbedded(1, true, TEMPLATE_TOPIC);

    @MockBean
    private DdModelerProgressRepository ddModelerProgressRepositoryMock;

    @Autowired
    private SyncProducer producer;

    @Autowired
    private DdModelerProgressBrokerService brokerService;

    @Test
    public void testTemplate() throws IOException {

        String id = "59b114a2e4dc96629bb2b2fb";
        Double percentageDone = 98.123;

        String input = "{" +
                "    \"id\":\"" + id + "\"," +
                "    \"percentage_done\":" + percentageDone + " " +
                "}";
        producer.produce(TEMPLATE_TOPIC, embeddedKafka, brokerService, input);


        // expecting repo to be called once with correct param
        DdModelerProgress ddModelerProgress = new DdModelerProgress();
        ddModelerProgress.setId(id);
        ddModelerProgress.setPercentageDone(percentageDone);

        verify(ddModelerProgressRepositoryMock).saveProgress(ddModelerProgress);

    }

}