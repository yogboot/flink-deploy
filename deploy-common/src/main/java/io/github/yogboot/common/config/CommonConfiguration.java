package io.github.yogboot.common.config;

import io.github.yogboot.api.properties.DeployProperties;
import io.github.yogboot.common.controller.ExceptionController;
import io.github.yogboot.common.filter.CommonKeyFilter;
import io.github.yogboot.common.response.GlobalExceptionAdvice;
import io.github.yogboot.common.response.SuccessResponseAdvice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;

@Slf4j
@RequiredArgsConstructor
@EnableConfigurationProperties(DeployProperties.class)
public class CommonConfiguration {

    private final DeployProperties deployProperties;

    private final MessageSource messageSource;

    @Bean
    @ConditionalOnClass(CommonConfiguration.class)
    private CommonKeyFilter initCommonFilter() {
        return new CommonKeyFilter(deployProperties);
    }

    @Bean
    @ConditionalOnClass(CommonConfiguration.class)
    private ExceptionController initExceptionController() {
        return new ExceptionController();
    }

    @Bean
    @ConditionalOnClass(CommonConfiguration.class)
    private GlobalExceptionAdvice initGlobalExceptionAdvice() {
        return new GlobalExceptionAdvice();
    }

    @Bean
    @ConditionalOnClass(CommonConfiguration.class)
    private SuccessResponseAdvice initSuccessResponseAdvice() {
        return new SuccessResponseAdvice(messageSource);
    }

}
