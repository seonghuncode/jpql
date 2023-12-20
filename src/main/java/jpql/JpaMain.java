package jpql;

import javax.persistence.*;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        //애플리케이션 로딩 시점에 한개만 만든다.(EntityManagerFactory : DB당 하나만 생성 / 애플리케이션이 실행될때 실행)
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        //일괄적인 트랜잭션 단위마다 만든다. (EntityManager : 요청이 올때마다 사용 삭재를 반복 하면서 사용 -> 쓰레드 공간을 공유하면 안된다.(쓰고 버린다는 개념으로 사용))
        //jpa모든 변경은 트랜잭션 안에서 실행해야 한다.
        EntityManager em = emf.createEntityManager();

        //트랜잭션을 얻는 작업
        EntityTransaction tx = em.getTransaction();
        tx.begin(); //트랜잭션 시작


        try {

            Team teamA = new Team();
            teamA.setName("팀A");
            em.persist(teamA);

            Team teamB = new Team();
            teamB.setName("팀B");
            em.persist(teamB);

            Member member1 = new Member();
            member1.setUsername("회원1");
            member1.setTeam(teamA);
            member1.setAge(0);
            em.persist(member1);

            Member member2 = new Member();
            member2.setUsername("회원2");
            member2.setTeam(teamA);
            member2.setAge(0);
            em.persist(member2);

            Member member3 = new Member();
            member3.setUsername("회원3");
            member3.setTeam(teamB);
            member3.setAge(0);
            em.persist(member3);

            /*
            * 해당 시점에서 Flush됨
            * 이미 실행을 했기 때문에 영속성 컨텍스트 에는 member1, member2, member3가 들어있는데
            * 벌크연산을 할 경우 바로 값을 DB에 강제로 접근을 했기때문에 영속성 컨텍스트에는 나이가 20살로 반영이 되지X
            *  -> em.clear()를 사용해서 영속성 컨텍스트를 지우고
            *  ->  Member findMember = em.find(Member.class, member1.getId());로 member를 다시 불러와서 사용해야 한다.
            * */


            //모든 회원의 나이를 20살로 변경
            int resultCount = em.createQuery("update Member m set m.age = 20")
                    .executeUpdate();

            em.clear();
            Member findMember = em.find(Member.class, member1.getId());
            System.out.println("findMember : " + findMember);


            tx.commit(); // -> 이때 DB에 쿼라가 날라간다.

        } catch (Exception e) {
            tx.rollback();
            e.printStackTrace();
        } finally {
            em.close(); //동작이 끝나면 항상 닫아준다
        }

        emf.close(); //was가 내려갈대 종료 (리소스가 내부적으로 종료)
    }


}
