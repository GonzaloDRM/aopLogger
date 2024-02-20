package com.aop.logger.logging;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
public class CoreLoggerTest {

    private CoreLogger coreLogger;
    private ProceedingJoinPoint joinPoint;
    private MethodSignature signature;
    @Mock
    private MockHttpServletRequest request;

    @BeforeEach
    public void setup(){
        request.addHeader("headerName","asd");
        request.setServletPath("/ect/logger/test");
        coreLogger = new CoreLogger(request);
        joinPoint = mock(ProceedingJoinPoint.class);
        signature = mock(MethodSignature.class);
    }

    @Test
    @DisplayName("test for logger")
    void loggerTest() throws Throwable {
        Object[] arguments = {"argumento"};
        String[] parametersName = {"argumentName"};
        given(signature.getParameterNames()).willReturn(parametersName);
        given(signature.toShortString()).willReturn("methodTest");
        given(joinPoint.getSignature()).willReturn(signature);
        given(joinPoint.getArgs()).willReturn(arguments);
        given(joinPoint.proceed()).willReturn("Result");

        Object result = coreLogger.logAround(joinPoint);

        assertNotNull(result);
    }




}
