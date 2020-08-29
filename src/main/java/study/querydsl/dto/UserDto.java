package study.querydsl.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class UserDto {

    private String name;
    private int age;

    @Builder
    private UserDto(String name, int age) {
        this.name = name;
        this.age = age;
    }
}
