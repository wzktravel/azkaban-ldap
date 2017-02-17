azkaban中可以使用ldap进行登录和权限控制。

## 使用方法
将jar包放置到`azkaban-web-server/extlib`目录下，并修改`conf/azkaban.properties`

示例：

```ini
user.manager.class=com.fxiaoke.azkaban.ldap.LdapUserAuthenticator
user.manager.ldap.ldapURL=LDAP://company.cn:389
user.manager.ldap.adminName=user@company.cn
user.manager.ldap.adminPassword=password
user.manager.ldap.domain=company.cn
user.manager.ldap.baseDN=DC=company,DC=cn
user.manager.ldap.adminUsers=user1,user2,user3
```