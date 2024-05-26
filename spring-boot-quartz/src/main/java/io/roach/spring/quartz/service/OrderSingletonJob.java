package io.roach.spring.quartz.service;

import org.quartz.DisallowConcurrentExecution;
import org.springframework.stereotype.Component;

@DisallowConcurrentExecution
@Component
public class OrderSingletonJob extends OrderJob {
}
