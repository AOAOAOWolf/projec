package com.leyou.item.service;

import com.leyou.common.exception.pojo.ExceptionEnum;
import com.leyou.common.exception.pojo.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    public List<Category> findCategoryByPid(Long pid) {
        //封装条件
        Category record = new Category();
        record.setParentId(pid);
        //根据条件做查询
        List<Category> list = categoryMapper.select(record);
        //判空
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public List<Category> findCategorysByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        if(CollectionUtils.isEmpty(list)){
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return list;
    }

    public Map<String,List<CategoryDTO>> findCategorysAll(){
        Map<String,List<CategoryDTO>> all = new HashMap<>();


        if (redisTemplate.hasKey("test")){
            BoundHashOperations<String, String, String> cartHashMap = redisTemplate.boundHashOps("test");

            cartHashMap.keys().forEach(str->{
                System.out.println(str);
                String s = cartHashMap.get(str);
                List<CategoryDTO> list = JsonUtils.toList(s, CategoryDTO.class);
                all.put(str,list);
            });
            return all;
        }

        BoundHashOperations<String, String, String> cartHashMap = redisTemplate.boundHashOps("test");

        Category first = new Category();
        first.setParentId(0L);
        List<Category> firstList = categoryMapper.select(first);

        firstList.forEach(a->{
            Category second = new Category();
            second.setParentId(a.getId());
            List<Category> secondList= categoryMapper.select(second);

            List<CategoryDTO> list= new ArrayList<>();

            secondList.forEach(b->{

                Category third = new Category();
                third.setParentId(b.getId());
                List<Category> select = categoryMapper.select(third);
                CategoryDTO dto = new CategoryDTO();
                dto.setName(b.getName());
                dto.setList(select);

                list.add(dto);

            });
            cartHashMap.put(a.getName(),JsonUtils.toString(list));
            all.put(a.getName(),list);

        });


       return all;
    }

}
