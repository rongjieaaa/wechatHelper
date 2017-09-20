package com.nuoxin.virtual.rep.api.service;

import com.nuoxin.virtual.rep.api.dao.SmsTemplateRepository;
import com.nuoxin.virtual.rep.api.entity.SmsTemplate;
import org.aspectj.weaver.Lint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;

/**
 * Created by fenggang on 9/18/17.
 */
@Service
@Transactional(readOnly = true)
public class SmsTemplateService {

    @Autowired
    private SmsTemplateRepository smsTemplateRepository;

    public SmsTemplate fingById(Long id){
        return smsTemplateRepository.findOne(id);
    }

    public List<SmsTemplate> findByAll(){
        return smsTemplateRepository.findAll();
    }
}
