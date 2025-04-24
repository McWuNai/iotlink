package com.yunze.web.controller.system;

import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.google.code.kaptcha.Producer;
import com.yunze.common.core.redis.RedisCache;
import com.yunze.common.mapper.yunze.YzUserMapper;
import com.yunze.common.utils.uuid.IdUtils;
import com.yunze.common.utils.uuid.UUID;
import com.yunze.common.utils.yunze.AesEncryptUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.yunze.common.constant.Constants;
import com.yunze.common.core.domain.AjaxResult;
import com.yunze.common.core.domain.entity.SysMenu;
import com.yunze.common.core.domain.entity.SysUser;
import com.yunze.common.core.domain.model.LoginBody;
import com.yunze.common.core.domain.model.LoginUser;
import com.yunze.common.utils.ServletUtils;
import com.yunze.framework.web.service.SysLoginService;
import com.yunze.framework.web.service.SysPermissionService;
import com.yunze.framework.web.service.TokenService;
import com.yunze.system.service.ISysMenuService;

import javax.annotation.Resource;

/**
 * 登录验证
 * 
 * @author yunze
 */
@RestController
public class SysLoginController
{
    @Autowired
    private SysLoginService loginService;

    @Autowired
    private ISysMenuService menuService;

    @Autowired
    private SysPermissionService permissionService;

    @Autowired
    private TokenService tokenService;
    @Resource
    private YzUserMapper yzUserMapper;
    @Resource
    private RedisCache redisCache;

    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    // 验证码类型
    @Value("${yunze.captchaType}")
    private String captchaType;


    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    /**
     * 登录方法
     *
     * @param  临时登录信息
     * @return 结果
     */
    @PostMapping("/loginDemo")
    public AjaxResult loginDemo(@RequestBody  String Pstr)
    {
        AjaxResult ajax = AjaxResult.success();
        LoginBody loginBody = null;
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {

            // 保存验证码信息
            String uuid = IdUtils.simpleUUID();
            String verifyKey = Constants.CAPTCHA_CODE_KEY + uuid;
            String code = null;

            // 生成验证码
            if ("math".equals(captchaType))
            {
                String capText = captchaProducerMath.createText();
                code = capText.substring(capText.lastIndexOf("@") + 1);
            }
            else if ("char".equals(captchaType))
            {
                code = captchaProducer.createText();
            }

            redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);

            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            loginBody = new LoginBody();
            JSONObject Parammap = JSON.parseObject(Pstr);
            loginBody.setCode(code);
            loginBody.setUsername(Parammap.get("username").toString());
            loginBody.setUuid(uuid);
            loginBody.setPassword(Parammap.get("password").toString());

        }catch (Exception e){
            System.out.println("登录异常");
        }


        String username = loginBody.getUsername();
        // 生成令牌
        String token = loginService.login(username, loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 登录方法
     * 
     * @param  登录信息
     * @return 结果
     */
    @PostMapping("/login")
    public AjaxResult login(@RequestBody  String Pstr)
    {
        AjaxResult ajax = AjaxResult.success();
        LoginBody loginBody = null;
        if(Pstr!=null){
            Pstr = Pstr.replace("%2F", "/");//转义 /
        }
        try {
            Pstr =  AesEncryptUtil.desEncrypt(Pstr);
            loginBody = new LoginBody();
            JSONObject Parammap = JSON.parseObject(Pstr);
            loginBody.setCode(Parammap.get("code").toString());
            loginBody.setUsername(Parammap.get("username").toString());
            loginBody.setUuid(Parammap.get("uuid").toString());
            loginBody.setPassword(Parammap.get("password").toString());

        }catch (Exception e){
            System.out.println("登录异常");
        }


        String username = loginBody.getUsername();
        boolean result= username.matches("[0-9]+");
        if (result == true) {
            System.out.println("该字符串是纯数字");
            Map<String,Object> fmap = new HashMap<>();
            fmap.put("phonenumber",username);
            Map<String,String> userMpa =  yzUserMapper.findUser_name(fmap);
            if(userMpa!=null && userMpa.get("user_name")!=null){
                username = userMpa.get("user_name");
            }
        }
        // 生成令牌
        String token = loginService.login(username, loginBody.getPassword(), loginBody.getCode(),
                loginBody.getUuid());
        ajax.put(Constants.TOKEN, token);
        return ajax;
    }

    /**
     * 短信登录方法
     * @param loginBody 登录信息
     * @return 结果
     */
    @PostMapping("/message")
    public AjaxResult loginMessage(@RequestBody LoginBody loginBody) throws Exception {
        boolean bool = false;
        AjaxResult ajax = null;
        String username = loginBody.getUsername();
        String code = loginBody.getCode();


        boolean result = username.matches("[0-9]+");
        String password = "";
        String message = "";
        Object yz_code = redisCache.getCacheObject("login_"+username);

        if(yz_code!=null ){
            if(code.equals(yz_code)){
                if (result == true) {
                    System.out.println("该字符串是纯数字");
                    Map<String,Object> fmap = new HashMap<>();
                    fmap.put("phonenumber",username);
                    Map<String,String> userMpa =  yzUserMapper.user_Massage(fmap);
                    if(userMpa!=null && userMpa.get("user_name")!=null){
                        bool = true;
                        username = userMpa.get("user_name");
                        password = userMpa.get("password");
                    }else{
                        message = "未找到手机号对应账号信息，请核对后重试！";
                    }
                }else{
                    message = "手机号格式不正确，请核对后重试！";
                }
            }else{
                message = "验证码有误！";
            }
        }else {
            message = "验证码已失效！";
        }

        if(bool){
            ajax = AjaxResult.success();
            // 生成令牌
            String token = loginService.Massagelogin(username, password,loginBody.getUuid());

            System.out.println(token);
            ajax.put(Constants.TOKEN, token);
        }else{

            ajax = AjaxResult.error(message);
        }

        return ajax;
    }

    /**
     * 获取用户信息
     * 
     * @return 用户信息
     */
    @GetMapping("getInfo")
    public AjaxResult getInfo()
    {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        SysUser user = loginUser.getUser();
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = AjaxResult.success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 获取路由信息
     * 
     * @return 路由信息
     */
    @GetMapping("getRouters")
    public AjaxResult getRouters()
    {
        LoginUser loginUser = tokenService.getLoginUser(ServletUtils.getRequest());
        // 用户信息
        SysUser user = loginUser.getUser();
        List<SysMenu> menus = menuService.selectMenuTreeByUserId(user.getUserId());
        return AjaxResult.success(menuService.buildMenus(menus));
    }
}
