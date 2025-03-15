package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.VeDate;
import com.yunze.common.config.RabbitMQConfig;
import com.yunze.common.mapper.yunze.YzCardMapper;
import com.yunze.common.mapper.yunze.YzPassagewayPollingMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import com.yunze.common.core.redis.RedisCache;
import com.alibaba.fastjson.TypeReference;
import java.util.concurrent.TimeUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

import javax.annotation.Resource;

/**
 * 卡流量轮询任务处理类
 * 负责处理通道下的卡流量轮询，包括数据缓存、批量处理和消息发送
 *
 * @author xxx
 * @since 2024-xx-xx
 */
@Slf4j
@Component
public class CardFlowTaskMQ {

    /** 卡轮询队列名称 */
    String polling_queueName = "polling_card_flow";
    /** 卡轮询路由键 */
    String polling_routingKey = "polling.card.flow";
    /** 卡轮询交换机名称 */
    String polling_exchangeName = "polling_card";

    /** 消息队列配置参数 */
    String ad_exchangeName = null, ad_queueName = null, ad_routingKey = null,
            ad_del_exchangeName = null, ad_del_queueName = null, ad_del_routingKey = null;

    /** 通道路由Mapper */
    @Resource
    private YzCardRouteMapper yzCardRouteMapper;
    /** 卡信息Mapper */
    @Resource
    private YzCardMapper yzCardMapper;
    /** RabbitMQ模板 */
    @Resource
    private RabbitTemplate rabbitTemplate;
    /** 通道轮询Mapper */
    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;
    /** RabbitMQ配置 */
    @Resource
    private RabbitMQConfig rabbitMQConfig;
    /** Redis缓存服务 */
    @Resource
    private RedisCache redisCache;

    /** Redis中存储通道数据的key */
    private static final String CHANNEL_CACHE_KEY = "polling:channels";
    /** Redis中存储卡数据的key前缀 */
    private static final String CARD_CACHE_PREFIX = "polling:channel:";
    /** Redis缓存过期时间(小时) */
    private static final Integer CACHE_TTL = 1;

    /**
     * 处理卡流量轮询的入口方法
     * 接收消息队列的消息，处理通道下的卡流量轮询任务
     *
     * @param msg2 消息内容，包含轮询时间等参数
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardFlowTest_queue")
    public void pollingCardFlow(String msg2) {
        if (StringUtils.isEmpty(msg2)) {
            return;
        }

        // 获取通道数据
        List<Map<String, Object>> channelArr = getChannelData();
        if (channelArr == null || channelArr.isEmpty()) {
            return;
        }

        // 处理业务逻辑
        processPollingLogic(msg2, channelArr);
    }

    /**
     * 获取通道数据
     * 优先从Redis获取，如果Redis中不存在则从MySQL获取并缓存到Redis
     *
     * @return 通道数据列表，如果获取失败返回null
     */
    private List<Map<String, Object>> getChannelData() {
        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("cd_algorithm", "1");

        // 先从Redis获取
        String channelCache = redisCache.getCacheObject(CHANNEL_CACHE_KEY);
        if (channelCache != null) {
            return JSON.parseObject(channelCache, new TypeReference<List<Map<String, Object>>>() {
            });
        }

        // Redis没有则从MySQL获取并缓存
        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && !channelArr.isEmpty()) {
            redisCache.setCacheObject(CHANNEL_CACHE_KEY, JSON.toJSONString(channelArr), CACHE_TTL, TimeUnit.HOURS);
            return channelArr;
        }

