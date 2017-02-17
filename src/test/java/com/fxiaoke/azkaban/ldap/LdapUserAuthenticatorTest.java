package com.fxiaoke.azkaban.ldap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import azkaban.user.User;
import azkaban.utils.Props;

/**
 * LdapUserAuthenticatorTest
 * Created by wzk on 16/6/23.
 */
@Ignore
public class LdapUserAuthenticatorTest {

  private LdapUserAuthenticator authenticator;

  @Before
  public void before() throws Exception {
    Props props = new Props();
    props.put("user.manager.ldap.ldapURL", "LDAP://firstshare.cn");
    props.put("user.manager.ldap.adminName", "user");
    props.put("user.manager.ldap.adminPassword", "password");
    props.put("user.manager.ldap.domain", "firstshare.cn");
    props.put("user.manager.ldap.baseDN", "DC=firstshare,DC=cn");
    authenticator = new LdapUserAuthenticator(props);
  }

  @Test
  public void getUser() throws Exception {
    User wangzk = authenticator.getUser("user", "");
    String email = wangzk.getEmail();
    String userId = wangzk.getUserId();
    System.out.println(userId);
    System.out.println(email);
  }
}
