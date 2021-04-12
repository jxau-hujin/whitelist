package cn.gezelligheid.whitelist.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 白名单配置文件
 * 读取配置文件中的数据放入 whiteList
 */
@ConfigurationProperties("whitelist")
public class WhiteListProperties {
    private List<String> whiteList;

    public List<String> getWhiteList() {
        return whiteList;
    }

    public void setWhiteList(List<String> whiteList) {
        this.whiteList = whiteList;
    }
}
