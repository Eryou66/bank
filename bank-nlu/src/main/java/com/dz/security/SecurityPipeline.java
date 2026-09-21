package com.dz.security;

import com.dz.context.RequestContext;
import com.dz.exception.BizException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityPipeline {

    private final List<SecurityStep> steps;

    @PostConstruct
    public void init(){
        steps.sort(Comparator.comparingInt(SecurityStep::order));
        log.info("七步安检流水线已装配：{}",
                steps.stream().map(SecurityStep::name).collect(Collectors.joining(" -> ")));
    }

    /**
     * 串行执行七步，任意一步失败就终端
     */
    public void run(SecurityContext context){
        for (SecurityStep step : steps) {
            long start = System.nanoTime();
            try{
                step.check(context);
            }catch (BizException e){
                log.warn("[安检]第{}步[{}] traceId={} code={} msg={}",
                        step.order(), step.name(),
                        RequestContext.getTraceId(), e.getErrorCode().getCode(), e.getMessage());
                throw e;
            }
            if (log.isDebugEnabled()){
                log.debug("[安检]第{}步[{}]通过， 耗时{}μs",
                        step.order(), step.name(), (System.nanoTime() - start) / 1000);
            }
        }
    }
}
