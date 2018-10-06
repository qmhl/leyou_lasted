package com.leyou.test;

import com.leyou.LySearchApplication;
import com.leyou.common.pojo.PageResult;
import com.leyou.item.pojo.SpuVO;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.service.IndexService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-30 16:25
 **/
@RunWith(SpringRunner.class)
@SpringBootTest(classes = LySearchApplication.class)
public class FeignTest {

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private IndexService indexService;

    @Test
    public void testQuery() {
        List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(74L, 75L, 76L));
        for (String name : names) {
            System.out.println("name = " + name);
        }
    }

    @Test
    public void createIndex() {
        this.elasticsearchTemplate.createIndex(Goods.class);
        this.elasticsearchTemplate.putMapping(Goods.class);
    }

    @Test
    public void loadData(){
        int page = 1;
        int rows = 100;
        int size = 0;
        do {
            // 查询spu
            PageResult<SpuVO> result = this.goodsClient.querySpuByPage(page, rows, true, null);
            List<SpuVO> spus = result.getItems();

            // spu转为goods
            List<Goods> goods = spus.stream().map(spu -> this.indexService.buildGoods(spu)).collect(Collectors.toList());

            // 把goods放入索引库
            this.goodsRepository.saveAll(goods);

            size = spus.size();
            page++;
        }while (size == 100);
    }
}

