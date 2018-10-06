package com.leyou.item.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-30 16:34
 **/
@RequestMapping("category")
public interface CategoryApi {

    @GetMapping("names")
    List<String> queryNamesByIds(@RequestParam("ids") List<Long> ids);
}
