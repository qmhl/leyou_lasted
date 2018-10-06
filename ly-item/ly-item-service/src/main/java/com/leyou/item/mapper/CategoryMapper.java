package com.leyou.item.mapper;

import com.leyou.item.pojo.Category;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-23 16:10
 **/
public interface CategoryMapper extends Mapper<Category>, SelectByIdListMapper<Category,Long> {
}
