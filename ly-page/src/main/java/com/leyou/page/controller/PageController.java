package com.leyou.page.controller;

import com.leyou.page.service.PageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-03 15:06
 **/
@Controller
public class PageController {

    @Autowired
    private PageService pageService;

    @GetMapping("item/{id}.html")
    public String toGoodsPage(@PathVariable("id") Long id, Model model){
        // 查询模型数据
        Map<String, Object> data = this.pageService.loadModel(id);
        // 添加模型数据
        model.addAttribute("spu", data.get("spu"));
        model.addAttribute("skus", data.get("skus"));
        model.addAttribute("detail", data.get("detail"));
        model.addAttribute("categories", data.get("categories"));
        model.addAttribute("brand", data.get("brand"));
        model.addAttribute("specGroups", data.get("specGroups"));

        this.pageService.asyncCreateHtml(id);
        return "item";
    }
}
