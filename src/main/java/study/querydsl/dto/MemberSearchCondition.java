package study.querydsl.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class MemberSearchCondition {
    // 회원명, 팀명, 나이(ageGeo, ageLoe)

    private String username;
    private String teamName;
    private Integer ageGoe;
    private Integer ageLoe;
}
