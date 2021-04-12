package cn.gezelligheid.whitelist.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 白名单注解
 * @key 对比的字段（对象，字符串）
 * @resultJson 不在白名单中返回的 Json 数据
 * @isObject key 是否是对象
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface WhiteList {
    String key() default "";

    String resultJson() default "";

    boolean isObject() default false;
}
