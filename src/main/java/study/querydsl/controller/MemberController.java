package study.querydsl.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import study.querydsl.dto.MemberSearchCondition;
import study.querydsl.dto.MemberTeamDto;
import study.querydsl.repository.MemberJpaRepository;
import study.querydsl.repository.MemberRepository;

import java.util.List;

@RestController
public class MemberController {

    private final MemberJpaRepository memberJpaRepository;
    private final MemberRepository memberRepository;

    public MemberController(MemberJpaRepository memberJpaRepository, MemberRepository memberRepository) {
        this.memberJpaRepository = memberJpaRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition memberSearchCondition) {
        return memberJpaRepository.search(memberSearchCondition);
    }

    @GetMapping("/v2/members")
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition memberSearchCondition, Pageable pageable) {
        return memberRepository.searchPageSimple(memberSearchCondition, pageable);
    }

    @GetMapping("/v3/members")
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition memberSearchCondition, Pageable pageable) {
        return memberRepository.searchPageComplex(memberSearchCondition, pageable);
    }
}
