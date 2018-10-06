package com.leyou.item.api;

import com.leyou.item.pojo.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-01 16:23
 **/
@RequestMapping("brand")
public interface BrandApi {
    @GetMapping("list")
    List<Brand> queryBrands(@RequestParam("ids") List<Long> ids);
}
