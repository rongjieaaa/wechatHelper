package com.nuoxin.virtual.rep.virtualrepapi.web.controller.request.question;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.io.Serializable;
import java.util.List;

/**
 * Created by fenggang on 9/11/17.
 */
@ApiModel
public class QuestionnaireSaveRequestBean implements Serializable {

    private static final long serialVersionUID = -955947577004247095L;

    @ApiModelProperty(value = "标题")
    private String title;
    @ApiModelProperty(value = "问题")
    private List<QuestionRequestBean> questions;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<QuestionRequestBean> getQuestions() {
        return questions;
    }

    public void setQuestions(List<QuestionRequestBean> questions) {
        this.questions = questions;
    }
}
