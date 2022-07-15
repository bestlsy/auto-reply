package com.tencent.wxcloudrun.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tencent.wxcloudrun.config.ApiResponse;
import com.tencent.wxcloudrun.dto.CounterRequest;
import com.tencent.wxcloudrun.dto.HttpUtils;
import com.tencent.wxcloudrun.dto.ResultDTO;
import com.tencent.wxcloudrun.model.Counter;
import com.tencent.wxcloudrun.service.CounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * counter控制器
 */
@RestController
public class CounterController {

  final CounterService counterService;
  final Logger logger;

  final Map<Object, Object> cache = new ConcurrentHashMap<>();

  public CounterController(@Autowired CounterService counterService) {
    this.counterService = counterService;
    this.logger = LoggerFactory.getLogger(CounterController.class);
  }

  @PostMapping("/api/message")
  public Object message(@RequestBody Map<String,Object> map) throws Exception{
    Map<String, Object> result = new HashMap<>();
    Object msgId = cache.get(map.get("MsgId"));
    if (!ObjectUtils.isEmpty(msgId)) {
      return "success";
    }
    cache.put(map.get("MsgId"), map);
    result.put("ToUserName", map.get("FromUserName"));
    result.put("FromUserName", map.get("ToUserName"));
    result.put("CreateTime", map.get("CreateTime"));
    result.put("MsgType", "text");
    result.put("Content", getMessage());
    String mapJson = getJson(map);
    String resultJson = getJson(result);
    logger.info("入参数 "+mapJson.toString());
    logger.info("出参数 " + resultJson.toString());
    return result;
  }

  private String getJson(Object ob) {
    ObjectMapper mapper = new ObjectMapper();
    try {
      String value = mapper.writeValueAsString(ob);
      return value;
    } catch (JsonProcessingException e) {
      throw new RuntimeException(e);
    }
  }

  private String getMessage() throws Exception{
    String json = HttpUtils.get("https://api.dzzui.com/api/qinghua?format=json");
    ObjectMapper mapper = new ObjectMapper();
    ResultDTO readValue = mapper.readValue(new StringReader(json), ResultDTO.class);
    if (readValue != null && readValue.getCode() == 1) {
      return readValue.getText();
    }
    return "生产队的驴也不能这么干呀，让我休息一会吧～";
  }

  /**
   * {
   *   "ToUserName": "用户OPENID",
   *   "FromUserName": "公众号/小程序原始ID",
   *   "CreateTime": "发送时间", // 整型，例如：1648014186
   *   "MsgType": "text",
   *   "Content": "文本消息"
   * }
   */


  /**
   * 获取当前计数
   * @return API response json
   */
  @GetMapping(value = "/api/count")
  ApiResponse get() {
    logger.info("/api/count get request");
    Optional<Counter> counter = counterService.getCounter(1);
    Integer count = 0;
    if (counter.isPresent()) {
      count = counter.get().getCount();
    }

    return ApiResponse.ok(count);
  }


  /**
   * 更新计数，自增或者清零
   * @param request {@link CounterRequest}
   * @return API response json
   */
  @PostMapping(value = "/api/count")
  ApiResponse create(@RequestBody CounterRequest request) {
    logger.info("/api/count post request, action: {}", request.getAction());

    Optional<Counter> curCounter = counterService.getCounter(1);
    if (request.getAction().equals("inc")) {
      Integer count = 1;
      if (curCounter.isPresent()) {
        count += curCounter.get().getCount();
      }
      Counter counter = new Counter();
      counter.setId(1);
      counter.setCount(count);
      counterService.upsertCount(counter);
      return ApiResponse.ok(count);
    } else if (request.getAction().equals("clear")) {
      if (!curCounter.isPresent()) {
        return ApiResponse.ok(0);
      }
      counterService.clearCount(1);
      return ApiResponse.ok(0);
    } else {
      return ApiResponse.error("参数action错误");
    }
  }
  
}