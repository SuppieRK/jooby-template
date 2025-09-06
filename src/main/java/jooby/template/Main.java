package jooby.template;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import io.jooby.Jooby;
import io.jooby.OpenAPIModule;
import io.jooby.flyway.FlywayModule;
import io.jooby.hikari.HikariModule;
import io.jooby.jackson.JacksonModule;
import io.jooby.metrics.MetricsModule;
import io.jooby.netty.NettyServer;
import java.util.concurrent.TimeUnit;
import jooby.template.applications.UserApplication;
import jooby.template.extensions.JooqModule;

/** Application main class. */
public final class Main extends Jooby {
  public static final ObjectMapper MAPPER = objectMapper();

  {
    final var metrics = new MetricRegistry();
    final var healthChecks = new HealthCheckRegistry();

    install(new JacksonModule(MAPPER));

    install(new HikariModule().metricRegistry(metrics).healthCheckRegistry(healthChecks));
    install(new FlywayModule());
    install(new JooqModule());
    install(new OpenAPIModule());
    install(
        new MetricsModule(metrics, healthChecks)
            // Example reporter, to be replaced with a real one
            .reporter(
                registry -> {
                  final var reporter =
                      Slf4jReporter.forRegistry(registry)
                          .convertDurationsTo(TimeUnit.SECONDS)
                          .convertRatesTo(TimeUnit.SECONDS)
                          .build();
                  reporter.start(100, TimeUnit.MILLISECONDS);
                  return reporter;
                })
            .ping()
            .healthCheck("deadlock", new ThreadDeadlockHealthCheck())
            .metric("memory", new MemoryUsageGaugeSet())
            .metric("threads", new ThreadStatesGaugeSet())
            .metric("gc", new GarbageCollectorMetricSet())
            .metric("fs", new FileDescriptorRatioGauge()));

    install(UserApplication.PATH, UserApplication::new);
  }

  /**
   * Main method.
   *
   * @param args for the application
   */
  public static void main(final String[] args) {
    runApp(args, new NettyServer(), Main::new);
  }

  /**
   * Creates a new {@link ObjectMapper}.
   *
   * @return an {@link ObjectMapper}.
   */
  private static ObjectMapper objectMapper() {
    final var mapper = new ObjectMapper();
    mapper.findAndRegisterModules();
    mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return mapper;
  }
}
