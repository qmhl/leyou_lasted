package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import com.leyou.page.util.ThreadUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * @author: HuYi.Zhang
 * @create: 2018-07-03 17:07
 **/
@Service
public class PageService {

    @Value("${ly.page.path}")
    public String pagePath;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private SpecificationClient specificationClient;

    public Map<String, Object> loadModel(Long id) {
        Map<String, Object> data = new HashMap<>();
        // 1、Spu
        Spu spu = this.goodsClient.querySpuById(id);
        Long spuId = spu.getId();
        // 2、SpuDetail
        SpuDetail detail = this.goodsClient.querySpuDetailBySpuId(spuId);
        // 3、SkuList
        List<Sku> skus = this.goodsClient.querySkuList(spuId);
        // 4、商品分类
        List<Category> categories = getCategories(spu);
        // 5、品牌
        Brand brand = this.brandClient.queryBrands(
                Arrays.asList(spu.getBrandId())).get(0);
        // 6、规格参数
        List<SpecGroup> specGroups = this.specificationClient.querySpecGroupAndParam(spu.getCid3());

        // 填充模型
        data.put("spu", spu);
        data.put("detail", detail);
        data.put("skus", skus);
        data.put("categories", categories);
        data.put("brand", brand);
        data.put("specGroups", specGroups);
        return data;
    }

    private List<Category> getCategories(Spu spu) {
        List<Long> ids = Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3());
        List<String> names = this.categoryClient.queryNamesByIds(ids);
        List<Category> list = new ArrayList<>();
        for (int i = 0; i < ids.size(); i++) {
            Category c = new Category();
            c.setId(ids.get(i));
            c.setName(names.get(i));
            list.add(c);
        }
        return list;
    }

    @Autowired
    private TemplateEngine templateEngine;

    private static final Logger logger = LoggerFactory.getLogger(PageService.class);

    public void createHtml(Long id) {
        PrintWriter writer = null;
        File bak = new File(id + ".bak.html");
        File dest = new File(pagePath, id + ".html");
        try {
            // 加载模型数据
            Map<String, Object> map = this.loadModel(id);
            // 创建上下文
            Context context = new Context();
            context.setVariables(map);

            // 如果存在，先备份
            if(dest.exists()){
                dest.renameTo(bak);
            }

            // 创建输出流
            writer = new PrintWriter(dest, "UTF-8");
            // 写出数据
            templateEngine.process("item", context, writer);
            // 删除备份
            bak.deleteOnExit();

        } catch (Exception e) {
            // 记录日志信息
            logger.error("创建静态页面出错",e);
            // 恢复备份
            bak.renameTo(dest);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    public void asyncCreateHtml(Long id) {
        try {
            ThreadUtils.execute(() -> createHtml(id));
        }catch (Exception e){
            logger.error("创建静态页面出错",e);
        }
    }

    public void deleteHtml(Long id) {
        File dest = new File(pagePath, id + ".html");
        dest.deleteOnExit();
    }
}
