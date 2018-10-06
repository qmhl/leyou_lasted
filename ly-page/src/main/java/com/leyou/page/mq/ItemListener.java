package com.leyou.page.mq;

import com.leyou.page.service.PageService;
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
    private PageService pageService;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("ly.page.create.queue"),
            exchange = @Exchange(ignoreDeclarationExceptions = "true",
                    name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = {"item.insert", "item.update"}
    ))
    public void listenCreate(Long id) {
        if (id == null) {
            return;
        }
        // 创建新的静态页
        this.pageService.createHtml(id);
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue("ly.page.delete.queue"),
            exchange = @Exchange(ignoreDeclarationExceptions = "true",
                    name = "ly.item.exchange", type = ExchangeTypes.TOPIC),
            key = "item.delete"
    ))
    public void listenDelete(Long id) {
        if (id == null) {
            return;
        }
        // 删除静态页
        this.pageService.deleteHtml(id);
    }
}
