package mao.spring_boot_redis_hmdp;

import mao.spring_boot_redis_hmdp.entity.Shop;
import mao.spring_boot_redis_hmdp.service.IShopService;
import mao.spring_boot_redis_hmdp.utils.RedisConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@SpringBootTest
class SpringBootRedisHmdpRealizeTheFunctionOfQueryingNearbyShopsApplicationTests
{

    @Resource
    private IShopService shopService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void contextLoads()
    {
    }

    @Test
    void load()
    {
        //查询店铺信息
        List<Shop> list = shopService.list();
        //店铺分组，放入到一个集合中
        Map<Long, List<Shop>> collect = list.stream().collect(Collectors.groupingBy(Shop::getTypeId));
        //分批写入redis
        for (Long typeId : collect.keySet())
        {
            //值
            List<Shop> shops = collect.get(typeId);
            List<RedisGeoCommands.GeoLocation<String>> locations = new ArrayList<>(shops.size());
            for (Shop shop : shops)
            {
                locations.add(new RedisGeoCommands.GeoLocation<>
                        (shop.getId().toString(), new Point(shop.getX(), shop.getY())));
            }
            //写入redis
            stringRedisTemplate.opsForGeo().add(RedisConstants.SHOP_GEO_KEY + typeId, locations);
        }
    }
}
