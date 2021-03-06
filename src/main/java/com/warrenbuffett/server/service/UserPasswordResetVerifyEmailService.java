package com.warrenbuffett.server.service;

import com.warrenbuffett.server.common.RedisUtil;
import com.warrenbuffett.server.domain.User;
import com.warrenbuffett.server.repository.UserRepository;
import javassist.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.UUID;
@RequiredArgsConstructor
@Service
public class UserPasswordResetVerifyEmailService{
    private final RedisUtil redisUtil;
    @Autowired
    private JavaMailSender emailSender;

    public void sendMail(String to,String sub, String text){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(sub);
        message.setText(text);
        emailSender.send(message);
    }
    public void sendVerificationMail(String email) throws NotFoundException {
        String VERIFICATION_LINK = "http://localhost:8080/api/user/password/verify/";
        if(email==null) throw new NotFoundException("멤버가 조회되지 않음");
        UUID uuid = UUID.randomUUID();
        // insert in redis
        redisUtil.setDataExpire(uuid.toString(),email, 60 * 30L);
        // 인증 링크
         sendMail(email,"[WeSave] 비밀번호 재설정 인증메일입니다.",VERIFICATION_LINK+uuid.toString());
//        sendMail(email,"[WeSave] 비밀번호 재설정 인증메일입니다.",uuid.toString());
    }
    public String verifyEmail(String key) throws NotFoundException {
        String memberEmail = redisUtil.getData(key);
        if(memberEmail==null) throw new NotFoundException("유효하지 않은 링크입니다.");
        redisUtil.deleteData(key);
        return memberEmail;
    }
}
