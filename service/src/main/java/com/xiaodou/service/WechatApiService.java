package com.xiaodou.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xiaodou.config.WechatProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

/**
 * 与微信官方 API 交互的服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WechatApiService {

    private final RestTemplate restTemplate;
    private final WechatProperties wechatProperties;

    private static final String CODE_2_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session?appid={appid}&secret={secret}&js_code={js_code}&grant_type=authorization_code";

    /**
     * 调用微信 code2session 接口，用 code 换取 openid 和 session_key
     *
     * @param code 小程序端通过 wx.login() 获取的 code
     * @return 微信返回的会话信息
     */
    public WechatSessionVO code2Session(String code) {
        String url = CODE_2_SESSION_URL
                .replace("{appid}", wechatProperties.getAppid())
                .replace("{secret}", wechatProperties.getSecret())
                .replace("{js_code}", code);

        try {
            WechatSessionVO sessionVO = restTemplate.getForObject(url, WechatSessionVO.class);
            if (sessionVO != null && sessionVO.getErrcode() != null && sessionVO.getErrcode() != 0) {
                log.error("调用 code2session 接口失败: errcode={}, errmsg={}", sessionVO.getErrcode(), sessionVO.getErrmsg());
                return null;
            }
            return sessionVO;
        } catch (Exception e) {
            log.error("请求微信 code2session 接口时发生异常", e);
            return null;
        }
    }

    /**
     * 微信 code2session 接口的响应体
     */
    @Data
    public static class WechatSessionVO {
        private String openid;
        @JsonProperty("session_key")
        private String sessionKey;
        private String unionid;
        private Integer errcode;
        private String errmsg;
    }
}
