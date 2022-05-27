package japbook.jpashop.repository;

import japbook.jpashop.domain.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
@RequiredArgsConstructor //@PersistenceContext 까지 injection
public class MemberRepository {

    private final EntityManager em;

    public void save(Member member) { //저장
        em.persist(member);
    }

    public Member findOne(Long id) { //단건 조회
        return em.find(Member.class, id);
    }

    public List<Member> findAll() { //전체 조회
        return em.createQuery("select m from Member m", Member.class)
                .getResultList();
    }

    //이름으로 검색
    public List<Member> findByName(String name) {
        return em.createQuery("select m from Member m where m.name = :name", Member.class)
                .setParameter("name", name)
                .getResultList();
    }
}
