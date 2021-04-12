package cn.gezelligheid.whitelist.aop;


import cn.gezelligheid.whitelist.annotation.WhiteList;
import cn.gezelligheid.whitelist.config.WhiteListAutoConfiguration;
import com.alibaba.fastjson.JSON;
import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

@Component
@Aspect
public class JoinPoint {

    private Logger logger = LoggerFactory.getLogger(JoinPoint.class);

    @Resource
    private List whiteList;

    @Pointcut("@annotation(cn.gezelligheid.whitelist.annotation.WhiteList)")
    public void aopPoint(){

    }

    @Around("aopPoint()")
    public Object doRouter(ProceedingJoinPoint jp) throws Throwable {
        Method method = getMethod(jp);
        WhiteList whiteListAnnotation = method.getAnnotation(WhiteList.class);
        boolean isObject = whiteListAnnotation.isObject();
        Object key;
        if(isObject){
            key = getObject(whiteListAnnotation.key(), jp.getArgs());
            if(key == null){
                return jp.proceed();
            }
            whiteList = WhiteListAutoConfiguration.conversionObject(whiteList, key.getClass());
        }else{
            key = getString(whiteListAnnotation.key(), jp.getArgs());
        }
        logger.info("key: %s value: %s", whiteListAnnotation.key(), key.toString());
        if(checkWhiteList(key)){
            return jp.proceed();
        }
        return wrapperResultJson(whiteListAnnotation, method);
    }


    /**
     * WhiteList key 为对象全限定名时
     * @param key
     * @param args
     * @return
     */
    private Object getObject(String key, Object[] args) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, NoSuchMethodException {
        Class clazz = Class.forName(key);
        for(Object arg : args){
            if(arg.getClass() == clazz){
                return BeanUtils.cloneBean(arg);
            }
        }
        return null;
    }

    /**
     * WhiteList key 为参数名
     * @param key
     * @param args
     * @return
     */
    private Object getString(String key, Object[] args){
        String value = null;
        for(Object arg : args){
            try {
                if(null == value || "".equals(value)){
                    value = BeanUtils.getProperty(arg, value);
                }
            } catch(Exception e){
                if(args.length == 1){
                    return args[0].toString();
                }
            }
        }
        return value;
    }

    /**
     * 不在白名单中返回自定义 Json
     * @param whiteList
     * @param method
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    private Object wrapperResultJson(WhiteList whiteList, Method method) throws InstantiationException, IllegalAccessException {
        String resultJson = whiteList.resultJson();
        Class<?> returnType = method.getReturnType();
        if("".equals(resultJson)){
            return returnType.newInstance();
        }
        return JSON.parseObject(resultJson, returnType);
    }

    /**
     * 检查是否在白名单中
     * @param key
     * @return
     */
    private boolean checkWhiteList(Object key){
        if(key == null){
            return true;
        }
        for(Object o : whiteList){
            if(key.getClass() == o.getClass()){
                return o.equals(key);
            }
        }
        return false;
    }

    /**
     * 获取 Method
     * @param jp
     * @return
     */
    private Method getMethod(ProceedingJoinPoint jp){
        Signature signature = jp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        return methodSignature.getMethod();
    }
}
