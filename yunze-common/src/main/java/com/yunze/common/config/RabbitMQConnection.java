package com.yunze.common.config;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * @ClassName RabbitMQConnection
 * @Version V1.0
 **/
public class RabbitMQConnection {

    /**
     * 创建连接
     *
     * @return
     */
    public static Connection getConnection()  {
        int Reconnection = 10;//重连次数
        int current = 1;//当前次数
        try {
            Connection con = CreateConnection();
            if(con!=null){
                return con;
            }else{
                for (int i = 0; i < Reconnection; i++) {
                    System.out.println("== RabbitMQConnection getConnection [重连次数（"+current+"）] == ");
                    con = CreateConnection();
                    current +=1;
                    if(con!=null){
                        break;
                    }
                }
            }
            if(con==null){
                System.out.println("重连次数（"+current+"）con 还是 null ");
            }
            return con;
        }catch (Exception e){
            System.out.println("=[Connection getConnection() Exception 【Start】 ]");
            System.out.println(e.getMessage());
            System.out.println("=[Connection getConnection() Exception 【End】 ]");
            return  null;
        }
    }

    /**
     *
     * 创建连接
     * @return
     */
    public  static Connection CreateConnection()  {
        try {
            //1.创建connectionFactory
            ConnectionFactory connectionFactory = new ConnectionFactory();
            //2.配置Host
            connectionFactory.setHost(System.getenv("RABBITMQ_HOST"));
            //3.设置Port
            connectionFactory.setPort(5672);
            connectionFactory.setRequestedHeartbeat( 40 );
            //4.设置账户和密码
            connectionFactory.setUsername(System.getenv("RABBITMQ_USER"));
            connectionFactory.setPassword(System.getenv("RABBITMQ_PASSWORD"));
            //5.设置VirtualHost
            connectionFactory.setVirtualHost(System.getenv("RABBITMQ_VHOST"));
            return connectionFactory.newConnection();
        }catch (Exception e){
            System.out.println("=[Connection CreateConnection() Exception 【Start】 ]");
            System.out.println(e.getMessage());
            System.out.println("=[Connection CreateConnection() Exception 【End】 ]");
            return  null;
        }
    }





}
