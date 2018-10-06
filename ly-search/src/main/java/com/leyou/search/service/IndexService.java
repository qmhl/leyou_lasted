package com.leyou.search.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.utils.NumberUtils;
import com.leyou.item.pojo.*;
import com.leyou.search.client.BrandClient;
import com.leyou.search.client.CategoryClient;
import com.leyou.search.client.GoodsClient;
import com.leyou.search.client.SpecificationClient;
import com.leyou.search.pojo.Goods;
import com.leyou.search.pojo.SearchRequest;
import com.leyou.search.pojo.SearchResult;
import com.leyou.search.repository.GoodsRepository;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.histogram.Histogram;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.StringTerms;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsExtractor;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-30 16:54
 **/
@Service
public class IndexService {

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private SpecificationClient specificationClient;

    @Autowired
    private GoodsRepository repository;

    @Autowired
    private BrandClient brandClient;

    @Autowired
    private ElasticsearchTemplate template;

    public Goods buildGoods(Spu spu) {
        Long id = spu.getId();
        // 1、准备数据
        // sku集合
        List<Sku> skus = this.goodsClient.querySkuList(id);
        // spuDetail
        SpuDetail detail = this.goodsClient.querySpuDetailBySpuId(id);
        // 商品分类
        List<String> names = this.categoryClient.queryNamesByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
        // 查询规格参数
        List<SpecParam> params = this.specificationClient.querySpecParams(
                null, spu.getCid3(), null, true);
        // TODO 查询品牌名称

        // 处理sku
        List<Long> prices = new ArrayList<>();
        List<Map<String, Object>> skuList = new ArrayList<>();
        for (Sku sku : skus) {
            prices.add(sku.getPrice());
            Map<String, Object> map = new HashMap<>();
            map.put("id", sku.getId());
            map.put("title", sku.getTitle());
            map.put("image", StringUtils.isBlank(sku.getImages()) ? "" : sku.getImages().split(",")[0]);
            map.put("price", sku.getPrice());
            skuList.add(map);
        }

        // 处理规格参数
        Map<Long, String> genericMap = JsonUtils.parseMap(detail.getGenericSpec(), Long.class, String.class);
        Map<Long, List<String>> specialMap = JsonUtils.nativeRead(
                detail.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
                });

        Map<String, Object> specs = new HashMap<>();
        for (SpecParam param : params) {
            if (param.getGeneric()) {
                // 通用参数
                String value = genericMap.get(param.getId());
                if (param.getNumeric()) {
                    // 数值类型，需要存储一个分段
                    value = this.chooseSegment(value, param);
                }
                specs.put(param.getName(), value);
            } else {
                // 特有参数
                specs.put(param.getName(), specialMap.get(param.getId()));
            }
        }

        Goods goods = new Goods();
        goods.setBrandId(spu.getBrandId());
        goods.setCid1(spu.getCid1());
        goods.setCid2(spu.getCid2());
        goods.setCid3(spu.getCid3());
        goods.setCreateTime(spu.getCreateTime());
        goods.setId(id);
        goods.setSubTitle(spu.getSubTitle());
        // 搜索条件 拼接：标题、分类、品牌
        goods.setAll(spu.getTitle() + " " + StringUtils.join(names, " "));
        goods.setPrice(prices);
        goods.setSkus(JsonUtils.serialize(skuList));
        goods.setSpecs(specs);

