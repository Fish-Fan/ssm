import com.kaishengit.pojo.User;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * Created by yanfeng-mac on 2017/5/3.
 */
import org.apache.shiro.*;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.Factory;
import org.junit.Assert;

import java.util.Arrays;

public class test {
    public static void main(String[] args) {
//        String password = "admin";
//        System.out.println(DigestUtils.md5Hex(password+"QDF&*^%&#$%$$%#123123^%^#%$#"));

        //1、获取SecurityManager工厂，此处使用Ini配置文件初始化SecurityManager
        Factory<SecurityManager> factory = new IniSecurityManagerFactory("classpath:shiro.ini");
        //2、得到SecurityManager实例 并绑定给SecurityUtils
        SecurityManager securityManager = factory.getInstance();
        //3、得到Subject及创建用户名/密码身份验证Token（即用户身份/凭证）
        SecurityUtils.setSecurityManager(securityManager);

        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken token = new UsernamePasswordToken("zhang","123");

        try{
            //4.登录
            subject.login(token);
            System.out.println(subject.getPrincipal());
            System.out.println(subject.isAuthenticated());

            //用户是否为某个角色
            System.out.println("zhang is has role1->" + subject.hasRole("role1"));
            System.out.println("zhang is has role2->" + subject.hasRole("role2"));
            System.out.println("zhang is has role1 and role2->" + subject.hasAllRoles(Arrays.asList("role1","role2")));
            System.out.println("zhang is has role1 and role2 and role3->" + subject.hasAllRoles(Arrays.asList("role1","role2","role3")));

            //用户是否有某些权限
            System.out.println("zhang can create user->" + subject.isPermitted("user.create"));
            System.out.println("zhang can update user->" + subject.isPermitted("user.update"));
            System.out.println("zhang can delete user->" + subject.isPermitted("user.delete"));

        } catch (AuthenticationException e){
            //5.登录失败
            e.printStackTrace();
        }



        //退出登录
        subject.logout();
    }
}
