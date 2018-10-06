package com.leyou.item.controller;

import com.leyou.item.pojo.Category;
import com.leyou.item.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-23 16:07
 **/
@RestController
@RequestMapping("category")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    /**
     * 根据父id查询商品分类
     * @param pid
     * @return
     */
    @GetMapping("list")
    public ResponseEntity<List<Category>> queryCategoryByPid(@RequestParam("pid") Long pid){
        List<Category> list = this.categoryService.queryCategoryByPid(pid);
        // 判断是否查询到
        if(list == null || list.size() <= 0 ){
            // 返回404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // 成功返回200
        return ResponseEntity.ok(list);
    }

    @GetMapping("names")
    public ResponseEntity<List<String>> queryNamesByIds(@RequestParam("ids") List<Long> ids){
        List<String> list = this.categoryService.queryNamesByIds(ids);
        // 判断是否查询到
        if(list == null || list.size() <= 0 ){
            // 返回404
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        // 成功返回200
        return ResponseEntity.ok(list);
    }
}
