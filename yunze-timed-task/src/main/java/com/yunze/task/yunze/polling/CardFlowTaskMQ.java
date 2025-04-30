package com.yunze.task.yunze.polling;

import com.alibaba.fastjson.JSON;
import com.yunze.apiCommon.mapper.YzCardRouteMapper;
import com.yunze.apiCommon.utils.RateLimiterUtil;
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
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.IntStream;

@Slf4j
@Component
public class CardFlowTaskMQ {

    // 卡轮询 路由队列
    String polling_queueName = "polling_card_flow";

    final String CardFlow_routingKey = "polling.cardBatchCardFlow.routingKey";

    String polling_routingKey = "polling.card.flow";

    String polling_exchangeName = "polling_card";
    //

    String ad_exchangeName = null, ad_queueName = null, ad_routingKey = null,
            ad_del_exchangeName = null, ad_del_queueName = null, ad_del_routingKey = null;

    // cardArr是一个List<Map<String, Object>>类型的变量
    final int batchSize = 20; // 每批次的最大数量

    @Resource
    private YzCardRouteMapper yzCardRouteMapper;

    @Resource
    private YzCardMapper yzCardMapper;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private YzPassagewayPollingMapper yzPassagewayPollingMapper;

    @Resource
    private RabbitMQConfig rabbitMQConfig;

