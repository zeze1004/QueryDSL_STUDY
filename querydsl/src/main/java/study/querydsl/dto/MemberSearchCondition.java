package study.querydsl.dto;

import com.querydsl.core.BooleanBuilder;

import java.util.List;

import static com.mysema.commons.lang.Assert.hasText;


public class MemberSearchCondition {
    // 나이(ageGoe, ageLoe)
    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
