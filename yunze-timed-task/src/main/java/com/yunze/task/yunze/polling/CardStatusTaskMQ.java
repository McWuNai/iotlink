package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CardStatusTaskMQ {

    private static final String POLLING_TYPE = "2"; // 卡状态轮询
    private static final String ALGORITHM_TYPE = "1"; // 高频轮询
    private static final String EXCHANGE_NAME = "polling_cardCardStatus_exchange";
    private static final String ROUTING_KEY = "polling.cardCardStatus.routingKey";

    /** Redis缓存key前缀 */
    private static final String CHANNEL_CACHE_KEY = "polling:status:channels";
    private static final String CARD_CACHE_PREFIX = "polling:status:channel:";
    private static final Integer CACHE_TTL = 11;

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    @Resource
    private YzCardMapper yzCardMapper;
    @Resource
    private RabbitTemplate rabbitTemplate;
    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;
    @Resource
    private RedisCache redisCache;

    /**
     * 处理卡状态轮询
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardStatusTest_queue")
    public void pollingCardStatus(String msg2) {
        if (StringUtils.isEmpty(msg2)) {
            return;
        }

        // 1. 解析消息
        Map<String, Object> Pmap = JSON.parseObject(msg2);
        Integer time = Integer.parseInt(Pmap.get("time").toString());

        // 2. 获取通道数据
        List<Map<String, Object>> channelArr = getChannelData();
        if (channelArr == null || channelArr.isEmpty()) {
            log.warn("未获取到通道数据");
            return;
        }

        // 3. 处理每个通道
        processChannels(channelArr, time);
    }

    /**
     * 获取通道数据
     */
    private List<Map<String, Object>> getChannelData() {
        // 先从Redis获取
        String channelCache = redisCache.getCacheObject(CHANNEL_CACHE_KEY);
        if (channelCache != null) {
            return JSON.parseObject(channelCache, new TypeReference<List<Map<String, Object>>>() {
            });
        }

        // 从数据库获取
        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("cd_algorithm", ALGORITHM_TYPE);
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);

        // 缓存结果
        if (channelArr != null && !channelArr.isEmpty()) {
            redisCache.setCacheObject(CHANNEL_CACHE_KEY, JSON.toJSONString(channelArr), CACHE_TTL, TimeUnit.HOURS);
        }

        return channelArr;
    }

    /**
     * 获取通道下的卡数据
     */
    private List<Map<String, Object>> getChannelCards(String cd_id) {
        // 先从Redis获取
        String cardKey = CARD_CACHE_PREFIX + cd_id;
        String cardCache = redisCache.getCacheObject(cardKey);
        if (cardCache != null) {
            return JSON.parseObject(cardCache, new TypeReference<List<Map<String, Object>>>() {
            });
        }

        // 从数据库获取
        Map<String, Object> findMap = new HashMap<>();
        findMap.put("channel_id", cd_id);
        List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);

        // 缓存结果
        if (cardArr != null && !cardArr.isEmpty()) {
            redisCache.setCacheObject(cardKey, JSON.toJSONString(cardArr), CACHE_TTL, TimeUnit.HOURS);
        }

        return cardArr;
    }

    /**
     * 处理通道数据
     * 按offset和通道顺序逐个发送消息
     */
    private void processChannels(List<Map<String, Object>> channelArr, Integer time) {
        int maxOffset = 3200; // 最大卡数量
        int maxChannels = channelArr.size(); // 通道数量

        // 预先获取所有通道的卡数据和创建轮询记录
        Map<String, String> channelPollingIds = new HashMap<>();
        Map<String, List<Map<String, Object>>> channelCards = new HashMap<>();

        // 1. 初始化阶段：获取数据和创建轮询记录
        for (Map<String, Object> channel : channelArr) {
            String cd_id = channel.get("cd_id").toString();
            List<Map<String, Object>> cardArr = getChannelCards(cd_id);

            if (cardArr != null && !cardArr.isEmpty()) {
                String polling_id = createPollingRecord(cd_id, cardArr.size());
                channelPollingIds.put(cd_id, polling_id);
                channelCards.put(cd_id, cardArr);
            }
        }

        // 2. 处理阶段：按offset和通道顺序逐个发送
        for (int offset = 0; offset < maxOffset; offset++) { // 外循环：处理每个offset
            for (int channelIndex = 0; channelIndex < maxChannels; channelIndex++) { // 内循环：处理每个通道
                Map<String, Object> channel = channelArr.get(channelIndex);
                String cd_id = channel.get("cd_id").toString();
                List<Map<String, Object>> cardArr = channelCards.get(cd_id);

                // 检查是否有该offset的卡
                if (cardArr != null && offset < cardArr.size()) {
                    Map<String, Object> card = cardArr.get(offset);

                    // 发送单条消息
                    sendCardMessage(channel, card, channelPollingIds.get(cd_id), time);

                    // 每发送一条消息后短暂休眠
                    try {
                        Thread.sleep(50); // 单条消息发送间隔
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        log.error("消息发送中断: offset={}, channel={}", offset, cd_id);
                        return;
                    }
                }
            }

            // 每处理完一个offset的所有通道后，稍作休息
            try {
                Thread.sleep(100); // 每批次间隔
                log.debug("完成offset {} 的处理", offset);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("批次处理中断: offset={}", offset);
                break;
            }
        }
    }

    /**
     * 创建轮询记录
     */
    private String createPollingRecord(String cd_id, int cardCount) {
        String polling_id = VeDate.getNo(4);
        Map<String, Object> pollingPublic_Map = new HashMap<>();
        pollingPublic_Map.put("cd_id", cd_id);
        pollingPublic_Map.put("cd_current", 0);
        pollingPublic_Map.put("polling_type", POLLING_TYPE);
        pollingPublic_Map.put("cd_count", cardCount);
        pollingPublic_Map.put("polling_id", polling_id);
        yzPassagewayPollingMapper.add(pollingPublic_Map);
        return polling_id;
    }

    /**
     * 发送卡消息
     */
    private void sendCardMessage(Map<String, Object> channel, Map<String, Object> card,
            String polling_id, Integer time) {
        try {
            Map<String, Object> messageData = new HashMap<>(channel);
            messageData.put("iccid", card.get("iccid"));
            messageData.put("card_no", card.get("card_no"));
            messageData.put("polling_id", polling_id);

            rabbitTemplate.convertAndSend(
                    EXCHANGE_NAME,
                    ROUTING_KEY,
                    JSON.toJSONString(messageData),
                    message -> {
                        message.getMessageProperties()
                                .setExpiration(String.valueOf(time * 1000 * 60));
                        return message;
                    });

            Thread.sleep(50); // 控制发送速率
        } catch (Exception e) {
            log.error("发送消息失败: {}", e.getMessage());
        }
    }
}
