package com.fxiaoke.azkaban.ldap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import azkaban.user.Permission;
import azkaban.user.Role;
import azkaban.user.User;
import azkaban.user.UserManager;
import azkaban.user.UserManagerException;
import azkaban.utils.Props;

/**
 * ldap using jndi
 * Created by wzk on 16/6/23.
 */
public class LdapUserAuthenticator implements UserManager {

  private LdapAuthenticationConfig ldapConfig = new LdapAuthenticationConfig();
  private Control[] controls;
  private List<String> adminUsers = new ArrayList<>();

  public LdapUserAuthenticator(Props props) {
    ldapConfig.setAdminName(props.get("user.manager.ldap.adminName"));
    ldapConfig.setDomain(props.get("user.manager.ldap.domain"));
    ldapConfig.setBaseDN(props.get("user.manager.ldap.baseDN"));
    ldapConfig.setLdapURL(props.get("user.manager.ldap.ldapURL"));
    ldapConfig.setAdminPassword(props.get("user.manager.ldap.adminPassword"));

    String admins = props.get("user.manager.ldap.adminUsers");
    if (admins != null && admins.trim().length() > 0) {
      adminUsers = Arrays.asList(admins.split(","));
    }
  }

  private LdapContext getLdapContext() throws NamingException {
    Properties env = new Properties();
    env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
    env.put("com.sun.jndi.ldap.connect.pool", "true");
    env.put(Context.SECURITY_AUTHENTICATION, "simple");
    env.put(Context.PROVIDER_URL, ldapConfig.getLdapURL());
    env.put(Context.SECURITY_PRINCIPAL, ldapConfig.getAdminName());
    env.put(Context.SECURITY_CREDENTIALS, ldapConfig.getAdminPassword());
    controls = new Control[]{new LdapControl()};
    return new InitialLdapContext(env, controls);
  }

  private SearchResult findAccountByAccountName(LdapContext ctx, String[] returnedAtts,
                                                String ldapSearchBase,
                                                String accountName) throws NamingException, UserManagerException {
    SearchResult searchResult = null;
    try {
      String searchFilter = "(&(objectClass=user)(sAMAccountName=" + accountName + "))";
      SearchControls searchControls = new SearchControls();
      searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
      if (returnedAtts != null && returnedAtts.length > 0) {
        searchControls.setReturningAttributes(returnedAtts);
      }

      NamingEnumeration<SearchResult>
          results = ctx.search(ldapSearchBase, searchFilter, searchControls);
      if (results.hasMoreElements()) {
        searchResult = results.nextElement();
        //make sure there is not another item available, there should be only 1 match
        if (results.hasMoreElements()) {
          throw new UserManagerException("Ldap error: Matched multiple users for the accountName {" + accountName + "}");
        }
      }
    } catch (Exception e) {
      throw new UserManagerException("Ldap error: ", e);
    }
    return searchResult;
  }

  private boolean authenticate(LdapContext ctx, String domain,
                               String userName, String password) throws UserManagerException {
    String userDn = userName + "@" + domain;
    try {
      ctx.getRequestControls();
      ctx.addToEnvironment(Context.SECURITY_PRINCIPAL, userDn);
      ctx.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
      ctx.reconnect(controls);
      return true;
    } catch (Exception e) {
      throw new UserManagerException("Password is error.");
    }
  }

  @Override
  public User getUser(String username, String password) throws UserManagerException {
    if (username == null || username.trim().isEmpty()) {
      throw new UserManagerException("Username is empty.");
    } else if (password == null || password.trim().isEmpty()) {
      throw new UserManagerException("Password is empty.");
    }

    User user = null;
    LdapContext ctx = null;
    try {
      ctx = getLdapContext();
      if (!authenticate(ctx, ldapConfig.getDomain(), username, password)) {
        throw new UserManagerException("LDAP error: username and password did not match.");
      }
      SearchResult result = findAccountByAccountName(ctx, null, ldapConfig.getBaseDN(), username);
      if (result == null) {
        throw new UserManagerException("LDAP error: User does not exist or maybe there are more than one this user in ldap.");
      }
      Attributes attributes = result.getAttributes();

      user = new User(username);
      Attribute mailAttribute = attributes.get("mail");
      if (mailAttribute != null) {
        String mail = mailAttribute.get().toString();
        user.setEmail(mail);
      }
      if (adminUsers.contains(username)) {
        user.addRole("admin");
      }
    } catch (NamingException e) {
      throw new UserManagerException("LDAP error: " + e.getMessage(), e);
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (NamingException ignored) {
        }
      }
    }
    return user;
  }

  @Override
  public boolean validateUser(String username) {
    if (username == null || username.trim().isEmpty()) {
      return false;
    }

    LdapContext ctx = null;
    try {
      ctx = getLdapContext();
      String[] returnedAtts = {"sAMAccountName", "mail"};
      SearchResult result = findAccountByAccountName(ctx, returnedAtts, ldapConfig.getBaseDN(), username);
      if (result != null) {
        return true;
      }
    } catch (Exception ignored) {
    } finally {
      if (ctx != null) {
        try {
          ctx.close();
        } catch (NamingException ignored) {
        }
      }
    }

    return false;
  }

  @Override
  public boolean validateGroup(String group) {
    return true;
  }

  @Override
  public Role getRole(String roleName) {
    Permission permission = new Permission();
    permission.addPermissionsByName(roleName.toUpperCase());
    return new Role(roleName, permission);
  }

  @Override
  public boolean validateProxyUser(String proxyUser, User realUser) {
    return false;
  }


  private class LdapControl implements Control {

    public byte[] getEncodedValue() {
      return null;
    }

    public String getID() {
      return "1.2.840.113556.1.4.1781";
    }

    public boolean isCritical() {
      return true;
    }
  }
}
