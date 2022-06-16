package Dot.Service;

import Dot.Mapper.BeanMapper;
import Dot.Model.BeanModel;
import Dot.Model.Page;
import genMVC.utils.Utility;
import genMVC.controller.Inject;
import genMVC.service.Service;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

@Service
public class BeanService {
    @Inject
    BeanMapper beanMapper;

    public List<BeanModel> all() {
        return beanMapper.all();
    }

    public Page<BeanModel> all(Integer currentPage, Integer pageSize, Integer userId) {
        Page<BeanModel> p = new Page();
        // 页大小
        p.setPageSize(pageSize);
        // 查询总行数
        Integer countInteger = this.findByUserId(userId).size();
        // 总页数
        Integer max = countInteger % pageSize == 0 ? countInteger / pageSize : countInteger / pageSize + 1;
        p.setPageTotalCount(countInteger);
        p.setPageTotal(max);
        p.setPageNo(currentPage);
        // 查询数据
        Utility.log("p: %s", p.toString());
        List<BeanModel> list = beanMapper.all(p.getPageNo(), p.getPageSize(), userId);
        p.setItems(list);
        return p;
    }

    public Page<BeanModel> all(Integer currentPage, Integer pageSize, Integer userId, String content) {
        Utility.log("all content", content);
        Page<BeanModel> p = new Page();
        // 页大小
        p.setPageSize(pageSize);
        // 查询总行数
        Integer countInteger = this.findByUserIdAndContent(userId, content).size();
        // 总页数
        Integer max = countInteger % pageSize == 0 ? countInteger / pageSize : countInteger / pageSize + 1;
        p.setPageTotalCount(countInteger);
        p.setPageTotal(max);
        p.setPageNo(currentPage);
        // 查询数据
        List<BeanModel> list = beanMapper.all(p.getPageNo(), p.getPageSize(), userId, content);
        p.setItems(list);
        return p;
    }

    public List<BeanModel> findByUserId(Integer userId) {
        return beanMapper.findByUserId(userId);
    }

    public List<BeanModel> findByUserIdAndContent(Integer userId, String content) {
        return beanMapper.findByUserIdAndContent(userId, content);
    }

    public BeanModel findByBeanId(Integer beanId) {
        List<BeanModel> res = beanMapper.findByBeanId(beanId);
        if (res.size() == 0) {
            return null;
        }
        return res.get(0);
    }

    public void updateByBeanIdAndContent(Integer beanId, String content) {
        BeanModel b = new BeanModel();
        b.id = beanId;
        b.content = content;
        b.updatedTime = System.currentTimeMillis() / 1000L;
        beanMapper.updateById(b);
    }

    public void update(BeanModel b) {
        b.updatedTime = System.currentTimeMillis() / 1000L;
        beanMapper.updateById(b);
    }

    public Integer add(BeanModel beanModel) {
        long time = System.currentTimeMillis() / 1000L;
        beanModel.createdTime = time;
        beanModel.updatedTime = time;
        String selfId = UUID.randomUUID().toString();
        beanModel.selfId = selfId;
        return Integer.valueOf(String.valueOf(beanMapper.add(beanModel)));
    }

}
