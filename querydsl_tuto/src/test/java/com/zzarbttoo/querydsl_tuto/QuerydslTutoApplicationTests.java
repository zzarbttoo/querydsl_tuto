package com.zzarbttoo.querydsl_tuto;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.zzarbttoo.querydsl_tuto.entity.Hello;
import com.zzarbttoo.querydsl_tuto.entity.QHello;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional //rollback
@Commit
class QuerydslTutoApplicationTests {

    //@Autowired
    @PersistenceContext
    EntityManager em;

    @Test
    void contextLoads() {

        Hello hello = new Hello();
        em.persist(hello);

        JPAQueryFactory query = new JPAQueryFactory(em);
        //QHello qHello = new QHello("h"); //alias
        QHello qHello = QHello.hello; //static으로 저렇게 만들어놓는다


        Hello result = query
                .selectFrom(qHello)
                .fetchOne();

        assertThat(result).isEqualTo(hello);
        assertThat(result.getId()).isEqualTo(hello.getId());




    }

}