        return goods;
    }

    private String chooseSegment(String value, SpecParam p) {
        double val = NumberUtils.toDouble(value);
        String result = "其它";
        // 保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            // 获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            // 判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }

    public SearchResult search(SearchRequest request) {
        String key = request.getKey();
        if (StringUtils.isBlank(key)) {
            // 如果用户没搜索条件，我们可以给默认的，或者返回null
            return null;
        }

        Integer page = request.getPage() - 1;// page 从0开始
        Integer size = request.getSize();

        // 1、创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        // 2、查询
        // 2.1、对结果进行筛选
        queryBuilder.withSourceFilter(new FetchSourceFilter(new String[]{"id", "skus", "subTitle"}, null));
        // 2.2、基本查询
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);

        // 2.3、分页
        queryBuilder.withPageable(PageRequest.of(page, size));

        // 2.4、聚合
        String categoryAggName = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAggName).field("cid3"));
        String brandAggName = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAggName).field("brandId"));

        // 3、返回结果
        AggregatedPage<Goods> result = (AggregatedPage<Goods>) this.repository.search(queryBuilder.build());

        // 4、解析结果
        // 4.1.普通分页结果
        long total = result.getTotalElements();
        long totalPage = (total + size - 1) / size;

        // 4.2、解析聚合结果

        // 解析分类结果
        List<Category> categories = parseCategory(result.getAggregation(categoryAggName));
        // 解析品牌的结果
        List<Brand> brands = parseBrand(result.getAggregation(brandAggName));

        // 5、判断是否需要对规格进行聚合
        List<Map<String, Object>> specs = null;
        if (categories.size() == 1) {
            specs = getSpecAgg(categories.get(0).getId(), basicQuery);
        }

        return new SearchResult(total, totalPage, result.getContent(), categories, brands, specs);
    }

    private QueryBuilder buildBasicQuery(SearchRequest request) {
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        // 基本查询条件
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()));
        // 过滤
        BoolQueryBuilder filterBuilder = QueryBuilders.boolQuery();
        // 过滤条件
        Map<String, String> filters = request.getFilter();
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            String key = entry.getKey();
            if(!"cid3".equals(key) && !"brandId".equals(key)){
                key = "specs." + key + ".keyword";
            }
            filterBuilder.must(QueryBuilders.termQuery(key,entry.getValue()));
        }

        queryBuilder.filter(filterBuilder);
        return queryBuilder;
    }

    private List<Map<String, Object>> getSpecAgg(Long cid, QueryBuilder basicQuery) {
        // 1、根据分类查找可搜索的规格参数
        List<SpecParam> params = this.specificationClient.querySpecParams(
                null, cid, null, true);
        // 2、对规格参数聚合
        // 2.1、创建查询构建器
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withPageable(PageRequest.of(0,1));
        // 2.2、添加过滤条件
        queryBuilder.withQuery(basicQuery);
        // 2.3、添加聚合
        for (SpecParam param : params) {
            String name = param.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name + ".keyword"));
        }

        // 3、查询
        Map<String, Aggregation> aggMap = this.template.query(queryBuilder.build(),
                response -> response.getAggregations()).asMap();

        // 4、解析聚合结果
        List<Map<String, Object>> specs = new ArrayList<>();
        for (SpecParam param : params) {
            // 根据参数名取聚合结果
            StringTerms terms = (StringTerms) aggMap.get(param.getName());
            Map<String,Object> map = new HashMap<>();
            map.put("k", param.getName());
            map.put("options", terms.getBuckets().stream().map(b -> b.getKeyAsString()));
            specs.add(map);
        }
        return specs;
    }

    private List<Brand> parseBrand(Aggregation agg) {
        LongTerms terms = (LongTerms) agg;
        // 解析商品分类
        List<Long> ids = terms.getBuckets().stream()
                .map(b -> b.getKeyAsNumber().longValue()).collect(Collectors.toList());
        // 根据品牌id查询所有的品牌
        return this.brandClient.queryBrands(ids);
    }

    private List<Category> parseCategory(Aggregation agg) {
        LongTerms terms = (LongTerms) agg;
        return terms.getBuckets().stream().map(b -> {
            long id = b.getKeyAsNumber().longValue();
            List<String> names = this.categoryClient.queryNamesByIds(Arrays.asList(id));
            Category c = new Category();
            c.setId(id);
            c.setName(names.get(0));
            return c;
        }).collect(Collectors.toList());
    }

    public void deleteIndex(Long id) {
        this.repository.deleteById(id);
    }

    public void createOrUpdateIndex(Long id) {
        // 查询spu
        Spu spu = this.goodsClient.querySpuById(id);
        // 构建goods
        Goods goods = this.buildGoods(spu);
        // 写入索引库
        this.repository.save(goods);
    }
}
