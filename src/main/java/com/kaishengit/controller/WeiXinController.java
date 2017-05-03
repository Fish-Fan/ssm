package com.kaishengit.controller;

import com.google.gson.Gson;
import com.kaishengit.pojo.Customer;
import com.kaishengit.pojo.User;
import com.kaishengit.service.CustomerService;
import com.kaishengit.service.UserService;
import com.kaishengit.service.WeiXinService;
import com.qq.weixin.mp.aes.AesException;
import com.qq.weixin.mp.aes.WXBizMsgCrypt;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.inject.Inject;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/wx")
public class WeiXinController {

    private Logger logger = LoggerFactory.getLogger(WeiXinController.class);

    @Value("${wx.token}")
    private String sToken;
    @Value("${wx.aeskey}")
    private String sEncodingAESKey;
    @Value("${wx.corpid}")
    private String sCorpID;

    @Inject
    private WeiXinService weiXinService;
    @Inject
    private UserService userService;
    @Inject
    private CustomerService customerService;

    /**
     * 微信回调模式的验证方法
     * @param msg_signature
     * @param timestamp
     * @param nonce
     * @param echostr
     * @return
     */
    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public String callback(String msg_signature,String timestamp,String nonce,String echostr) {
        logger.debug("{} - {} - {} - {}",msg_signature,timestamp,nonce,echostr);
        String sEchoStr = null;
        try {
            WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(sToken, sEncodingAESKey, sCorpID);
            sEchoStr = wxcpt.VerifyURL(msg_signature,timestamp,nonce,echostr);
        } catch (AesException e) {
            e.printStackTrace();
        }
        return sEchoStr;
    }

    @RequestMapping(value = "/customer",method = RequestMethod.GET)
    public String customerList(@RequestHeader("User-Agent") String userAgent, String code, HttpSession session, Model model) {
        if(userAgent.contains("MicroMessenger")) {

            //验证是否为通讯录中的成员
            if(StringUtils.isEmpty(code)) {
                return "weixin/401";
            } else {
                String json = weiXinService.useCodeGetUserId(code);
                Map<String,String> map = new Gson().fromJson(json, HashMap.class);
                if(map.containsKey("UserId")) {
                    String userId = map.get("UserId");

                    User user = userService.findByUserId(userId);
                    if(user == null) {
                        return "weixin/401";
                    } else {

                        session.setAttribute("curr_user",user);
                        List<Customer> customerList = customerService.findCustomerByUserId(user.getId());
                        model.addAttribute("customerList",customerList);
                    }


                    return "weixin/customerlist";
                } else {
                    return "weixin/401";
                }
            }



        } else {
            return "weixin/noweixin";
        }

    }


    @RequestMapping(value = "/customer/{id:\\d+}",method = RequestMethod.GET)
    public String viewCustomer(@PathVariable Integer id,Model model,HttpSession session) {
        User user = (User) session.getAttribute("curr_user");
        if(user == null) {
            return "weixin/401";
        } else {
            Customer customer = customerService.findCustById(id);
            model.addAttribute("customer",customer);
            return "weixin/customer";
        }
    }





}
