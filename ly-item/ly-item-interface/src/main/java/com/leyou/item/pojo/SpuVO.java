package com.leyou.item.pojo;

import javax.persistence.Transient;
import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-26 18:37
 **/
public class SpuVO extends Spu {

    @Transient
    private String cname;
    @Transient
    private String bname;
    @Transient
    private SpuDetail spuDetail;
    @Transient
    private List<Sku> skus;

    public String getCname() {
        return cname;
    }

    public void setCname(String cname) {
        this.cname = cname;
    }

    public String getBname() {
        return bname;
    }

    public void setBname(String bname) {
        this.bname = bname;
    }

    public SpuDetail getSpuDetail() {
        return spuDetail;
    }

    public void setSpuDetail(SpuDetail spuDetail) {
        this.spuDetail = spuDetail;
    }

    public List<Sku> getSkus() {
        return skus;
    }

    public void setSkus(List<Sku> skus) {
        this.skus = skus;
    }
}
