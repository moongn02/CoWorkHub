package cn.moongn.coworkhub.common.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class EmailUtils {

    private final JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * 发送简单邮件
     *
     * @param to      收件人
     * @param subject 主题
     * @param content 内容
     */
    public boolean sendSimpleMail(String to, String subject, String content) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true);
            javaMailSender.send(message);
            log.info("邮件发送成功: {}", to);
            return true;
        } catch (MessagingException e) {
            log.error("邮件发送失败: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * 发送验证码邮件
     *
     * @param to          收件人
     * @param code        验证码
     * @param expireTime  过期时间（分钟）
     */
    public boolean sendVerificationCodeMail(String to, String code, int expireTime) {
        String subject = "CoWorkHub - 密码重置验证码";
        String content = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                        "<h2 style='color: #4086f4;'>CoWorkHub 密码重置</h2>" +
                        "<p>您好，</p>" +
                        "<p>您正在进行密码重置操作，请使用以下验证码：</p>" +
                        "<div style='background-color: #f5f5f5; padding: 10px; text-align: center; font-size: 24px; font-weight: bold; letter-spacing: 5px; margin: 20px 0;'>%s</div>" +
                        "<p>此验证码将在 %d 分钟后失效。</p>" +
                        "<p>如果这不是您本人的操作，请忽略此邮件。</p>" +
                        "<p>谢谢！</p>" +
                        "<p>CoWorkHub 团队</p>" +
                        "</div>",
                code, expireTime
        );

        return sendSimpleMail(to, subject, content);
    }

    /**
     * 发送后置任务可开始处理的通知邮件
     *
     * @param to         收件人邮箱
     * @param realName   收件人姓名
     * @param taskTitle  任务标题
     * @param taskId     任务ID
     * @param expectedTime  期望完成时间
     * @return 是否发送成功
     */
    public boolean sendPostTaskNotifyMail(String to, String realName, String taskTitle, Long taskId, String expectedTime) {
        String subject = "CoWorkHub - 任务开始通知";
        String content = String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #ddd; border-radius: 5px;'>" +
                        "<h2 style='color: #4086f4;'>任务开始通知</h2>" +
                        "<p>您好，<b>%s</b>：</p>" +
                        "<p>您负责的任务 <b>【%s】</b>（ID：%d） 的所有前置任务已全部完成，可以开始处理了。</p>" +
                        "<p><b>期望完成时间：</b> %s</p>" +
                        "<p>请及时处理，谢谢！</p>" +
                        "<p style='color: #888;'>本邮件由系统自动发送，请勿回复。</p>" +
                        "<p>CoWorkHub 团队</p>" +
                        "</div>",
                realName, taskTitle, taskId, expectedTime
        );

        return sendSimpleMail(to, subject, content);
    }
}