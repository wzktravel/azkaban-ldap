azkaban�п���ʹ��ldap���е�¼��Ȩ�޿��ơ�

## ʹ�÷���
��jar�����õ�`azkaban-web-server/extlib`Ŀ¼�£����޸�`conf/azkaban.properties`

ʾ����

```ini
user.manager.class=com.fxiaoke.azkaban.ldap.LdapUserAuthenticator
user.manager.ldap.ldapURL=LDAP://company.cn:389
user.manager.ldap.adminName=user@company.cn
user.manager.ldap.adminPassword=password
user.manager.ldap.domain=company.cn
user.manager.ldap.baseDN=DC=company,DC=cn
user.manager.ldap.adminUsers=user1,user2,user3
```