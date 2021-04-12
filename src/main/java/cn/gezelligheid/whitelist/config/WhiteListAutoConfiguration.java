package cn.gezelligheid.whitelist.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConditionalOnClass(WhiteListProperties.class)
@EnableConfigurationProperties(WhiteListProperties.class)
public class WhiteListAutoConfiguration {
    private static final String SPLIT = "&";

    @Bean("whiteList")
    @ConditionalOnMissingBean
    public List<String> whiteList(WhiteListProperties whiteListProperties){
        return whiteListProperties.getWhiteList();
    }

    /**
     * 将 whiteList 转换成对象
     * @param whiteList
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static List conversionObject(List whiteList, Class clazz) throws InstantiationException, IllegalAccessException {
        List result = new ArrayList(10);

        for(Object o : whiteList){
            Object target = conversion(clazz.getDeclaredFields(), clazz.newInstance(), o);
            if(target == null){
                continue;
            }
            result.add(target);
        }
        return result;
    }

    /**
     * 拷贝对象方法
     * @param fields
     * @param dest
     * @param src
     * @return
     * @throws IllegalAccessException
     */
    private static Object conversion(Field[] fields, Object dest, Object src) throws IllegalAccessException {
        if(src.getClass() != String.class){
            return null;
        }
        String[] splits = ((String)src).split(SPLIT);
        if(splits.length != fields.length){
            return null;
        }
        for(int i = 0; i < splits.length; i++){
            fields[i].setAccessible(true);
            fields[i].set(dest, splits[i]);
        }
        return dest;
    }
}
