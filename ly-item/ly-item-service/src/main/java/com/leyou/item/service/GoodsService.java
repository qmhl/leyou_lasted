package com.leyou.item.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.mapper.*;
import com.leyou.item.pojo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-26 18:18
 **/
@Service
public class GoodsService {

    @Autowired
    private SpuMapper spuMapper;

    @Autowired
    private BrandMapper brandMapper;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuVO> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 1、分页
        PageHelper.startPage(page, rows);
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        // 2、上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        // 3、关键词过滤
        if (StringUtils.isNotBlank(key)) {
            criteria.andLike("title", "%" + key + "%");
        }
        // 4、查询
        Page<Spu> info = (Page<Spu>) this.spuMapper.selectByExample(example);

        List<SpuVO> list = info.stream().map(spu -> {
            SpuVO spuVO = new SpuVO();
            // 添加基本属性
            BeanUtils.copyProperties(spu, spuVO);
            // 添加cname
            List<Long> ids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
            spuVO.setCname(
                    StringUtils.join(this.categoryService.queryNamesByIds(ids), "/"));

            // 添加bname
            Brand brand = this.brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuVO.setBname(brand.getName());
            return spuVO;
        }).collect(Collectors.toList());

        return new PageResult<>(info.getTotal(), list);
    }

    @Transactional
    public void saveGoods(SpuVO spu) {
        // 1、新增SPU
        spu.setId(null);
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        this.spuMapper.insert(spu);

        // 2、新增SpuDetail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        this.spuDetailMapper.insert(detail);

        // 3、新增Sku
        saveSkuAndStock(spu);

        // 发送消息
        this.amqpTemplate.convertAndSend("item.insert", spu.getId());
    }

    private void saveSkuAndStock(SpuVO spu) {
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            // 4、新增Stock
            Stock stock = new Stock();
            stock.setStock(sku.getStock());
            stock.setSkuId(sku.getId());
            this.stockMapper.insert(stock);
        }
    }

    public SpuDetail querySpuDetailBySpuId(Long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }

    public List<Sku> querySkuList(Long id) {
        // 查询spu下的sku
        Sku t = new Sku();
        t.setSpuId(id);
        List<Sku> skus = this.skuMapper.select(t);

        for (Sku sku : skus) {
            // 查询sku的库存
            Stock stock = this.stockMapper.selectByPrimaryKey(sku.getId());
            sku.setStock(stock.getStock());
        }
        return skus;
    }

    public void updateGoods(SpuVO spu) {
        if (spu.getId() == null) {
            throw new RuntimeException("spu的id不能为空");
        }
        // 修改spu
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);
        spu.setValid(null);
        spu.setSaleable(null);
        this.spuMapper.updateByPrimaryKeySelective(spu);

        // 修改spuDetail
        this.spuDetailMapper.updateByPrimaryKey(spu.getSpuDetail());

        // 删除stock
        List<Sku> skus = spu.getSkus();
        if (skus != null && skus.size() > 0) {
            // 获取sku 的id集合
            List<Long> ids = skus.stream().map(sku -> sku.getId()).collect(Collectors.toList());
            this.stockMapper.deleteByIdList(ids);
        }

        // 删除sku
        Sku t = new Sku();
        t.setSpuId(spu.getId());
        this.skuMapper.delete(t);


        // 新增sku和stock
        saveSkuAndStock(spu);

        // 发送消息
        this.amqpTemplate.convertAndSend("item.update", spu.getId());
    }

    public Spu querySpuById(Long id) {
        return this.spuMapper.selectByPrimaryKey(id);
    }
}
