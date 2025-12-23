package com.example.AzureTestProject.Api.Loader;

import com.example.AzureTestProject.Api.Entity.Feature;
import com.example.AzureTestProject.Api.Repository.FeatureRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class FeatureLoader {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private FeatureRepository featureRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Order(1)
    public void loadFeatures() {
        String[] beans = context.getBeanNamesForAnnotation(com.example.AzureTestProject.Api.Loader.Annotation.Feature.class);
        Set<Feature> featuresToSave = new HashSet<>();
        Set<String> patterns = new HashSet<>();

        for (String beanName : beans) {
            Object bean = context.getBean(beanName);
            Class<?> clazz = AopUtils.getTargetClass(bean);

            RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
            if (classMapping != null && classMapping.value().length > 0) {
                patterns.add(classMapping.value()[0]);
            }
        }

        List<Feature> existingFeatures = featureRepository.findByPatternIn(List.copyOf(patterns));
        Set<String> existingPatterns = existingFeatures.stream()
                .map(Feature::getPattern)
                .collect(Collectors.toSet());

        for (String beanName : beans) {
            Object bean = context.getBean(beanName);
            Class<?> clazz = AopUtils.getTargetClass(bean);
            String featureName = clazz.getSimpleName();

            RequestMapping classMapping = clazz.getAnnotation(RequestMapping.class);
            if (classMapping != null && classMapping.value().length > 0) {
                String baseUri = classMapping.value()[0];
                if (!existingPatterns.contains(baseUri)) {
                    Feature feature = new Feature();
                    feature.setName(featureName);
                    feature.setPattern(baseUri);
                    featuresToSave.add(feature);
                }
            }
        }

        if (!featuresToSave.isEmpty()) {
            featureRepository.saveAll(featuresToSave);
        }

        log.info("Loaded " + featuresToSave.size() + " new class-level features.");
    }

}
