package io.micronaut.starter.feature.micrometer

import io.micronaut.context.BeanContext
import io.micronaut.starter.command.CommandContext
import io.micronaut.starter.feature.Features
import io.micronaut.starter.fixture.ContextFixture
import io.micronaut.starter.fixture.ProjectFixture
import io.micronaut.starter.options.BuildTool
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import io.micronaut.starter.feature.build.gradle.templates.buildGradle
import io.micronaut.starter.feature.build.maven.templates.pom

class MicrometerSpec extends Specification implements ProjectFixture, ContextFixture {

    @Shared @AutoCleanup BeanContext beanContext = BeanContext.run()

    @Unroll
    void 'test gradle micrometer feature #micrometerFeature.name'() {
        given:
        String dependency = "micronaut-micrometer-registry-${micrometerFeature.name - 'micrometer-'}"
        Features features = getFeatures([micrometerFeature.name])

        when:
        String template = buildGradle.template(buildProject(), features).render().toString()

        then:
        template.contains("implementation \"io.micronaut.configuration:${dependency}\"")

        where:
        micrometerFeature << beanContext.getBeansOfType(MicrometerFeature).iterator()
    }

    void "test gradle micrometer multiple features"() {
        when:
        Features features = getFeatures(["micrometer-atlas", "micrometer-influx"])
        String template = buildGradle.template(buildProject(), features).render().toString()

        then:
        template.contains("""
    implementation "io.micronaut.configuration:micronaut-micrometer-core"
    implementation "io.micronaut.configuration:micronaut-micrometer-registry-atlas"
    implementation "io.micronaut.configuration:micronaut-micrometer-registry-influx"
""")
        template.count("io.micronaut.configuration:micronaut-micrometer-core") == 1
    }

    @Unroll
    void 'test maven micrometer feature #micrometerFeature.name'() {
        given:
        String dependency = "micronaut-micrometer-registry-${micrometerFeature.name - 'micrometer-'}"
        Features features = getFeatures([micrometerFeature.name], null, null, BuildTool.maven)

        when:
        String template = pom.template(buildProject(), features, [:]).render().toString()

        then:
        template.contains("""
    <dependency>
      <groupId>io.micronaut.configuration</groupId>
      <artifactId>$dependency</artifactId>
      <scope>compile</scope>
    </dependency>
""")

        where:
        micrometerFeature << beanContext.getBeansOfType(MicrometerFeature).iterator()
    }

    void "test maven micrometer multiple features"() {
        when:
        Features features = getFeatures(["micrometer-atlas", "micrometer-influx"], null, null, BuildTool.maven)
        String template = pom.template(buildProject(), features, [:]).render().toString()

        then:
        template.contains("""
    <dependency>
      <groupId>io.micronaut.configuration</groupId>
      <artifactId>micronaut-micrometer-core</artifactId>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>io.micronaut.configuration</groupId>
      <artifactId>micronaut-micrometer-registry-atlas</artifactId>
      <scope>compile</scope>
    </dependency>
    <dependency>
      <groupId>io.micronaut.configuration</groupId>
      <artifactId>micronaut-micrometer-registry-influx</artifactId>
      <scope>compile</scope>
    </dependency>
""")
        template.count("micronaut-micrometer-core") == 1
    }

    @Unroll
    void 'test micrometer configuration for feature=#micrometerFeature'() {
        when:
        CommandContext commandContext = buildCommandContext([micrometerFeature])

        then: 'the micrometer configuration is enabled for the feature'
        commandContext.configuration.get("micronaut.metrics.export.${configKey}.enabled".toString()) == true
        commandContext.configuration.get('micronaut.metrics.enabled') == true

        where:
        micrometerFeature << beanContext.getBeansOfType(MicrometerFeature)*.name.iterator()
        configKey = "${micrometerFeature - 'micrometer-'}".replace('-', '')
    }

    void 'test mandatory dependencies for micrometer features are added'() {
        when: 'a new feature context with one micrometer feature'
        Features features = getFeatures(["micrometer-appoptics"])

        then: 'the micrometer feature is added and also micrommeter-core and management'
        features.containsAll(['micrometer-appoptics', 'micrometer', 'management'])
    }

}