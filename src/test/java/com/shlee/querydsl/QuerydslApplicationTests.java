package com.shlee.querydsl;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.shlee.querydsl.entity.Hello;
import com.shlee.querydsl.entity.QHello;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import java.util.List;

@SpringBootTest
@Transactional // 트랜잭션이 있으면 rollback 됨
@Commit
class QuerydslApplicationTests {

	@Autowired
	EntityManager em;

	@Test
	void contextLoads() {
		Hello hello = new Hello();
		em.persist(hello);

		JPAQueryFactory query = new JPAQueryFactory(em);
		QHello qHello = QHello.hello;


		// queydsl과 관련된 것은 q타입을 넣어야함
		Hello result = query
				.selectFrom(qHello)
				.fetchOne();

		Assertions.assertThat(result).isEqualTo(hello);
		Assertions.assertThat(result.getId()).isEqualTo(hello.getId());
	}



}
