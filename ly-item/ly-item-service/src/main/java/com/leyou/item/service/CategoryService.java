package com.leyou.item.service;

import com.leyou.item.mapper.CategoryMapper;
import com.leyou.item.pojo.Category;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-23 16:09
 **/
@Service
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    public List<Category> queryCategoryByPid(Long pid) {
        Category t = new Category();
        t.setParentId(pid);
        return this.categoryMapper.select(t);
    }

    public List<String> queryNamesByIds(List<Long> ids){
        return this.categoryMapper.selectByIdList(ids)
                .stream().map(c -> c.getName()).collect(Collectors.toList());
    }
}
