package org.runnect.server.user.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class RunnectUserTest {

    private RunnectUser userWithId(Long id) {
        RunnectUser user = RunnectUser.builder()
            .nickname("러너")
            .socialId("social")
            .email("runner@runnect.io")
            .provider(SocialType.KAKAO)
            .build();
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    @Test
    void 자기_자신과는_같다() {
        RunnectUser user = userWithId(1L);

        assertThat(user).isEqualTo(user);
    }

    @Test
    void id가_같으면_인스턴스가_달라도_같다() {
        RunnectUser a = userWithId(1L);
        RunnectUser b = userWithId(1L);

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    void id가_다르면_다르다() {
        RunnectUser a = userWithId(1L);
        RunnectUser b = userWithId(2L);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void id가_없는_인스턴스끼리는_다르다() {
        RunnectUser a = userWithId(null);
        RunnectUser b = userWithId(null);

        assertThat(a).isNotEqualTo(b);
    }

    @Test
    void null과_비교하면_다르다() {
        RunnectUser user = userWithId(1L);

        assertThat(user).isNotEqualTo(null);
    }

    @Test
    void 다른_타입과_비교하면_다르다() {
        RunnectUser user = userWithId(1L);

        assertThat(user).isNotEqualTo("1");
    }

    @Test
    void hashCode는_id와_무관하게_동일_클래스면_같다() {
        RunnectUser a = userWithId(1L);
        RunnectUser b = userWithId(2L);

        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
