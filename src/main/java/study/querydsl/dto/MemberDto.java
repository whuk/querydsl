package study.querydsl.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class MemberDto {

    private String username;
    private int age;

    @Builder
    private MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }
}
