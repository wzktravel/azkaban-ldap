package com.fxiaoke.azkaban.ldap;

import java.io.Serializable;

/**
 * Ldap配置信息，配置管理员账号密码以及根DN等
 * Created by jiangwj on 2015/12/29.
 */
public class LdapAuthenticationConfig implements Serializable{
  private String adminName;
  private String adminPassword;
  private String ldapURL;
  private String baseDN;
  private String domain;

  public String getDomain() {
    return domain;
  }

  public void setDomain(String domain) {
    this.domain = domain;
  }
  public String getAdminName() {
    return adminName;
  }

  public void setAdminName(String adminName) {
    this.adminName = adminName;
  }

  public String getAdminPassword() {
    return adminPassword;
  }

  public void setAdminPassword(String adminPassword) {
    this.adminPassword = adminPassword;
  }

  public String getLdapURL() {
    return ldapURL;
  }

  public void setLdapURL(String ldapURL) {
    this.ldapURL = ldapURL;
  }

  public String getBaseDN() {
    return baseDN;
  }

  public void setBaseDN(String baseDN) {
    this.baseDN = baseDN;
  }

}
