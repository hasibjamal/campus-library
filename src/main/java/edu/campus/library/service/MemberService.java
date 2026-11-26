package edu.campus.library.service;

import edu.campus.library.domain.Repository;
import edu.campus.library.domain.member.Member;
import edu.campus.library.domain.member.MemberType;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

public class MemberService {
    private final Repository<Member> members;
    private final IdGenerator ids;

    public MemberService(Repository<Member> members, IdGenerator ids) {
        this.members = members;
        this.ids = ids;
    }

    public Member register(String name, String email, MemberType type) {
        return members.save(new Member(ids.next("M"), name, email, type));
    }

    public Member get(String id) {
        return members.findById(id).orElseThrow(() -> new NoSuchElementException("No member " + id));
    }

    public Member save(Member member) {
        return members.save(member);
    }

    public List<Member> all() {
        return members.findAll();
    }

    public BigDecimal payFine(String memberId, BigDecimal amount) {
        Member m = get(memberId);
        BigDecimal applied = m.payFine(amount);
        members.save(m);
        return applied;
    }
}
