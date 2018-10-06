package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.BrandMapper;
import com.leyou.item.pojo.Brand;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-23 18:32
 **/
@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;

    public PageResult<Brand> queryBrandByPage(
            Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        // 1、分页
        PageHelper.startPage(page, rows);
        // 排序和查询
        Example example = new Example(Brand.class);
        // 2、排序
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByCause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByCause);
        }
        // 3、查询
        if(StringUtils.isNotBlank(key)){
            String likeKey = "%" + key + "%";
            example.createCriteria()
                    .orLike("name", likeKey)
                    .orEqualTo("letter", key.toUpperCase());
        }
        List<Brand> list = this.brandMapper.selectByExample(example);

        PageInfo<Brand> info = new PageInfo<>(list);

        return new PageResult<>(info.getTotal(), list);
    }

    @Transactional
    public void saveBrand(Brand brand, List<Long> cids) {
        // 新增品牌
        brand.setId(null);
        this.brandMapper.insert(brand);

        // 新增中间表
        for (Long cid : cids) {
            this.brandMapper.insertCategoryBrand(Long.valueOf(cid), brand.getId());
        }
    }

    public List<Brand> queryBrandByCid(Long cid) {
        return this.brandMapper.queryByCategoryId(cid);
    }

    public List<Brand> queryBrands(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }
}
