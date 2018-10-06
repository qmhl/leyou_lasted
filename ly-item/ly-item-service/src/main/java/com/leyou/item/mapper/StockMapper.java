package com.leyou.item.mapper;

import com.leyou.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.DeleteByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-27 16:42
 **/
public interface StockMapper extends Mapper<Stock>,DeleteByIdListMapper<Stock,Long> {
}