    /**
     * 轮询 卡状态
     * time 多少 分钟 后失效
     */
    @RabbitHandler
    @RabbitListener(queues = "admin_pollingCardFlowTest_queue")
    public void pollingCardFlow(String msg2) {
        if (StringUtils.isEmpty(msg2)) {
            return;
        }
        Map<String, Object> Pmap = JSON.parseObject(msg2);
        Integer time = Integer.parseInt(Pmap.get("time").toString());
        // 1.状态 正常 轮询开启 时 获取 每个 通道下卡号 加入队列

        Map<String, Object> findRouteID_Map = new HashMap<>();
        findRouteID_Map.put("FindCd_id", null);
        findRouteID_Map.put("cd_algorithm", "1");// 高频轮询

        List<Map<String, Object>> channelArr = yzCardRouteMapper.findRouteID(findRouteID_Map);
        if (channelArr != null && channelArr.size() > 0) {
            final String CardFlow_routingKey = "polling.cardBatchCardFlow.routingKey";
            try {
                // 设置任务 路由器 名称 与队列 名称
                ad_exchangeName = "polling_cardBatchCardFlow_exchange";
                ad_queueName = "polling_cardBatchCardFlow_queue";
                ad_del_exchangeName = "polling_dlxcardBatchCardFlow_exchange";
                ad_del_queueName = "polling_dlxcardBatchCardFlow_queue";
                ad_del_routingKey = "polling.dlxcardBatchCardFlow.routingKey";
                // rabbitMQConfig.creatExchangeQueue(ad_exchangeName, ad_queueName,
                // CardFlow_routingKey, ad_del_exchangeName, ad_del_queueName,
                // ad_del_routingKey, null);
            } catch (Exception e) {
                System.out.println(e.getMessage().toString());
            }

            // 2.获取 通道下卡号
            /*
             * // 创建一个固定大小的线程池，大小可以根据你的硬件资源和需求调整
             * int threadPoolSize = Runtime.getRuntime().availableProcessors();
             * ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);
             *
             * try {
             * // 提交每个通道的任务到线程池中
             * for (Map<String, Object> channel_obj : channelArr) {
             * // 每个通道创建一个独立的RateLimiter，设置为每秒最多2个许可
             * RateLimiter rateLimiter = RateLimiter.create(10.0); // 每秒2个token
             *
             * executor.submit(() -> {
             * try {
             * Map<String, Object> findMap = new HashMap<>();
             * String cd_id = channel_obj.get("cd_id").toString();
             * findMap.put("channel_id", cd_id);
             * List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);
             *
             * if (cardArr != null && !cardArr.isEmpty()) {
             * // 插入 通道轮询详情表
             * Map<String, Object> pollingPublic_Map = new HashMap<>();
             * pollingPublic_Map.put("cd_id", cd_id);
             * pollingPublic_Map.put("cd_current", 0);
             * String polling_id = VeDate.getNo(4);
             *
             * pollingPublic_Map.put("polling_type", "3");
             * pollingPublic_Map.put("cd_count", cardArr.size());
             * pollingPublic_Map.put("polling_id", polling_id);
             * yzPassagewayPollingMapper.add(pollingPublic_Map); // 新增 轮询详情表
             *
             * // 预计算静态值
             * String expiration = "" + (time * 1000 * 60);
             *
             * // 使用流式处理发送消息，并通过RateLimiter进行限流
             * for (Map<String, Object> card : cardArr) {
             * // 等待获取一个许可（即等待足够的时间以确保不超过速率）
             * rateLimiter.acquire(); // 默认获取1个许可
             *
             * Map<String, Object> Card = new HashMap<>(channel_obj);
             * Card.put("iccid", card.get("iccid"));
             * Card.put("card_no", card.get("card_no"));
             * Card.put("network_type", card.get("network_type"));
             * Card.put("polling_id", polling_id); // 轮询任务详情编号
             *
             * String msg = JSON.toJSONString(Card);
             *
             * // 生产任务
             * try {
             * rabbitTemplate.convertAndSend("polling_cardCardFlow_exchange",
             * CardFlow_routingKey, msg, message -> {
             * message.getMessageProperties().setExpiration(expiration);
             * return message;
             * });
             * } catch (Exception e) {
             * System.out.println(e.getMessage().toString());
             * }
             * }
             * }
             * } catch (Exception e) {
             * System.out.println(e.getMessage().toString());
             * }
             * });
             * }
             *
             * // 关闭线程池并等待所有任务完成
             * executor.shutdown();
             * try {
             * if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
             * executor.shutdownNow();
             * }
             * } catch (InterruptedException e) {
             * throw new RuntimeException(e);
             * }
             * } finally {
             * if (!executor.isTerminated()) {
             * executor.shutdownNow();
             * }
             * }
             */

            for (Map<String, Object> channel_obj : channelArr) {
                Map<String, Object> findMap = new HashMap<>();
                String cd_id = channel_obj.get("cd_id").toString();
                findMap.put("channel_id", cd_id);
                List<Map<String, Object>> cardArr = yzCardMapper.findChannelIdCar(findMap);
                if (cardArr != null && cardArr.size() > 0) {
                    // 插入 通道轮询详情表
                    Map<String, Object> pollingPublic_Map = new HashMap<>();
                    pollingPublic_Map.put("cd_id", cd_id);
                    pollingPublic_Map.put("cd_current", 0);
                    // 卡状态 用量 轮询
                    String polling_id = VeDate.getNo(4);

                    pollingPublic_Map.put("polling_type", "3");
                    pollingPublic_Map.put("cd_count", cardArr.size());
                    pollingPublic_Map.put("polling_id", polling_id);
                    yzPassagewayPollingMapper.add(pollingPublic_Map);// 新增 轮询详情表
                    // 2.卡状态
                    // 卡号放入路由

                    // 预计算静态值
                    String expiration = "" + (time * 1000 * 60);
                    List<String> iccids = new ArrayList<>();
                    cardArr.stream().forEach(card -> {
                        Map<String, Object> Card = new HashMap<>(channel_obj);
                        iccids.add(card.get("iccid").toString());

                        // 使用计数器替代indexOf检查
                        if (iccids.size() == batchSize) {
                            sendBatchMessage(Card, iccids, polling_id, expiration);
                            iccids.clear();
                        }
                    });

                    // 处理剩余的消息
                    if (!iccids.isEmpty()) {
                        Map<String, Object> Card = new HashMap<>(channel_obj);
                        sendBatchMessage(Card, iccids, polling_id, expiration);
                    }

                    /*
                     * cardArr.parallelStream()
                     * .filter(card -> "1".equals(card.get("status_id")) ||
                     * "2".equals(card.get("status_id")))
                     * .forEach(card -> {
                     * Map<String, Object> Card = new HashMap<>(channel_obj);
                     * Card.put("iccid", card.get("iccid"));
                     * Card.put("card_no", card.get("card_no"));
                     * Card.put("network_type", card.get("network_type"));
                     * Card.put("polling_id", polling_id); // 轮询任务详情编号
                     * 
                     * String msg = JSON.toJSONString(Card);
                     * 
                     * // 生产任务
                     * try {
                     * rabbitTemplate.convertAndSend("polling_cardCardFlow_exchange",
                     * CardFlow_routingKey, msg, message -> {
                     * message.getMessageProperties().setExpiration(expiration);
                     * return message;
                     * });
                     * } catch (Exception e) {
                     * System.out.println(e.getMessage());
                     * }
                     * });
                     */

                    /*
                     * for (int j = 0; j < cardArr.size(); j++) {
                     * Map<String, Object> card = cardArr.get(j);
                     * Map<String, Object> Card = new HashMap<>();
                     * Card.putAll(channel_obj);
                     * Card.put("iccid", card.get("iccid"));
                     * Card.put("card_no", card.get("card_no"));
                     * Card.put("network_type", card.get("network_type"));
                     * Card.put("polling_id", polling_id);//轮询任务详情编号
                     * String msg = JSON.toJSONString(Card);
                     * //生产任务
                     * try {
                     * rabbitTemplate.convertAndSend("polling_cardCardFlow_exchange",
                     * CardFlow_routingKey, msg, message -> {
                     * // 设置消息过期时间 time 分钟 过期
                     * message.getMessageProperties().setExpiration("" + (time * 1000 * 60));
                     * return message;
                     * });
                     * } catch (Exception e) {
                     * System.out.println(e.getMessage().toString());
                     * }
                     * }
                     */
                }
            }
        }
    }

    // 抽取发送消息的逻辑到单独的方法
    private void sendBatchMessage(Map<String, Object> Card, List<String> iccids, String polling_id, String expiration) {
        Card.put("iccids", iccids);
        Card.put("polling_id", polling_id);

        String msg = JSON.toJSONString(Card);
        try {
            rabbitTemplate.invoke(operations -> {
                operations.convertAndSend(ad_exchangeName, CardFlow_routingKey, msg,
                        message -> {
                            message.getMessageProperties().setExpiration(expiration);
                            return message;
                        });
                return null;
            });
        } catch (Exception e) {
            log.error("同步发送消息失败: {}", e.getMessage());
            throw new RuntimeException("同步发送消息失败", e);
        }
    }

}