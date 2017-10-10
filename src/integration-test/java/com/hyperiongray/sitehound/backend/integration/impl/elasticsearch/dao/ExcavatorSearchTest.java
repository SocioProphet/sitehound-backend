package com.hyperiongray.sitehound.backend.integration.impl.elasticsearch.dao;

import com.google.common.collect.Sets;
import com.hyperiongray.sitehound.backend.TestConfiguration;
import com.hyperiongray.sitehound.backend.service.crawler.excavator.ExcavatorSearchService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.Set;

import static junit.framework.TestCase.fail;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestConfiguration.class)
@ActiveProfiles("integration-test")
@TestPropertySource(locations = {"file:config/properties-override/excavator.properties"})
public class ExcavatorSearchTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExcavatorSearchTest.class);

    @Autowired private ExcavatorSearchService excavatorSearchService;

    @Test
    public void test(){
//        Set<String> keywords = Sets.newHashSet("bitcoin", "etherum");
        Set<String> keywords = Sets.newHashSet("bitcoin");
        int startingFrom = 0;
        int pageSize = 50;

        try {
            excavatorSearchService.search(keywords, startingFrom, pageSize);
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }

    }
}
