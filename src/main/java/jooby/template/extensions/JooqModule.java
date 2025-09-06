package jooby.template.extensions;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.jooby.Extension;
import io.jooby.Jooby;
import io.jooby.ServiceKey;
import javax.sql.DataSource;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

/** Simple jOOQ module to expose a {@link DSLContext} to the application. */
public final class JooqModule implements Extension {

  private final String name;

  /**
   * Creates a new jOOQ module.
   *
   * @param name The name/key of the data source to attach.
   */
  public JooqModule(@NonNull String name) {
    this.name = name;
  }

  /**
   * Creates a new jOOQ module. Use the default/first datasource and register objects using the
   * <code>db</code> key.
   */
  public JooqModule() {
    this("db");
  }

  @Override
  public void install(@NonNull Jooby application) throws Exception {
    var registry = application.getServices();
    var dataSource = registry.getOrNull(ServiceKey.key(DataSource.class, name));
    if (dataSource == null) {
      throw new IllegalStateException("No DataSource found for key: " + name);
    }

    var jooq = DSL.using(dataSource, SQLDialect.POSTGRES);

    registry.putIfAbsent(DSLContext.class, jooq);
    registry.put(ServiceKey.key(DSLContext.class, name), jooq);
  }
}
