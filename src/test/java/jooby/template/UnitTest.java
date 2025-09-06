package jooby.template;

import static jooby.template.db.tables.Users.USERS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.jooby.StatusCode;
import io.jooby.problem.HttpProblem;
import io.jooby.test.MockRouter;
import java.util.UUID;
import jooby.template.applications.UserApplication;
import jooby.template.db.DatabaseIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UnitTest extends DatabaseIntegrationTest {

  MockRouter router;

  @BeforeEach
  void setUp() {
    router = new MockRouter(new Main());
  }

  @Test
  void throwsBadRequestIfUserIdIsIncorrect() {
    assertThatThrownBy(() -> router.get("/users/%s".formatted("not-a-valid-uuid")))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void throwsNotFoundIfUserIsMissing() {
    assertThatThrownBy(() -> router.get("/users/%s".formatted(UUID.randomUUID())))
        .isInstanceOf(HttpProblem.class);
  }

  @Test
  void returnsUser() {
    final var existingUser = database().newRecord(USERS);
    existingUser.setId(UUID.randomUUID());
    existingUser.setName("John");
    existingUser.store();

    router.get(
        "/users/%s".formatted(existingUser.getId()),
        rsp -> {
          assertThat(rsp.getStatusCode()).isEqualTo(StatusCode.OK);

          var user = rsp.value(UserApplication.User.class);
          assertThat(user.name()).isEqualTo("John");
        });
  }
}
