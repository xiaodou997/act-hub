package com.xiaodou.service;

import com.xiaodou.config.Ip2RegionConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lionsoul.ip2region.xdb.Searcher;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Role;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

/**
 * IP地理位置查询服务
 * <p>
 * 基于ip2region数据库提供IP地址到地理位置的查询功能，支持IPv4和IPv6地址查询。
 * 使用xdb格式数据库文件，初始化时加载到内存中提高查询效率。
 * </p>
 *
 * @ClassName: Ip2RegionService
 * @Description: IP地理位置查询服务实现
 * @Author: xiaodou V=>dddou117
 * @Date: 2025/5/9
 * @Version: V1.0
 * @JDK: JDK21
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Role(BeanDefinition.ROLE_INFRASTRUCTURE)
public class Ip2RegionService {

    /**
     * ip2region搜索器实例
     */
    private Searcher searcher;

    /**
     * IP数据库配置
     */
    private final Ip2RegionConfig ip2RegionConfig;

    /**
     * 初始化方法，在Bean创建后自动调用
     * <p>
     * 加载ip2region数据库文件到内存中，初始化搜索器实例
     * </p>
     *
     * @throws RuntimeException 当数据库文件不存在或加载失败时抛出
     */
    @PostConstruct
    public void init() {
        try {
            // 直接从配置的路径加载文件
            File file = new File(ip2RegionConfig.getPath());
            if (!file.exists()) {
                throw new RuntimeException("IP数据库文件不存在: " + file.getAbsolutePath());
            }

            String dbPath = file.getAbsolutePath();

            // 创建完全基于文件的查询对象
            byte[] cBuff = Searcher.loadContentFromFile(dbPath);
            searcher = Searcher.newWithBuffer(cBuff);

            log.info("ip2region数据库加载成功");
        } catch (Exception e) {
            log.error("初始化ip2region失败", e);
            throw new RuntimeException("IP定位服务初始化失败");
        }
    }

    /**
     * 根据IP地址查询完整地理位置信息
     * <p>
     * 返回格式：国家|区域|省份|城市|ISP
     * 当IP无效或查询失败时返回"未知|未知|未知|未知|未知"
     * </p>
     *
     * @param ip 要查询的IP地址(IPv4/IPv6)
     * @return 地理位置信息字符串，用竖线分隔
     */
    public String getLocation(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "未知|未知|未知|未知|未知";
        }

        try {
            String region = searcher.search(ip);
            return region != null ? region : "未知|未知|未知|未知|未知";
        } catch (Exception e) {
            log.warn("IP定位失败: {}", ip, e);
            return "未知|未知|未知|未知|未知";
        }
    }

    /**
     * 获取简化版地理位置信息
     * <p>
     * 返回格式：国家-省份-城市
     * 当某部分信息为0时会替换为"未知XX"
     * </p>
     *
     * @param ip 要查询的IP地址(IPv4/IPv6)
     * @return 简化版地理位置信息字符串，用短横线分隔
     */
    public String getSimpleLocation(String ip) {
        String[] parts = getLocation(ip).split("\\|");
        if (parts.length >= 5) {
            // 格式: 国家-省份-城市
            return String.format("%s-%s-%s", "0".equals(parts[0]) ? "未知国家" : parts[0],
                "0".equals(parts[2]) ? "未知省份" : parts[2], "0".equals(parts[3]) ? "未知城市" : parts[3]);
        }
        return "未知位置";
    }

    /**
     * 销毁方法，在Bean销毁前自动调用
     * <p>
     * 释放ip2region搜索器占用的资源
     * </p>
     */
    @PreDestroy
    public void destroy() {
        try {
            if (searcher != null) {
                searcher.close();
            }
        } catch (IOException e) {
            log.error("关闭ip2region资源失败", e);
        }
    }
}
