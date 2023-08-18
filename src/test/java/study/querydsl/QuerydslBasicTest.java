package study.querydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Team;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;

import java.util.List;

import static study.querydsl.entity.QMember.*;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory;

    @BeforeEach
    public void before(){

        //JPAQueryFactory를 필드로 제공
        queryFactory = new JPAQueryFactory(em);

        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);
        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL(){

        String qlString = "select m from Member m " +
                "where m.username=:username";

        Member findMember = em.createQuery(qlString, Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");

    }

    @Test
    public void startQuerydsl(){

        //Q클래스 인스턴스 생성 방법 2가지
//        QMember m = new QMember("member1");
//        QMember m = QMember.member;

        Member findMember = queryFactory
                .select(member)
                .from(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search(){
        queryFactory.selectFrom(member)
                .where(
                        member.username.eq("member1"),
                        member.age.eq(10)
//                                .and(member.age.eq(10))
                )
                .fetchOne();
    }

    /**
     * member.username.eq("member1") // username = 'member1'
     * member.username.ne("member1") //username != 'member1'
     * member.username.eq("member1").not() // username != 'member1'
     * member.username.isNotNull() //이름이 is not null
     * member.age.in(10, 20) // age in (10,20)
     * member.age.notIn(10, 20) // age not in (10, 20)
     * member.age.between(10,30) //between 10, 30
     * member.age.goe(30) // age >= 30
     * member.age.gt(30) // age > 30
     * member.age.loe(30) // age <= 30
     * member.age.lt(30) // age < 30
     * member.username.like("member%") //like 검색
     * member.username.contains("member") // like ‘%member%’ 검색
     * member.username.startsWith("member") //like ‘member%’ 검색
     * ...
     */

    @Test
    public void resultFetch(){
        //전체 리스트
        List<Member> memberList = queryFactory
                .select(member)
                .fetch();

        //단일 조회
        Member findMember = queryFactory
                .selectFrom(member)
                .fetchOne();

        //처음 한 건 조회 -> limit 1
        Member firstMember = queryFactory
                .selectFrom(member)
                .fetchFirst();

        //페이징 사용
        QueryResults<Member> pageList = queryFactory
                .selectFrom(member)
                .fetchResults();
        //페이징 결과에서 데이터 추출
        List<Member> results = pageList.getResults();
        //페이징 결과의 전체 개수
        long totalCount1 = pageList.getTotal();

        //count 쿼리
        long totalCount2 = queryFactory
                .selectFrom(member)
                .fetchCount();
    }

}
