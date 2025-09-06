package jooby.template.applications;

import static jooby.template.db.tables.Users.USERS;

import io.jooby.Context;
import io.jooby.Jooby;
import io.jooby.metrics.MetricsFilter;
import io.jooby.problem.HttpProblem;
import java.util.UUID;
import org.jooq.DSLContext;

/** A sample set of endpoints for users. */
public final class UserApplication extends Jooby {
  public static final String PATH = "/users";

  {
    use(new MetricsFilter());

    get("/{id}", this::getUser);
  }

  /**
   * Get a user by id.
   *
   * @param ctx the context
   * @return the user
   */
  private User getUser(Context ctx) {
    final var dsl = require(DSLContext.class);
    final var id = ctx.path("id").value(UUID::fromString);

    return dsl.selectFrom(USERS)
        .where(USERS.ID.eq(id))
        .fetchOptional()
        .map(record -> new User(record.getId(), record.getName()))
        .orElseThrow(() -> HttpProblem.notFound("User not found"));
  }

  /**
   * Data model for the user.
   *
   * @param id of the user
   * @param name of the user
   */
  public record User(UUID id, String name) {}
}
