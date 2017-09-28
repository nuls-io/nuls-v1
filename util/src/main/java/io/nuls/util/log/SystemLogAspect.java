package io.nuls.util.log;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * Created by Niels on 2017/9/27.
 * nuls.io
 */
@Aspect
@Component
public class SystemLogAspect {
    @Pointcut("execution (* io.nuls..*.*(..))")
    public void aspectJMethod() {
    }
//    @Before("aspectJMethod()")
//    public void doBefore(JoinPoint joinPoint){
//        System.out.println("----dobefore()开始----");
//        System.out. println("执行业务逻辑前做一些工作");
//        System.out.println("通过jointPoint获得所需内容");
//        System.out.println("----dobefore()结束----");
//    }
    @Around("aspectJMethod()")
    public Object doAround(ProceedingJoinPoint pjp) throws Throwable{

//        Object[] args=pjp.getArgs();
//        long start = System.currentTimeMillis();
        //核心逻辑
        Object retval=pjp.proceed();
//        long useTime = System.currentTimeMillis()-start;
//        Log.debug(pjp.getSignature()+"args:{},return:{},useTime:{}ms",args,retval,useTime);
        return retval;
    }
//    @After(value="aspectJMethod()")
//    public void doAfter(JoinPoint joinPoint){
//        System.out.println("----doAfter()开始----");
//        System.out.println("执行核心逻辑之后，所做工作");
//        System.out.println("通过jointPoint获得所需内容");
//        System.out.println("----doAfter()结束----");
//    }
//
//    @AfterReturning(value="aspectJMethod()",returning="retval")
//    public void doReturn(JoinPoint joinPoint, String retval){
//        System.out.println("AfterReturning()开始");
//        System.out.println("Return value= "+retval);
//        System.out.println("此处可对返回结果做一些处理");
//        System.out.println("----AfterReturning()结束----");
//
//    }

    @AfterThrowing(value="aspectJMethod()", throwing="e")
    public void doThrowing(JoinPoint joinPoint,Exception e){
        Log.error("",e);
    }
}
