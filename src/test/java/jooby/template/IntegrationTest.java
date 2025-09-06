package jooby.template;

import static io.jooby.MediaType.JSON;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCEPT;
import static jooby.template.db.tables.Users.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import io.jooby.StatusCode;
import io.jooby.test.JoobyTest;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.UUID;
import jooby.template.applications.UserApplication;
import jooby.template.db.DatabaseIntegrationTest;
import okhttp3.OkHttpClient;
import okhttp3.Request;

class IntegrationTest extends DatabaseIntegrationTest {

  static OkHttpClient client = new OkHttpClient();

  @JoobyTest(Main.class)
  void throwsBadRequestIfUserIdIsIncorrect(int serverPort) throws IOException {
    final var req =
        new Request.Builder()
            .url("http://localhost:%d/users/%s".formatted(serverPort, "not-a-valid-uuid"))
            .addHeader(ACCEPT.toString(), JSON)
            .build();

    try (final var rsp = client.newCall(req).execute()) {
      assertThat(rsp.code()).isEqualTo(StatusCode.BAD_REQUEST.value());
    }
  }

  @JoobyTest(Main.class)
  void throwsNotFoundIfUserIsMissing(int serverPort) throws IOException {
    final var req =
        new Request.Builder()
            .url("http://localhost:%d/users/%s".formatted(serverPort, UUID.randomUUID()))
            .addHeader(ACCEPT.toString(), JSON)
            .build();

    try (final var rsp = client.newCall(req).execute()) {
      assertThat(rsp.code()).isEqualTo(StatusCode.NOT_FOUND.value());
    }
  }

  @JoobyTest(Main.class)
  void returnsUser(int serverPort) throws IOException {
    final var existingUser = database().newRecord(USERS);
    existingUser.setId(UUID.randomUUID());
    existingUser.setName("John");
    existingUser.store();

    final var req =
        new Request.Builder()
            .url("http://localhost:%d/users/%s".formatted(serverPort, existingUser.getId()))
            .addHeader(ACCEPT.toString(), JSON)
            .build();

    try (final var rsp = client.newCall(req).execute()) {
      assertThat(rsp.code()).isEqualTo(StatusCode.OK.value());

      final var user = parse(rsp.body().bytes(), UserApplication.User.class);
      assertThat(user.name()).isEqualTo("John");
    }
  }

  private static <T> T parse(byte[] bytes, Class<T> type) {
    try {
      return Main.MAPPER.readValue(bytes, type);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
