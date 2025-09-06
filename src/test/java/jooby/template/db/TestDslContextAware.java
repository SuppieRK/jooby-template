package jooby.template.db;

import org.jooq.DSLContext;

/** Allows test fixtures to be aware of the DSL context. */
public interface TestDslContextAware {
  DSLContext database();
}
