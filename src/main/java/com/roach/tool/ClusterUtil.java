package com.roach.tool;

import com.alibaba.fastjson.JSON;
import com.roach.base.bean.Connect;
import com.roach.base.bean.KeyBean;
import com.roach.base.bean.RedisNode;
import com.roach.base.bean.Relation;
import org.springframework.util.StringUtils;
import redis.clients.jedis.*;

import java.util.*;

import static com.roach.tool.DataUtil.getCurrentOpenConnect;
import static com.roach.tool.StringUtil.FLAG_COLON;

/**
 * 集群工具类
 *
 * @author jdktomcat
 */
public class ClusterUtil {

    /**
     * 可用连接实例的最大数目，默认值为8
     */
    private static int MAX_TOTAL = 50;

    /**
     * 控制一个pool最多有多少个状态为idle(空闲的)的jedis实例，默认值也是8。
     */
    private static int MAX_IDLE = 10;

    /**
     * 等待可用连接的最大时间，单位毫秒，默认值为-1，表示永不超时。如果超过等待时间，则直接抛出JedisConnectionException；
     */
    private static int MAX_WAIT = 5000;

    /**
     * 超时时间
     */
    private static final int TIME_OUT = 10000;

    /**
     * 在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
     */
    private static boolean TEST_ON_BORROW = true;

    private static boolean TEST_ON_RETURN = true;

    private static JedisCluster cluster;


    public static JedisCluster openCluster(Connect connect) throws Exception {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxTotal(MAX_TOTAL);
        config.setMaxIdle(MAX_IDLE);
        config.setMaxWaitMillis(MAX_WAIT);
        config.setTestOnBorrow(TEST_ON_BORROW);
        config.setTestOnReturn(TEST_ON_RETURN);
        Set<HostAndPort> nodes = new LinkedHashSet<>();
        List<RedisNode> nodeList = getClusterNode(connect);
        for (RedisNode node : nodeList) {
            String host = StringUtil.getKeyString(FLAG_COLON, node.getAddr());
            String port = StringUtil.getValueString(FLAG_COLON, node.getAddr());
            nodes.add(new HostAndPort(host, Integer.valueOf(port)));
        }
        String pass = connect.getRpass();
        if (StringUtils.isEmpty(pass)) {
            cluster = new JedisCluster(nodes, TIME_OUT, config);
        } else {
            cluster = new JedisCluster(nodes, 1000, 1000, 1, pass, config);
        }
        return cluster;
    }


