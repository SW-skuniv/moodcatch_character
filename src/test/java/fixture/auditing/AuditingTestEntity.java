package fixture.auditing;

import com.moodcatch.global.common.BaseTimeEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/**
 * BaseTimeEntity 감사(Auditing) 테스트 전용 엔티티.
 * com.moodcatch 밖에 두어 다른 @SpringBootTest의 엔티티 스캔에 잡히지 않게 한다.
 * (ddl-auto=validate라 테이블이 없는 엔티티가 잡히면 컨텍스트가 뜨지 않는다)
 */
@Entity
public class AuditingTestEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue
    private Long id;
}
