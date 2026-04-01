package com.sgd_hc.sgd_hc.security.config.tenant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DataSourceConfig {

    // BD Por Defecto
    @Value("${spring.datasource.url}")
    private String defaultUrl;
    @Value("${spring.datasource.username}")
    private String defaultUsername;
    @Value("${spring.datasource.password}")
    private String defaultPassword;

    // BD Clínica A
    @Value("${tenants.clinica-a.url}")
    private String clinicaAUrl;
    @Value("${tenants.clinica-a.username}")
    private String clinicaAUsername;
    @Value("${tenants.clinica-a.password}")
    private String clinicaAPassword;

    // BD Clínica B
    @Value("${tenants.clinica-b.url}")
    private String clinicaBUrl;
    @Value("${tenants.clinica-b.username}")
    private String clinicaBUsername;
    @Value("${tenants.clinica-b.password}")
    private String clinicaBPassword;

    @Bean
    @Primary // @Primary obliga a Hibernate/JPA a usar este DataSource y no el autoconfigurado
    public DataSource dataSource() {
        TenantRoutingDataSource routingDataSource = new TenantRoutingDataSource();

        // 1. Instanciar la BD por defecto y MIGRARLA
        DataSource defaultDataSource = DataSourceBuilder.create()
                .url(defaultUrl).username(defaultUsername).password(defaultPassword).build();
        runFlyway(defaultDataSource, "Default DB"); // <--- NUEVO

        // 2. Instanciar las BDs de las clínicas y MIGRARLAS
        DataSource clinicaADataSource = DataSourceBuilder.create()
                .url(clinicaAUrl).username(clinicaAUsername).password(clinicaAPassword).build();
        runFlyway(clinicaADataSource, "Clinica A"); // <--- NUEVO

        DataSource clinicaBDataSource = DataSourceBuilder.create()
                .url(clinicaBUrl).username(clinicaBUsername).password(clinicaBPassword).build();
        runFlyway(clinicaBDataSource, "Clinica B"); // <--- NUEVO

        // 3. Crear el "Mapa" de enrutamiento
        Map<Object, Object> targetDataSources = new HashMap<>();
        targetDataSources.put("clinica-a", clinicaADataSource);
        targetDataSources.put("clinica-b", clinicaBDataSource);

        // 4. Configurar el enrutador
        routingDataSource.setDefaultTargetDataSource(defaultDataSource);
        routingDataSource.setTargetDataSources(targetDataSources);
        routingDataSource.afterPropertiesSet();

        return routingDataSource;
    }

    private void runFlyway(DataSource dataSource, String tenantName) {
        System.out.println("Ejecutando migraciones Flyway para: " + tenantName);
        Flyway flyway = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .baselineVersion("0")
                .load();
        flyway.migrate();
    }
}