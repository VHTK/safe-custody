package com.bourne.aspect;

import com.bourne.util.JsonUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import java.lang.reflect.Method;

/**
 * @Author: vhtk
 * @Description:
 * @Date: 2020/6/22
 */
@Component
@Aspect
@Profile("default")
public class WriteLogHandler {

    @Pointcut(value = "@annotation(com.bourne.aspect.WriteLog) || execution(* com.bourne..*Controller.*(..))")
    public void pointCut() {

    }

    /**
     * 日志
     */
    private static Logger logger = LoggerFactory.getLogger(WriteLogHandler.class);

    /**
     * 字符数量
     */
    private static final int LOG_SIZE = 2048;


    /**
     * 切入点声明
     */
    private static String LINE_SPLIT = "\n|\t\t\t";


    /**
     * 方法切入点（输出方法执行概要日志）
     *
     * @param joinPoint 连接点
     * @return 原方法返回值
     * @throws Throwable 异常
     */
    @Around(value = "pointCut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        printStart(sb);
        sb.append(LINE_SPLIT);
        sb.append("执行方法:");
        buildMethodInfo(joinPoint, sb);
        sb.append(LINE_SPLIT);

        try {
            Object retVal = joinPoint.proceed();
            String json = JsonUtil.toJson(retVal);
            if (!StringUtils.isEmpty(json) && json.length() >= LOG_SIZE) {
                json = json.substring(0, LOG_SIZE);
            }
            sb.append("返回值:").append(json);
            return retVal;
        } catch (Throwable ex) {
            sb.append("发生异常:").append(ex.getMessage());
            throw ex;
        } finally {
            //调用
            sb.append(LINE_SPLIT);
            sb.append("耗时(ms):");
            //计算方法耗时
            sb.append(System.currentTimeMillis() - startTime);
            printEnd(sb);
            logger.info(sb.toString());
        }
    }

    private void buildMethodInfo(JoinPoint joinPoint, StringBuilder stringBuilder) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        //方法
        stringBuilder.append(method.toGenericString());
        stringBuilder.append(LINE_SPLIT);
        stringBuilder.append("参数依次为:");
        for (int i = 0; i < joinPoint.getArgs().length; i++) {

            stringBuilder.append(LINE_SPLIT);
            stringBuilder.append("\t\t");
            stringBuilder.append(i);
            stringBuilder.append(":");
            Object argument = joinPoint.getArgs()[i];
            if (argument instanceof ServletRequest) {
                stringBuilder.append(argument);
            } else {
                stringBuilder.append(JsonUtil.toJson(argument));
            }
        }
    }

    private void printStart(StringBuilder stringBuilder) {
        stringBuilder.append("\n+-----------------");
    }

    private void printEnd(StringBuilder stringBuilder) {
        stringBuilder.append("\n\\__________________");
    }
}

