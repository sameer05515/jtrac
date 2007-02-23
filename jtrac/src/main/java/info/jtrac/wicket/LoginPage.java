/*
 * Copyright 2002-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.wicket;

import info.jtrac.Jtrac;
import info.jtrac.Version;
import info.jtrac.domain.User;
import java.io.Serializable;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import wicket.Component;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.PasswordTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.BoundCompoundPropertyModel;

/**
 * login page
 */
public class LoginPage extends WebPage {              
    
    protected final Log logger = LogFactory.getLog(getClass());
    
    public LoginPage() {
        add(new Label("title", getLocalizer().getString("login.title", null)));
        add(new Link("home") {
            public void onClick() {
            }
        });
        add(new LoginForm("form"));
        add(new Label("version", Version.VERSION));
    }
    
    private class LoginForm extends Form {        
        
        public LoginForm(String id) {            
            super(id);          
            add(new FeedbackPanel("feedback"));
            setModel(new BoundCompoundPropertyModel(new LoginFormModel()));
            final TextField loginName = new TextField("loginName");
            loginName.setOutputMarkupId(true);
            add(loginName);
            final PasswordTextField password = new PasswordTextField("password");
            password.setRequired(false);
            password.setOutputMarkupId(true);
            add(password);
            // set focus on right textbox
            getBodyContainer().addOnLoadModifier(new AbstractReadOnlyModel() {
                public Object getObject(Component c) {
                    String markupId;
                    if(loginName.getConvertedInput() == null) {
                        markupId = loginName.getMarkupId();
                    } else {
                        markupId = password.getMarkupId();
                    }
                    return "document.getElementById('" + markupId + "').focus()";
                }
            });            
            add(new CheckBox("rememberMe"));
        }
                
        @Override
        protected void onSubmit() {                    
            LoginFormModel model = (LoginFormModel) getModelObject();
            if(model.getLoginName() == null || model.getPassword() == null) {
                logger.debug("login failed login name and password are mandatory");
                error(getLocalizer().getString("login.error", null));
                return;
            }
            Jtrac jtrac = ((JtracApplication) getApplication()).getJtrac();
            User user = null;
            try {
                user = (User) jtrac.loadUserByUsername(model.getLoginName());
            } catch (UsernameNotFoundException e) {
                logger.debug("login failed - user not found");
                error(getLocalizer().getString("login.error", null));
                return;
            }            
            if (user.getPassword().equals(jtrac.encodeClearText(model.getPassword()))) {
                ((JtracSession) getSession()).setUser(user);
                if (!continueToOriginalDestination()) {
                    setResponsePage(DashboardPage.class);
                } 
            } else {
                logger.debug("login failed - password does not match");
                error(getLocalizer().getString("login.error", null));                    
            }                  
        }     
                        
    }     
    
    private class LoginFormModel implements Serializable {
        
        private String loginName;
        private String password;
        private boolean rememberMe;

        public String getLoginName() {
            return loginName;
        }

        public void setLoginName(String loginName) {
            this.loginName = loginName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }        

        public boolean isRememberMe() {
            return rememberMe;
        }

        public void setRememberMe(boolean rememberMe) {
            this.rememberMe = rememberMe;
        }        
        
    }
        

    
}