package com.example.checktensionapi;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "customprop")
public class ConfigProperties {

        private boolean mail_smpt_auth;
        private boolean mail_smpt_starttls_enable ;
        private String mail_smpt_host;
        private int mail_smpt_port;
        private String mail_from;
        private String session_password;
        private String mail_to;
        private long scheduleFixedDelay;
        private long timeout;
        private String url;

// standard getters and setters

    public String getUrl() { return url; }

    public void setUrl(String url) { this.url = url; }

    public long getScheduleFixedDelay() {
        return scheduleFixedDelay;
    }

    public void setScheduleFixedDelay(long scheduleFixedDelay) {
        this.scheduleFixedDelay = scheduleFixedDelay;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }

    public boolean isMail_smpt_auth() {
        return mail_smpt_auth;
    }

    public void setMail_smpt_auth(boolean mail_smpt_auth) {
        this.mail_smpt_auth = mail_smpt_auth;
    }

    public boolean isMail_smpt_starttls_enable() {
        return mail_smpt_starttls_enable;
    }

    public void setMail_smpt_starttls_enable(boolean mail_smpt_starttls_enable) {
        this.mail_smpt_starttls_enable = mail_smpt_starttls_enable;
    }

    public String getMail_smpt_host() {
        return mail_smpt_host;
    }

    public void setMail_smpt_host(String mail_smpt_host) {
        this.mail_smpt_host = mail_smpt_host;
    }

    public int getMail_smpt_port() {
        return mail_smpt_port;
    }

    public void setMail_smpt_port(int mail_smpt_port) {
        this.mail_smpt_port = mail_smpt_port;
    }

    public String getMail_from() {
        return mail_from;
    }

    public void setMail_from(String mail_from) {
        this.mail_from = mail_from;
    }

    public String getSession_password() {
        return session_password;
    }

    public void setSession_password(String session_password) {
        this.session_password = session_password;
    }

    public String getMail_to() {
        return mail_to;
    }

    public void setMail_to(String mail_to) {
        this.mail_to = mail_to;
    }
}
