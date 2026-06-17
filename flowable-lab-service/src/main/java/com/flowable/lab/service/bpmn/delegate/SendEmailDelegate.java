package com.flowable.lab.service.bpmn.delegate;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component("sendEmailDelegate")
public class SendEmailDelegate implements JavaDelegate {

    private static final Logger log = LoggerFactory.getLogger(SendEmailDelegate.class);

    @Override
    public void execute(DelegateExecution execution) {
        String to = (String) execution.getVariable("emailTo");
        String subject = (String) execution.getVariable("emailSubject");
        String content = (String) execution.getVariable("emailContent");

        log.info("=== 模拟发送邮件 ===");
        log.info("收件人: {}", to != null ? to : "未指定");
        log.info("主题: {}", subject != null ? subject : "流程通知");
        log.info("内容: {}", content != null ? content : "您有一个待办任务需要处理");
        log.info("===================");
    }
}