        log.error("无法获取通道数据");
        return null;
    }

    /**
     * 获取指定通道的卡数据
     * 优先从Redis获取，如果Redis中不存在则从MySQL获取并缓存到Redis
     *
     * @param cd_id 通道ID
     * @return 卡数据列表，如果获取失败返回null
     */
    private List<Map<String, Object>> getCardData(String cd_id) {
        String cardKey = CARD_CACHE_PREFIX + cd_id;
        String cardCache = redisCache.getCacheObject(cardKey);

        if (cardCache != null) {
            return JSON.parseObject(cardCache, new TypeReference<List<Map<String, Object>>>() {
            });
        }

        Map<String, Object> findMap = new HashMap<>();
        findMap.put("channel_id", cd_id);
        List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);

        if (cardArr != null && !cardArr.isEmpty()) {
            redisCache.setCacheObject(cardKey, JSON.toJSONString(cardArr), CACHE_TTL, TimeUnit.HOURS);
            return cardArr;
        }

        return null;
    }

    /**
     * 处理轮询业务逻辑的核心方法
     * 包括统计卡数量、创建轮询记录、分批处理和发送消息
     *
     * @param msg2       原始消息内容
     * @param channelArr 通道数据列表
     */
    private void processPollingLogic(String msg2, List<Map<String, Object>> channelArr) {
        Map<String, Object> Pmap = JSON.parseObject(msg2);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        final String CardFlow_routingKey = "polling.cardCardFlow.routingKey";

        setupQueueConfig();

        // 预先为每个通道创建轮询记录
        Map<String, String> channelPollingIds = new HashMap<>(); // 存储通道对应的轮询ID
        Map<String, Integer> channelCardCounts = new HashMap<>(); // 存储通道对应的卡数量
        int maxCardCount = 3200; // 预设最大卡数量

        // 初始化统计阶段
        for (Map<String, Object> channel : channelArr) {
            String cd_id = channel.get("cd_id").toString();
            List<Map<String, Object>> allCards = getCardData(cd_id);
            int validCardCount = 0;

            if (allCards != null) {
                // 只统计状态为1或2的卡
                validCardCount = (int) allCards.stream()
                        .filter(card -> "1".equals(card.get("status_id")) || "2".equals(card.get("status_id")))
                        .count();
            }

            channelCardCounts.put(cd_id, validCardCount);

            // 创建轮询记录
            if (validCardCount > 0) {
                String polling_id = createPollingRecord(cd_id, validCardCount);
                channelPollingIds.put(cd_id, polling_id);
            }
        }

        // 循环处理每个下标的卡数据，不超过实际卡数量
        for (int offset = 0; offset < maxCardCount; offset++) {
            List<Map<String, Object>> batchCards = new ArrayList<>();

            for (Map<String, Object> channel_obj : channelArr) {
                String cd_id = channel_obj.get("cd_id").toString();
                List<Map<String, Object>> allCards = getCardData(cd_id);

                // 检查是否超出该通道的卡数量
                if (allCards != null && offset < allCards.size()) {
                    Map<String, Object> card = allCards.get(offset);
                    // 只处理状态为1或2的卡
                    if ("1".equals(card.get("status_id")) || "2".equals(card.get("status_id"))) {
                        Map<String, Object> cardInfo = new HashMap<>(channel_obj);
                        cardInfo.put("iccid", card.get("iccid"));
                        cardInfo.put("card_no", card.get("card_no"));
                        cardInfo.put("network_type", card.get("network_type"));
                        cardInfo.put("polling_id", channelPollingIds.get(cd_id));
                        batchCards.add(cardInfo);
                    }
                }
                // 如果超出该通道的卡数量，直接跳过
            }

            // 批量发送消息
            if (!batchCards.isEmpty()) {
                String expiration = "" + (time * 1000 * 60);
                batchCards.forEach(card -> rabbitTemplate.convertAndSend(
                        "polling_cardCardFlow_exchange",
                        CardFlow_routingKey,
                        JSON.toJSONString(card),
                        message -> {
                            message.getMessageProperties().setExpiration(expiration);
                            return message;
                        }));

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * 设置消息队列配置
     * 初始化交换机、队列和路由键等配置
     */
    private void setupQueueConfig() {
        try {
            ad_exchangeName = "polling_cardCardFlow_exchange";
            ad_queueName = "polling_cardCardFlow_queue";
            ad_del_exchangeName = "polling_dlxcardCardFlow_exchange";
            ad_del_queueName = "polling_dlxcardCardFlow_queue";
            ad_del_routingKey = "polling.dlxcardCardFlow.routingKey";
        } catch (Exception e) {
            log.error("队列配置异常", e);
        }
    }

    /**
     * 创建轮询记录
     * 记录通道的轮询信息，包括卡数量、当前进度等
     *
     * @param cd_id     通道ID
     * @param cardCount 卡数量
     * @return 生成的轮询ID
     */
    private String createPollingRecord(String cd_id, int cardCount) {
        String polling_id = VeDate.getNo(4);
        Map<String, Object> pollingPublic_Map = new HashMap<>();
        pollingPublic_Map.put("cd_id", cd_id); // 通道ID
        pollingPublic_Map.put("cd_current", 0); // 当前处理进度
        pollingPublic_Map.put("polling_type", "3"); // 轮询类型：3-流量轮询
        pollingPublic_Map.put("cd_count", cardCount); // 总卡数
        pollingPublic_Map.put("polling_id", polling_id); // 轮询ID
        yzPassagewayPollingMapper.add(pollingPublic_Map);
        return polling_id;
    }
}
