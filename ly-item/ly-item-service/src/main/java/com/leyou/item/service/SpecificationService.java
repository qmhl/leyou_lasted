package com.leyou.item.service;

import com.leyou.item.mapper.SpecGroupMapper;
import com.leyou.item.mapper.SpecParamMapper;
import com.leyou.item.pojo.SpecGroup;
import com.leyou.item.pojo.SpecParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author: HuYi.Zhang
 * @create: 2018-06-26 16:52
 **/
@Service
public class SpecificationService {

    @Autowired
    private SpecGroupMapper specGroupMapper;

    @Autowired
    private SpecParamMapper specParamMapper;


    public List<SpecGroup> querySpecGroupsByCid(Long cid) {
        SpecGroup t = new SpecGroup();
        t.setCid(cid);
        return this.specGroupMapper.select(t);
    }

    public List<SpecParam> querySpecParams(Long gid, Long cid, Boolean generic, Boolean searching) {
        SpecParam t = new SpecParam();
        t.setGroupId(gid);
        t.setCid(cid);
        t.setGeneric(generic);
        t.setSearching(searching);
        return this.specParamMapper.select(t);
    }

    public List<SpecGroup> querySpecGroupAndParam(Long cid) {
        List<SpecGroup> groups = this.querySpecGroupsByCid(cid);
        for (SpecGroup group : groups) {
            List<SpecParam> params = this.querySpecParams(
                    group.getId(), null, null, null);
            group.setParams(params);
        }
        return groups;
    }
}
