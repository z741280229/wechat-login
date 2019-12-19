package com.jovan.wxjava.controller;

import com.alibaba.fastjson.JSON;
import com.jovan.wxjava.entity.RestResult;
import com.jovan.wxjava.entity.RestResultGenerator;
import com.jovan.wxjava.entity.Token;
import com.jovan.wxjava.entity.User;
import com.jovan.wxjava.protocol.LoginProtocol;
import com.jovan.wxjava.service.WeChatService;
import com.jovan.wxjava.util.HttpClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;

/**
 * @author Jovan
 * @create 2019/8/12
 */
@RestController
public class WeChatController {

    @Autowired
    private WeChatService weChatService;

    /**
     * 获取微信登陆二维码地址
     * @return
     */
    @GetMapping("/getQRCodeUrl")
    public RestResult getQRCodeUrl() {
        return RestResultGenerator.createOkResult(weChatService.getQRCodeUrl());
    }

    /**
     * 微信扫码回调处理
     * 使用 @Valid + BindingResult 进行 controller 参数校验，实现断路器。大家可以根据自己的喜好来，不必跟我这样做
     * @param input
     * @param bindingResult
     * @return
     */
    /*@GetMapping("/wxCallBack")*/
    public String wxCallBack(@RequestBody @Valid LoginProtocol.WeChatQrCodeCallBack.Input input, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "failedPage";
        }

        if (weChatService.wxCallBack(input)) {
            return "successPage";
        } else {
            return "failedPage";
        }
    }

    @GetMapping("/wxCallBack")
    public void wxcallback(HttpServletRequest request, HttpServletResponse response){
        System.out.println("微信服务器回调...");
        // 获取到code值
        String code = request.getParameter("code");
        // 判断
        if (code == null) {
            throw new RuntimeException("用户禁止授权...");
        }

        try {
            // 获取到了code值，回调没有问题
            // 定义地址
            String token_url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wx7287a60bb700fd21" +
                    "&secret=1ef8755f92bebae8ad7bab432ba29cbf&code=" + code + "&grant_type=authorization_code";
            // 发送请求
            HttpClient client = new HttpClient(token_url);
            // 发送get请求
            client.get();
            // 获取到请求的结果  json格式的字符串，把json格式的字符串转换成对象或者Map集合
            String token_content = client.getContent();
            // 把json字符串转换成对象
            Token token = JSON.parseObject(token_content, Token.class);

            // 获取到接口调用凭证
            // 获取个人的信息
            String user_url = "https://api.weixin.qq.com/sns/userinfo?access_token=" + token.getAccess_token() + "&openid=" + token.getOpenid();
            HttpClient client1 = new HttpClient(user_url);
            client1.get();
            String user_content = client1.getContent();
            // 解析json字符串
            User user = JSON.parseObject(user_content, User.class);

            System.out.println("微信用户信息：" + user);
        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @GetMapping("/test")
    public String test(){
        return "test";
    }
}