    public static void closeCluster() {
        try {
            if (cluster != null) {
                cluster.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static JedisCluster getCluster(Connect connect) throws Exception {
        if (null == cluster || cluster.getClusterNodes().size() == 0) {
            return openCluster(connect);
        } else {
            return cluster;
        }
    }

    public static List<RedisNode> getClusterNode(Connect connect) throws Exception {
        List<RedisNode> nodeList = new ArrayList<>();
        Jedis jedis = null;
        if ("1".equals(connect.getType())) {
            jedis = new Jedis(connect.getRhost(), 55555);
        } else {
            jedis = new Jedis(connect.getRhost(), Integer.valueOf(connect.getRport()));
        }
        if (!StringUtils.isEmpty(connect.getRpass())) {
            jedis.auth(connect.getRpass());
        }
        String clusterNodes = jedis.clusterNodes();
        String[] nodes = clusterNodes.split("\n");
        for (String node : nodes) {
            String[] nodeFileds = node.split(" ");
            RedisNode redisNode = new RedisNode();
            redisNode.setId(nodeFileds[0]);
            redisNode.setAddr(nodeFileds[1]);
            redisNode.setFlag(nodeFileds[2]);
            redisNode.setPid(nodeFileds[3]);
            redisNode.setPing(nodeFileds[4]);
            redisNode.setPong(nodeFileds[5]);
            redisNode.setEpoch(nodeFileds[6]);
            redisNode.setState(nodeFileds[7]);
            if (nodeFileds.length == 9) {
                redisNode.setHash(nodeFileds[8]);
            }
            if (redisNode.getAddr().contains("@")) {
                redisNode.setAddr(redisNode.getAddr().split("@")[0]);
            }
            nodeList.add(redisNode);
        }
        jedis.close();
        return nodeList;
    }

    public static Map<String, RedisNode> getMasterNode(List<RedisNode> nodeList) {
        Map<String, RedisNode> nodeMap = new HashMap<>();
        for (RedisNode node : nodeList) {
            if (node.getFlag().indexOf("master") > -1) {
                nodeMap.put(node.getAddr(), node);
            }
        }
        return nodeMap;
    }


    public static Jedis getMasterSelf() {
        JedisCluster jedisCluster = DataUtil.getJedisClusterObject();
        if (null != jedisCluster) {
            Connect connect = getCurrentOpenConnect();
            Map<String, JedisPool> clusterNodes = jedisCluster.getClusterNodes();
            for (String nk : clusterNodes.keySet()) {
                if (connect.getType().equals("0") && nk.contains(connect.getRhost())) {
                    return clusterNodes.get(nk).getResource();
                }
                if (connect.getType().equals("1") && nk.contains(connect.getShost())) {
                    return clusterNodes.get(nk).getResource();
                }
            }
        }
        return null;
    }


    public static boolean isCluster(Connect connect) {
        boolean isCulter = false;
        Jedis jedis = null;
        try {
            String pass = connect.getRpass();
            if ("1".equals(connect.getType())) {
                JschUtil.openSSH(connect);
                jedis = new Jedis(connect.getRhost(), 55555);
            } else {
                jedis = new Jedis(connect.getRhost(), Integer.valueOf(connect.getRport()));
            }
            if (!StringUtils.isEmpty(pass)) {
                jedis.auth(pass);
            }
            String serverInfo = jedis.info("server");
            String[] server = serverInfo.split("\n");
            for (String info : server) {
                String key = StringUtil.getKeyString(FLAG_COLON, info);
                String value = StringUtil.getValueString(FLAG_COLON, info);
                if (key.equals("redis_mode") && value.equals("cluster\r")) {
                    isCulter = true;
                }
            }
            if (null != jedis) {
                jedis.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return isCulter;
    }


    /**
     * 判断key是否存在
     */
    public static boolean existsKey(JedisCluster jedisCluster, String key) {
        return jedisCluster.exists(key);
    }


    /**
     * 重命名key
     */
    public static void renameKey(JedisCluster jedisCluster, String oldKey, String newKey) {
        String type = jedisCluster.type(oldKey);
        switch (type) {
            case "set":
                Set<String> set = jedisCluster.smembers(oldKey);
                for (String temp : set) {
                    jedisCluster.sadd(newKey, temp);
                }
                break;
            case "none":
                break;
            case "list":
                List<String> list = jedisCluster.lrange(oldKey, 0, -1);
                for (String temp : list) {
                    jedisCluster.lpush(newKey, temp);
                }
                break;
            case "zset":
                List<String> zset = new ArrayList<>(jedisCluster.zrange(oldKey, 0, -1));
                for (String temp : zset) {
                    jedisCluster.zadd(newKey, zset.indexOf(temp) + 1, temp);
                }
                break;
            case "hash":
                jedisCluster.hmset(newKey, jedisCluster.hgetAll(oldKey));
                break;
            case "string":
                String strs = jedisCluster.get(oldKey);
                jedisCluster.set(newKey, strs);
                break;
        }
    }

    /**
     * 设置key时间
     */
    public static long retimeKey(JedisCluster jedisCluster, String key, int time) {
        return jedisCluster.expire(key, time);
    }

    /**
     * 删除key
     */
    public static long deleteKey(JedisCluster jedisCluster, String key) {
        return jedisCluster.del(key);
    }

    /**
     * 修改String的Value
     */
    public static String updateStr(JedisCluster jedisCluster, String key, String val) {
        return jedisCluster.set(key, val);
    }

    /**
     * 添加Set的item
     */
    public static long insertSet(JedisCluster jedisCluster, String key, String val) {
        return jedisCluster.sadd(key, val);
    }

    /**
     * 添加Zset的item
     */
    public static long insertZset(JedisCluster jedisCluster, String key, String val) {
        return jedisCluster.zadd(key, 1, val);
    }

    /**
     * 添加List的item
     */
    public static long insertList(JedisCluster jedisCluster, String key, String val) {
        return jedisCluster.rpush(key, val);
    }

    /**
     * 添加Hase的key和val
     */
    public static long insertHash(JedisCluster jedisCluster, String key, String mapKey, String mapVal) {
        return jedisCluster.hset(key, mapKey, mapVal);
    }

    /**
     * 删除Set的item
     */
    public static long deleteSet(JedisCluster jedisCluster, String key, String val) {
        return jedisCluster.srem(key, val);
    }

    /**
     * 删除Zset的item
     */
    public static long deleteZset(JedisCluster jedisCluster, String key, String val) {
        return jedisCluster.zrem(key, val);
    }

    /**
     * 删除List的item
     */
    public static long deleteList(JedisCluster jedisCluster, String key, long keyIndex) {
        String tempItem = KeyUtil.getUUIDKey();
        jedisCluster.lset(key, keyIndex, tempItem);
        return jedisCluster.lrem(key, 0, tempItem);
    }

    /**
     * 删除List的item
     */
    public static long deleteHash(JedisCluster jedisCluster, String key, String mapKey) {
        return jedisCluster.hdel(key, mapKey);
    }

    /**
     * 还原数据
     */
    public static void recoveryKey(JedisCluster jedisCluster, String jsonStr) {
        String[] jsons = jsonStr.split("\r\n", -1);
        for (String json : jsons) {
            KeyBean keyBean = JsonUtil.parseKeyBeanObject(json);
            if (null != keyBean) {
                String keys = keyBean.getKey();
                String type = keyBean.getType();
                String data = JSON.toJSONString(keyBean.getData());
                switch (type) {
                    //set (集合)
                    case "set":
                        List sets = JSON.parseObject(data, List.class);
                        for (Object setTemp : sets) {
                            jedisCluster.sadd(keys, setTemp.toString());
                        }
                        break;
                    //list (列表)
                    case "list":
                        List lists = JSON.parseObject(data, List.class);
                        for (Object listTemp : lists) {
                            jedisCluster.lpush(keys, listTemp.toString());
                        }
                        break;
                    //zset (有序集)
                    case "zset":
                        List zsets = JSON.parseObject(data, List.class);
                        for (Object zsetTemp : zsets) {
                            jedisCluster.zadd(keys, zsets.indexOf(zsetTemp) + 1, zsetTemp.toString());
                        }
                        break;
                    //hash (哈希表)
                    case "hash":
                        Map map = JSON.parseObject(data, Map.class);
                        jedisCluster.hmset(keys, map);
                        break;
                    //string (字符串)
                    case "string":
                        jedisCluster.set(keys, keyBean.getData().toString());
                        break;
                }
            }
        }
    }

    //导出数据
    public static String backupKey(Jedis jedis, String pattern) {
        StringBuffer dataBuffer = new StringBuffer("");
        if (StringUtils.isEmpty(pattern)) {
            pattern = "*";
        }
        Object dataObj = null;
        Set<String> keySet = jedis.keys(pattern);
        if (null != keySet) {
            for (String key : keySet) {
                KeyBean keyBean = new KeyBean();
                keyBean.setKey(key);
                keyBean.setType(jedis.type(key));
                keyBean.setTtl(jedis.ttl(key));
                switch (keyBean.getType()) {
                    //set (集合)
                    case "set":
                        dataObj = jedis.smembers(key);
                        break;
                    //list (列表)
                    case "list":
                        dataObj = jedis.lrange(key, 0, -1);

                        break;
                    //zset (有序集)
                    case "zset":
                        dataObj = jedis.zrange(key, 0, -1);
                        break;
                    //hash (哈希表)
                    case "hash":
                        dataObj = jedis.hgetAll(key);
                        break;
                    //string (字符串)
                    case "string":
                        dataObj = jedis.get(key);
                        break;
                }
                keyBean.setData(dataObj);
                dataBuffer.append(JSON.toJSONString(keyBean));
                dataBuffer.append("\r\n");
            }
        }
        return dataBuffer.toString();
    }

    /**
     * 获取Redis Key信息
     */
    public static KeyBean getKeyInfo(JedisCluster jedisCluster, String key) {
        KeyBean keyBean = new KeyBean();
        keyBean.setKey(key);
        keyBean.setType(jedisCluster.type(key));
        keyBean.setTtl(jedisCluster.ttl(key));
        //none (key不存在)
        //string (字符串)
        //list (列表)
        //set (集合)
        //zset (有序集)
        //hash (哈希表)
        switch (keyBean.getType()) {
            case "set":
                Set<String> set = jedisCluster.smembers(key);
                StringBuffer setBuf = new StringBuffer();
                for (String info : set) {
                    setBuf.append(info).append(",");
                }
                String textSet = setBuf.toString();
                keyBean.setText(textSet.substring(0, textSet.length() - 1));
                keyBean.setJson(JSON.toJSONString(set));
                keyBean.setRaws(keyBean.getText().replace(",", "</br>"));
                break;
            case "none":
                keyBean.setText("");
                break;
            case "list":
                List<String> list = jedisCluster.lrange(key, 0, -1);
                StringBuffer listBuf = new StringBuffer();
                for (String info : list) {
                    listBuf.append(info).append(",");
                }
                String textList = listBuf.toString();
                keyBean.setText(textList.substring(0, textList.length() - 1));
                keyBean.setJson(JSON.toJSONString(list));
                keyBean.setRaws(keyBean.getText().replace(",", "</br>"));
                break;
            case "zset":
                Set<String> zset = jedisCluster.zrange(key, 0, -1);
                StringBuffer zsetBuf = new StringBuffer();
                for (String info : zset) {
                    zsetBuf.append(info).append(",");
                }
                String textZset = zsetBuf.toString();
                keyBean.setText(textZset.substring(0, textZset.length() - 1));
                keyBean.setJson(JSON.toJSONString(zset));
                keyBean.setRaws(keyBean.getText().replace(",", "</br>"));
                break;
            case "hash":
                Map<String, String> map = jedisCluster.hgetAll(key);
                StringBuffer mapBuf = new StringBuffer();
                for (Map.Entry entry : map.entrySet()) {
                    mapBuf.append(entry.getKey()).append(":").append(entry.getValue()).append(",");
                }
                String textMap = mapBuf.toString();
                keyBean.setText(textMap.substring(0, textMap.length() - 1));
                keyBean.setJson(JSON.toJSONString(map));
                keyBean.setRaws(keyBean.getText().replace(",", "</br>"));
                break;
            case "string":
                keyBean.setText(jedisCluster.get(key));
                keyBean.setJson(JSON.toJSONString(keyBean.getText()));
                keyBean.setRaws(keyBean.getText());
                break;
        }
        keyBean.setSize(keyBean.getText().getBytes().length);
        return keyBean;
    }


    public static List<Relation> getClusterRelation(String info) {
        List<Relation> relations = new ArrayList<>();
        List<String> masters = new ArrayList<>();
        List<String> slaves = new ArrayList<>();
        String[] nodes = info.split("\n");
        for (String node : nodes) {
            if (node.contains("master")) {
                masters.add(node);
            } else {
                slaves.add(node);
            }
        }
        for (String master : masters) {
            String[] masterNode = master.split(" ");
            Relation relationMaster = new Relation();
            relationMaster.setNode(masterNode[0]);
            relationMaster.setHost(masterNode[1]);
            relationMaster.setRole("主节点");
            if (masterNode[7].equals("connected")) {
                relationMaster.setFlag("已连接");
            } else {
                relationMaster.setFlag("已断开");
            }
            relationMaster.setSlot(masterNode[8]);
            relations.add(relationMaster);
            for (String slave : slaves) {
                String[] slaveNode = slave.split(" ");
                if (slaveNode[3].equals(masterNode[0])) {
                    Relation relationSlave = new Relation();
                    relationSlave.setNode(slaveNode[0]);
                    relationSlave.setHost(slaveNode[1]);
                    relationSlave.setRole("从节点");
                    if (slaveNode[7].equals("connected")) {
                        relationSlave.setFlag("已连接");
                    } else {
                        relationSlave.setFlag("已断开");
                    }
                    relations.add(relationSlave);
                }
            }
        }
        return relations;
    }

    public static void testClusterData() {
        /*-------------------String Test------------------------*/
        cluster.set("testString1", "testString1");
        cluster.set("testString2", "testString2");
        /*-------------------List Test--------------------------*/
        cluster.del("testList");
        cluster.lpush("testList", "list01");
        cluster.lpush("testList", "list02");
        cluster.lpush("testList", "list03");
        cluster.lpush("testList", "list04");
        cluster.lpush("testList", "list05");
        /*-------------------Map Test---------------------------*/
        Map map = new HashMap();
        map.put("map01", "map01-value");
        map.put("map02", "map02-value");
        map.put("map03", "map03-value");
        map.put("map04", "map04-value");
        map.put("map05", "map05-value");
        cluster.hmset("testMap", map);
        /*-------------------Set Test---------------------------*/
        cluster.sadd("testSet", "set-value01");
        cluster.sadd("testSet", "set-value02");
        cluster.sadd("testSet", "set-value03");
        cluster.sadd("testSet", "set-value04");
        cluster.sadd("testSet", "set-value05");
        /*-------------------Zset Test--------------------------*/
        cluster.zadd("testZset", 1, "set-value01");
        cluster.zadd("testZset", 2, "set-value02");
        cluster.zadd("testZset", 3, "set-value03");
        cluster.zadd("testZset", 4, "set-value04");
        cluster.zadd("testZset", 5, "set-value05");
    }


    public static void main(String[] args) throws Exception {
        Connect connect = new Connect();
        connect.setRport("7001");
        connect.setRhost("127.0.0.1");
        connect.setType("0");
        openCluster(connect);
        testClusterData();
    }
}
