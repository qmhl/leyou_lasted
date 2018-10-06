package com.leyou.page.client;

import com.leyou.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-30 16:35
 **/
@FeignClient("item-service")
public interface CategoryClient extends CategoryApi {
}
