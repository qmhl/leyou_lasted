package com.leyou.search.mq;

import com.leyou.search.service.IndexService;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-05 14:43
 **/
@Component
public class ItemListener {

    @Autowired
    private IndexService indexService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("ly.search.create.queue"),
            exchange = @Exchange(ignoreDeclarationExceptions = "true",
                    name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void listenCreate(Long id){
        if(id == null){
            return;
        }
        // 添加或更新索引
        this.indexService.createOrUpdateIndex(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("ly.search.delete.queue"),
            exchange = @Exchange(ignoreDeclarationExceptions = "true",
                    name = "ly.item.exchange",type = ExchangeTypes.TOPIC),
            key = "item.delete"
    ))
    public void listenDelete(Long id){
        if(id == null){
            return;
        }
        // 删除对应索引
        this.indexService.deleteIndex(id);
    }
}
